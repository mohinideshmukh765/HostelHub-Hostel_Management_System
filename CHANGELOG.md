# Changelog

All notable changes to HostelHub will be documented here.

> Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased] — Stage 3: Authorization Hardening

> Active engineering work. Items below reflect findings from Stage 1 source analysis documented in [`docs/auth/AUTHORIZATION_GAPS.md`](docs/auth/AUTHORIZATION_GAPS.md).

### To Add
- JWT validation filter at `api-gateway` — validate Bearer token before forwarding; reject unauthorized requests to protected paths
- Forward verified identity headers (`X-User-Id`, `X-User-Roles`) from gateway to downstream services
- `spring-boot-starter-security` dependency + `SecurityFilterChain` in `hostel-service`, `allocation-service`, `complaint-service`, `leave-service`
- `@PreAuthorize("hasRole('ADMIN')")` guards on hostel create/update/delete endpoints
- `@PreAuthorize("hasRole('ADMIN') or hasRole('WARDEN')")` guards on allocation create/checkout endpoints
- JWT-extracted principal replaces `?studentId=UUID` query parameter in complaint-service and leave-service
- JWT-extracted principal replaces `?wardenId=UUID` query parameter in complaint-service and leave-service
- Shared internal secret header (or network-level restriction) for `/api/internal/**` endpoints
- Move `jwt.secret` from `identity-service/src/main/resources/application.properties` to environment variable

### To Fix
- `POST /api/auth/admin/wardens` and `POST /api/auth/admin/admins` restricted from `permitAll()` to `hasRole('ADMIN')`
- `hostelId` assignment for warden/admin accounts — expose a setter endpoint so warden-hostel relationships can be established
- Null-pointer risk in `leave-service` when student has no active allocation (`fetchAllocation()` returns `null`; dereference follows)
- Align `@Future` constraint on `startDate` with service-level date check (contradiction: `@Future` rejects today; service-level allows today)

### To Remove
- `GET /api/auth/token-test` endpoint — debug endpoint that generates a real JWT for hardcoded user `mohini@example.com`
- `test-service` — scaffolding service with one endpoint and no business logic; registered in Eureka and routed through gateway

### To Investigate / Defer
- `SYSTEM_ADMIN` role — define what it should be allowed to do, or remove from `RoleName.java`
- `HostelWarden` entity in `hostel-service` — orphaned entity with no repository/service/controller; decide whether to implement the warden-hostel assignment flow or drop the entity
- `closedBy`, `rejectionReason`, `escalated` fields in complaint and leave entities — define code paths that populate them, or document as reserved

---

## [0.2.0] — Stage 2: Documentation (2026-08-29)

### Added
- Root `README.md`: project description, architecture diagram, tech stack table, quick-start guide, project highlights, known issues/roadmap
- `docs/architecture/service-diagram.md`: Mermaid service-to-service call graph
- `docs/architecture/deployment-diagram.md`: Mermaid startup order and dependency graph
- `docs/auth/ROLES_AND_PERMISSIONS.md`: Full Role × Action × Service permission matrix (pre-fix state)
- `docs/api/API_TESTING.md`: Full endpoint inventory across all 8 services with curl examples and failure cases
- `docs/api/postman/HostelHub.postman_collection.json`: Postman collection, one folder per service, with environment variable support
- `CHANGELOG.md` seeded with Stage 3 planned work

---

## [0.1.0] — Stage 1: Discovery (source analysis)

### Added
- `docs/architecture/SYSTEM_OVERVIEW.md`: Cross-service architecture, service map, config-server analysis, startup order, domain summary, auth findings
- `docs/services/identity-service.md`: Entities, endpoints, JWT rules, business rules, auth gaps
- `docs/services/hostel-service.md`: Entities, endpoints, business rules, auth gaps
- `docs/services/allocation-service.md`: Entities, endpoints, Feign dependencies, business rules, race condition note
- `docs/services/complaint-service.md`: Entities, endpoints, status machine, business rules, auth gaps
- `docs/services/leave-service.md`: Entities, endpoints, status machine, optimistic locking, business rules, auth gaps
- `docs/services/api-gateway.md`: Route table, what the gateway does and does not do
- `docs/services/config-server.md`: Centralized vs. local config inventory
- `docs/services/service-registry.md`: Eureka configuration and registered services
- `docs/auth/AUTHENTICATION.md`: JWT structure, token lifecycle, per-service enforcement
- `docs/auth/AUTHORIZATION_GAPS.md`: Full gap inventory with 7 findings, risk levels, and planned fixes
