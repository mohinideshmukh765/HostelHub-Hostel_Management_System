# Authorization Gaps — Stage 1 Findings

> Read-only discovery. Every finding is grounded in source code.
> No fixes applied here — this is the pre-fix record.

---

## Summary

The system has **authentication without authorization**. `identity-service` correctly
issues and validates JWTs. However, the api-gateway does not validate tokens, and
all downstream services have no Spring Security at all. Any caller who knows a
valid user UUID can perform any action on their behalf across all services.

---

## System-Wide Gap Table

| Service | Endpoint | Method | Should be allowed | Actually enforced | Gap |
|---------|----------|--------|-------------------|-------------------|-----|
| identity-service | `/api/auth/admin/wardens` | POST | ADMIN only | `permitAll()` | Anyone can create a warden |
| identity-service | `/api/auth/admin/admins` | POST | ADMIN only | `permitAll()` | Anyone can create an admin |
| identity-service | `/api/internal/users/**` | GET | Internal services only | `permitAll()` | External callers can fetch any user profile |
| identity-service | `/api/auth/token-test` | GET | Remove entirely | Auth required, but endpoint is dangerous | Debug token leak |
| hostel-service | `POST /api/hostels` | POST | ADMIN | No auth | Anyone |
| hostel-service | `PUT /api/hostels/{id}` | PUT | ADMIN | No auth | Anyone |
| hostel-service | `DELETE /api/hostels/{id}` | DELETE | ADMIN | No auth | Anyone |
| hostel-service | `POST /api/hostels/{id}/rooms` | POST | ADMIN / WARDEN | No auth | Anyone |
| hostel-service | `PUT /api/internal/beds/{id}/occupy` | PUT | Internal only | No auth | Any network caller |
| hostel-service | `PUT /api/internal/beds/{id}/release` | PUT | Internal only | No auth | Any network caller |
| allocation-service | `POST /api/allocations` | POST | ADMIN / WARDEN | No auth | Anyone can allocate any student |
| allocation-service | `POST /api/allocations/{id}/checkout` | POST | ADMIN / WARDEN | No auth | Anyone can check out any student |
| allocation-service | `GET /api/allocations/student/{studentId}` | GET | Student (own) / WARDEN / ADMIN | No auth | Any caller |
| allocation-service | `GET /api/internal/allocations/student/{id}` | GET | Internal only | No auth | Any network caller |
| complaint-service | `POST /api/complaints?studentId=X` | POST | Authenticated student X | UUID query param only | Impersonation: file complaint as any student |
| complaint-service | `PUT /api/complaints/{id}?studentId=X` | PUT | Authenticated student X | UUID query param only | Impersonation |
| complaint-service | `DELETE /api/complaints/{id}?studentId=X` | DELETE | Authenticated student X | UUID query param only | Impersonation |
| complaint-service | `PUT /api/complaints/{id}/status?wardenId=X` | PUT | Warden of that hostel | UUID param + hostel check (via Feign) | `wardenId` not verified to be the actual caller |
| complaint-service | `GET /api/complaints` | GET | ADMIN / WARDEN | No auth | Any caller sees all complaints |
| leave-service | `POST /api/leaves?studentId=X` | POST | Authenticated student X | UUID query param only | Impersonation |
| leave-service | `PUT /api/leaves/{id}?studentId=X` | PUT | Authenticated student X | UUID query param only | Impersonation |
| leave-service | `DELETE /api/leaves/{id}?studentId=X` | DELETE | Authenticated student X | UUID query param only | Impersonation |
| leave-service | `PUT /api/leaves/{id}/accept?wardenId=X` | PUT | Warden of that hostel | Hostel check via Feign, but `wardenId` unverified | Impersonation of any warden UUID |
| leave-service | `PUT /api/leaves/{id}/status?wardenId=X` | PUT | Warden of that hostel | Hostel check via Feign, but `wardenId` unverified | Impersonation |
| leave-service | `GET /api/leaves` | GET | ADMIN / WARDEN | No auth | Any caller sees all leave records |
| leave-service | `GET /api/leaves/hostel/{hostelId}` | GET | WARDEN of that hostel / ADMIN | No auth | Anyone |

---

## Finding 1 — No Token Validation at Gateway

- **Affects:** All services
- **Issue:** api-gateway has zero auth filters. All tokens (or lack thereof) pass through.
- **Risk:** HIGH — foundational architectural gap
- **Fix (Stage 3):** Add JWT validation filter at the gateway; forward verified identity headers downstream

---

## Finding 2 — No Spring Security on Business Services

- **Affects:** hostel-service, allocation-service, complaint-service, leave-service
- **Issue:** No `spring-security` dependency configured. No `SecurityFilterChain`.
- **Risk:** HIGH — even if the gateway had auth, services can be called directly on their ports
- **Fix (Stage 3):** Add Spring Security + method-level `@PreAuthorize` to each service

---

## Finding 3 — Identity via UUID Query Parameter (Impersonation)

- **Affects:** complaint-service, leave-service, allocation-service
- **Issue:** `?studentId=` and `?wardenId=` are untrusted caller-supplied parameters. No proof the caller owns that identity.
- **Risk:** CRITICAL — any user can act as any other user
- **Fix (Stage 3):** Replace UUID params with claims extracted from validated JWT

---

## Finding 4 — Internal Endpoints Exposed to Network

- **Affects:** identity-service (`/api/internal/users/**`), hostel-service (`/api/internal/beds/**`), allocation-service (`/api/internal/allocations/**`)
- **Issue:** These endpoints are not routed through the gateway, but they're reachable at the service port with no authentication.
- **Risk:** HIGH — bed state can be flipped by any caller; user PII is readable by anyone
- **Fix (Stage 3):** Restrict internal endpoints via network policy, or add a shared internal secret header, or use service-mesh mTLS

---

## Finding 5 — Admin/Warden Creation Without Auth

- **Affects:** identity-service
- **Issue:** `POST /api/auth/admin/wardens` and `POST /api/auth/admin/admins` are `permitAll()` in `SecurityConfig.java`
- **Risk:** HIGH — anyone can promote themselves or others to WARDEN or ADMIN
- **Fix (Stage 3):** Require `ROLE_ADMIN` on these endpoints

---

## Finding 6 — Debug Endpoint in Production Code

- **Affects:** identity-service
- **Issue:** `GET /api/auth/token-test` generates a real JWT for the hardcoded user `mohini@example.com`. It requires authentication, so it won't work without a token — but it's a code smell and should not exist in any deployed environment.
- **Risk:** MEDIUM
- **Fix:** Delete `AuthController.tokenTest()` method

---

## Finding 7 — JWT Secret Committed to Source

- **Affects:** identity-service
- **Issue:** `jwt.secret` is hardcoded in `application.properties` (Base64 of "ThisIsASecureSecretKeyForJWTTestingPurposesOnly1234567890")
- **Risk:** HIGH — if the repo is public, the secret is compromised; tokens can be forged
- **Fix (Stage 3):** Move to environment variable or secrets manager; rotate the secret
