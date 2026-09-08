# HostelHub — System Overview

> Read-only discovery pass — Stage 1. Ground truth from source code.
> No assumptions; every claim cites the file it was found in.

---

## 1. Services at a Glance

| Service | Port | DB | Eureka registration |
|---------|------|----|-------------------|
| config-server | 8888 | none | ❌ (standalone) |
| service-registry | 8761 | none | ❌ (`register-with-eureka=false`) |
| api-gateway | 8080 | none | ✅ |
| identity-service | 8081 | `identity_db` (PostgreSQL) | ✅ |
| hostel-service | 8092 | `hostel_db` (PostgreSQL) | ✅ |
| allocation-service | 8093 | `allocation_db` (PostgreSQL) | ✅ |
| complaint-service | 8094 | `complaint_db` (PostgreSQL) | ✅ |
| leave-service | 8095 | `hostelhub_leave_db` (PostgreSQL) | ✅ |
| **test-service** | **8090** | **none** | **✅** |

> **test-service verdict**: pure scaffolding. One controller, one endpoint
> (`GET /api/test/hello`), no entities, no business logic. It is routed
> through the gateway (`/api/test/**`) and registers with Eureka.
> It should be removed before any production deployment.

---

## 2. Inter-Service Call Graph (Feign clients found in source)

```
allocation-service  →  identity-service   (GET /api/internal/users/{id})
allocation-service  →  hostel-service     (GET  /api/internal/beds/{id})
                                          (PUT  /api/internal/beds/{id}/occupy)
                                          (PUT  /api/internal/beds/{id}/release)

complaint-service   →  identity-service   (GET /api/internal/users/{userId})
complaint-service   →  allocation-service (GET /api/internal/allocations/student/{studentId})

leave-service       →  identity-service   (GET /api/internal/users/{id})
leave-service       →  allocation-service (GET /api/internal/allocations/student/{studentId})

hostel-service      → (no outbound Feign calls found; @EnableFeignClients present
                        but no client interfaces defined in source tree)

identity-service    → (no outbound Feign calls)
```

**Direction summary (caller → callee):**

```
api-gateway
  └─► identity-service
  └─► hostel-service
  └─► allocation-service
  └─► complaint-service
  └─► leave-service
  └─► test-service

allocation-service
  └─► identity-service
  └─► hostel-service

complaint-service
  └─► identity-service
  └─► allocation-service

leave-service
  └─► identity-service
  └─► allocation-service
```

All Feign clients resolve by Eureka service name (e.g. `@FeignClient(name = "identity-service")`).
**No hardcoded IP/port URLs found** in any Feign client.

---

## 3. Service Discovery

- All business services set `spring.config.import=optional:configserver:http://localhost:8888`
  and pull `eureka.client.service-url.defaultZone=http://localhost:8761/eureka/` from
  config-server's per-service `.properties` files.
- **Exception — complaint-service**: `eureka.client.service-url.defaultZone` is *also* defined
  locally in `complaint-service/src/main/resources/application.properties`
  (duplicates the config-server value, so no functional inconsistency, but it's noise).
- **Exception — identity-service**: does NOT use `spring.config.import`; all config is
  entirely local in `identity-service/src/main/resources/application.properties`.
  It is the only business service that does not pull from config-server.
- api-gateway and service-registry also do not import from config-server — they are
  self-contained.

---

## 4. What Config-Server Actually Centralises

Config-server uses `spring.profiles.active=native` with
`spring.cloud.config.server.native.search-locations=classpath:/config`.

| Config file | Properties it holds |
|-------------|---------------------|
| `config/application.properties` | Global: `hostelhub.message`, actuator exposure |
| `config/allocation-service.properties` | Port 8093, DB URL, JPA, Eureka, actuator |
| `config/complaint-service.properties` | Port 8094, DB URL, JPA, Eureka instance |
| `config/hostel-service.properties` | Port 8092, DB URL, JPA, Eureka |
| `config/leave-service.properties` | Port 8095, DB URL, JPA, Eureka |

**Not centralised (local-only):**
- identity-service — all config is local (port, DB URL, JWT secret, Eureka)
- api-gateway — local only
- service-registry — local only

**Inconsistency found:** `leave-service.properties` (config-server) has
`spring.datasource.password=postgres` but other services use `password=mohini`.
This suggests leave-service may have been created by a different developer or at a
different time.

---

## 5. API Gateway — What It Actually Does Today

Source: `api-gateway/src/main/resources/application.properties`

The gateway does **only** path-based routing. It does **not**:
- validate JWT tokens
- add/forward any auth headers to downstream services
- apply rate limiting
- apply logging filters
- do any other pre/post processing

**Routes configured (Spring Cloud Gateway MVC, `lb://` service-name):**

| Route ID | Predicate | Upstream |
|----------|-----------|----------|
| `test-service-route` | `Path=/api/test/**` | `lb://test-service` |
| `identity-service-route` | `Path=/api/auth/**` | `lb://identity-service` |
| `allocation-service` | `Path=/api/allocations/**` | `lb://allocation-service` |
| `hostel-service` | `Path=/api/hostels/**` | `lb://hostel-service` |
| `complaint-service` | `Path=/api/complaints/**` | `lb://complaint-service` |
| `leave-service` | `Path=/api/leaves/**` | `lb://leave-service` |

> ⚠️ **Security gap**: `/api/internal/**` endpoints (used for inter-service calls)
> are NOT routed through the gateway — they are directly accessible on the service
> ports. There is no network policy preventing external callers from hitting them
> directly.

---

## 6. Startup Order (Dependency Graph)

```
① config-server (8888)     — must start first; no dependencies
② service-registry (8761)  — no config-server dependency; must be up before
                              any @EnableDiscoveryClient service starts
③ identity-service (8081)  — no config-server dependency; needs Eureka
③ hostel-service (8092)    — needs config-server + Eureka
③ allocation-service (8093)— needs config-server + Eureka
③ complaint-service (8094) — needs config-server + Eureka
③ leave-service (8095)     — needs config-server + Eureka
④ api-gateway (8080)       — needs Eureka (to resolve lb:// routes)
```

---

## 7. Domain Summary (cross-service)

Each service owns its own PostgreSQL database.
Cross-service references are UUIDs stored as plain columns — no database-level
foreign keys across service boundaries.

| Cross-service reference | Stored in | Points to | Verified at write? |
|-------------------------|-----------|-----------|-------------------|
| `allocation.studentId` | allocation_db | identity_db.users.id | ✅ via Feign (`identityClient.getStudent`) |
| `allocation.bedId` | allocation_db | hostel_db.beds.id | ✅ via Feign (`hostelClient.getBed`) |
| `allocation.hostelId` | allocation_db | hostel_db.hostels.id | ❌ copied from `bed.hostelId` at write time; not independently verified |
| `complaint.studentId` | complaint_db | identity_db.users.id | ✅ via Feign |
| `complaint.hostelId` | complaint_db | hostel_db.hostels.id | ❌ sourced from allocation, not verified against hostel-service |
| `complaint.roomId` | complaint_db | hostel_db.rooms.id | ❌ convention only |
| `complaint.bedId` | complaint_db | hostel_db.beds.id | ❌ convention only |
| `leave.studentId` | leave_db | identity_db.users.id | ✅ via Feign |
| `leave.hostelId` | leave_db | hostel_db.hostels.id | ❌ sourced from allocation; not independently verified |
| `leave.roomId` | leave_db | hostel_db.rooms.id | ❌ convention only |
| `leave.bedId` | leave_db | hostel_db.beds.id | ❌ convention only |
| `hostel.adminId` | hostel_db | identity_db.users.id | ❌ not verified; plain UUID column |
| `hostelwarden.wardenId` | hostel_db | identity_db.users.id | ❌ not verified |
| `user.hostelId` | identity_db | hostel_db.hostels.id | ❌ not verified |

> **Critical observation:** `HostelWarden` entity exists in hostel-service but is
> not referenced from any service, controller, or Feign client found in the current
> codebase. It appears to be an abandoned or future entity.

---

## 8. Roles Found in Code

Defined in `identity-service/…/entity/RoleName.java`:

```java
STUDENT, WARDEN, ADMIN, SYSTEM_ADMIN
```

`SYSTEM_ADMIN` is **defined in the enum but never referenced** in any controller,
service, or security rule found in the codebase.

---

## 9. Authentication vs Authorization — System-Wide Finding

**identity-service** has a real Spring Security + JWT stack:
- `JwtService` issues access tokens (15 min expiry) and refresh tokens (7 days)
- `JwtAuthenticationFilter` validates Bearer tokens on inbound requests to identity-service only
- `SecurityConfig` permits `/api/auth/**`, `/api/internal/users/**`, and admin endpoints without auth

**All other services (hostel, allocation, complaint, leave)**:
- Have **no Spring Security dependency**, **no JWT filter**, and **no authentication mechanism**
- They accept whoever calls them — any UUID passed as a `@RequestParam studentId`
  or `@RequestParam wardenId` is trusted unconditionally

**Result:** The entire authorization model in the business services is
**honour-system only**. A caller who knows any student's UUID can create complaints,
leave requests, etc. in their name. See per-service tables in `docs/services/`.

---

## 10. See Also

- [service-diagram.md](service-diagram.md) — Mermaid call graph
- [deployment-diagram.md](deployment-diagram.md) — startup order
- Per-service deep dives in [`docs/services/`](../services/)
- [AUTHENTICATION.md](../auth/AUTHENTICATION.md) — auth flow detail
- [AUTHORIZATION_GAPS.md](../auth/AUTHORIZATION_GAPS.md) — gap inventory
