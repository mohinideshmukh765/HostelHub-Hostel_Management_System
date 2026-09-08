# API Gateway

> Port: **8080** · DB: none · Config: **local only**

---

## Responsibility

Single HTTP entry point for all external clients. Routes requests to the correct
downstream service using Spring Cloud Gateway (MVC variant) with Eureka-based
load-balancing (`lb://` URIs).

**It does nothing else today.** No auth filtering, no rate limiting, no logging filters,
no request/response transformation.

---

## Route Table

Source: `api-gateway/src/main/resources/application.properties`

| Route ID | Path Predicate | Upstream (Eureka name) | Notes |
|----------|---------------|------------------------|-------|
| `test-service-route` | `/api/test/**` | `lb://test-service` | Scaffolding only |
| `identity-service-route` | `/api/auth/**` | `lb://identity-service` | |
| `allocation-service` | `/api/allocations/**` | `lb://allocation-service` | |
| `hostel-service` | `/api/hostels/**` | `lb://hostel-service` | |
| `complaint-service` | `/api/complaints/**` | `lb://complaint-service` | |
| `leave-service` | `/api/leaves/**` | `lb://leave-service` | |

> **Not routed through gateway:** `/api/internal/**` — internal endpoints are
> exposed directly on each service's port. No network policy prevents external
> callers from reaching them.

---

## Entities / Data Model

N/A — stateless routing layer. No database.

---

## Service Dependencies

| Direction | Service | Reason |
|-----------|---------|--------|
| Routes to | All business services | Path-based routing |
| Registers with | service-registry | To resolve `lb://` URIs |

---

## What's Missing (Authorization Gaps)

A production-grade gateway would:
- Validate the JWT on every incoming request before forwarding
- Forward verified identity (e.g. `X-User-Id`, `X-User-Roles` headers) to downstream services
- Reject unauthenticated requests to protected paths

**None of the above is present today.** The gateway forwards all requests regardless
of whether they carry a valid token.

---

## Known-Suspect Areas

1. **`test-service-route`** should be removed along with `test-service` itself.
2. The gateway uses the MVC variant (`spring.cloud.gateway.server.webmvc.routes`), not the reactive WebFlux variant. This is fine but worth noting if a reactive filter is ever added.
