# HostelHub — Environment & Database Setup Guide

> Detailed setup reference for getting all 5 databases and all 8 services running locally.
> Quick-start lives in the [root README](README.md). This doc covers edge cases, credentials, and troubleshooting.

---

## Table of Contents

- [Port Allocation](#port-allocation)
- [Database Setup](#database-setup)
  - [Create Databases](#create-databases)
  - [Credentials per Service](#credentials-per-service)
- [Environment Variables](#environment-variables)
  - [Template: application-local.properties](#template-application-localproperties)
- [Config-Server Properties Reference](#config-server-properties-reference)
- [Service-Level Config Quirks](#service-level-config-quirks)
- [Troubleshooting](#troubleshooting)

---

## Port Allocation

| Service | Port | Notes |
|---------|------|-------|
| `config-server` | `8888` | Must start first |
| `service-registry` (Eureka) | `8761` | Must start second |
| `identity-service` | `8081` | Does **not** use config-server |
| `hostel-service` | `8092` | Uses config-server |
| `allocation-service` | `8093` | Uses config-server |
| `complaint-service` | `8094` | Uses config-server |
| `leave-service` | `8095` | Uses config-server |
| `api-gateway` | `8080` | Must start last; public entry point |
| `test-service` | `8090` | ⚠️ Scaffolding — will be removed |

> All ports can be overridden via `server.port` in the relevant `application-local.properties`.

---

## Database Setup

### Create Databases

Connect to your PostgreSQL instance and run:

```sql
-- Run as a superuser (e.g. the 'postgres' user)
CREATE DATABASE identity_db;
CREATE DATABASE hostel_db;
CREATE DATABASE allocation_db;
CREATE DATABASE complaint_db;
CREATE DATABASE hostelhub_leave_db;
```

All services use **Hibernate DDL auto** (`spring.jpa.hibernate.ddl-auto=update` or `create-drop`) — tables are created automatically on first startup. **No migration scripts are needed.**

### Credentials per Service

| Service | Database | Username | Password | Sourced from |
|---------|----------|----------|----------|-------------|
| `identity-service` | `identity_db` | `postgres` | `mohini` | Local `application.properties` |
| `hostel-service` | `hostel_db` | `postgres` | `mohini` | config-server `hostel-service.properties` |
| `allocation-service` | `allocation_db` | `postgres` | `mohini` | config-server `allocation-service.properties` |
| `complaint-service` | `complaint_db` | `postgres` | `mohini` | config-server `complaint-service.properties` |
| `leave-service` | `hostelhub_leave_db` | `postgres` | `postgres` | config-server `leave-service.properties` ⚠️ |

> ⚠️ **Known inconsistency:** `leave-service` uses password `postgres` while all other services use `mohini`. This appears to be a copy-paste oversight. Fix by editing `config-server/src/main/resources/config/leave-service.properties` to match your local password, or override in `application-local.properties` as shown below.

---

## Environment Variables

The following values should **not** be committed to git. Use `application-local.properties` (already in `.gitignore`) for local overrides.

| Variable | Service | Default (dev only!) | Description |
|----------|---------|--------------------|----|
| `spring.datasource.password` | all | `mohini` / `postgres` | PostgreSQL password |
| `spring.datasource.username` | all | `postgres` | PostgreSQL username |
| `spring.datasource.url` | all | `jdbc:postgresql://localhost:5432/<db>` | DB connection URL |
| `jwt.secret` | `identity-service` | hardcoded in `.properties` ⚠️ | HS256 signing secret (must be ≥256 bits) |
| `jwt.access-token-expiry` | `identity-service` | `900000` (15 min in ms) | Access token TTL |
| `jwt.refresh-token-expiry` | `identity-service` | `604800000` (7 days in ms) | Refresh token TTL |
| `server.port` | all | (see table above) | Override if ports conflict |

### Template: application-local.properties

Create this file in each service's `src/main/resources/` directory. It is excluded from git by the root `.gitignore`.

**For `identity-service`:**
```properties
# identity-service/src/main/resources/application-local.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/identity_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD_HERE
jwt.secret=YOUR_AT_LEAST_256_BIT_SECRET_HERE
```

**For `hostel-service` / `allocation-service` / `complaint-service`:**
```properties
# e.g. hostel-service/src/main/resources/application-local.properties
spring.datasource.password=YOUR_PASSWORD_HERE
```

**For `leave-service`:**
```properties
# leave-service/src/main/resources/application-local.properties
spring.datasource.password=YOUR_PASSWORD_HERE
```

> Spring Boot automatically merges `application-local.properties` when you set `spring.profiles.active=local`, or you can use `spring.config.additional-location` to load it without activating a profile.

---

## Config-Server Properties Reference

The config-server uses `spring.profiles.active=native` and serves property files from `classpath:/config`.

| File | Services it configures | Key properties |
|------|----------------------|----------------|
| `config/application.properties` | all (global) | `hostelhub.message`, actuator exposure |
| `config/hostel-service.properties` | `hostel-service` | port 8092, DB URL, JPA, Eureka |
| `config/allocation-service.properties` | `allocation-service` | port 8093, DB URL, JPA, Eureka |
| `config/complaint-service.properties` | `complaint-service` | port 8094, DB URL, JPA, Eureka |
| `config/leave-service.properties` | `leave-service` | port 8095, DB URL, JPA, Eureka |

**Services that do NOT use config-server (use local config only):**
- `identity-service` — all config in `identity-service/src/main/resources/application.properties`
- `api-gateway` — all config in `api-gateway/src/main/resources/application.properties`
- `service-registry` — all config in `service-registry/src/main/resources/application.properties`

---

## Service-Level Config Quirks

| Quirk | Detail |
|-------|--------|
| `identity-service` skips config-server | It does NOT set `spring.config.import`. If you need to change its DB or JWT config, edit its local `application.properties` (or `application-local.properties`). |
| `optional:` import prefix | Business services use `optional:configserver:http://localhost:8888`. If config-server is not running, they start silently with missing DB config and fail at first DB operation. There is no `fail-fast=true`. |
| `complaint-service` Eureka duplication | `eureka.client.service-url.defaultZone` is defined both locally and in config-server for complaint-service. Functionally identical; the local value overrides config-server. |
| `leave-service` password mismatch | See [Credentials per Service](#credentials-per-service). |

---

## Troubleshooting

### Service fails to start with `Connection refused` on port 5432
PostgreSQL is not running. Start it with:
```bash
# Linux/macOS
sudo service postgresql start

# Windows
net start postgresql-x64-14   # adjust version suffix as needed
```

### Service starts but Feign calls fail with `No instances available`
The target service is not registered in Eureka. Check http://localhost:8761 — the service should appear in the list. If it's missing, check that service's startup logs for errors.

### `leave-service` throws NullPointerException on leave creation
This is a [known bug](README.md#known-issues--roadmap): `fetchAllocation()` returns `null` for students with no active allocation, and the result is dereferenced without a null check. Ensure the student has an `ACTIVE` allocation before creating a leave request.

### Gateway returns 503 for all routes
The api-gateway could not resolve any `lb://` service names from Eureka at startup. Ensure all business services are registered in Eureka **before** starting the gateway.

### `jwt.secret` error on identity-service startup
The secret must be a Base64-encoded string of at least 256 bits (32 bytes). Generate one with:
```bash
openssl rand -base64 32
```
