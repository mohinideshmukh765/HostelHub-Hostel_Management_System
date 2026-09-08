# Roles & Permissions

> **State:** Pre-fix (Stage 1). Updated after Stage 3 authorization work is applied.
> Every entry is sourced from code — no assumptions.

---

## Roles Defined in Code

Source: `identity-service/.../entity/RoleName.java`

| Role | Defined in enum | Referenced in business logic | Notes |
|------|----------------|------------------------------|-------|
| `STUDENT` | ✅ | ✅ — `roles.contains("STUDENT")` checked in allocation, complaint, leave services | Default role on `register` |
| `WARDEN` | ✅ | ✅ — `roles.contains("WARDEN")` checked in complaint and leave services | Requires `hostelId` set on user for hostel-matching to work |
| `ADMIN` | ✅ | ❌ — defined and assignable; **no business rule in any service checks for ADMIN** | Can be created via `POST /api/auth/admin/admins` |
| `SYSTEM_ADMIN` | ✅ | ❌ — **never referenced in any code** | Dead code; undefined purpose |

---

## Permission Matrix

> **Legend:** ✅ Allowed and enforced · ⚠️ Partially enforced (some check runs but is bypassable) · ❌ Not enforced (open to all callers) · 🚫 Should be denied

### identity-service (port 8081)

| Endpoint | Method | ADMIN | WARDEN | STUDENT | Unauthenticated | Current enforcement | Intended (post-Stage 3) |
|----------|--------|-------|--------|---------|-----------------|---------------------|--------------------------|
| `/api/auth/register` | POST | ✅ | ✅ | ✅ | ✅ | `permitAll()` | `permitAll()` ✅ |
| `/api/auth/login` | POST | ✅ | ✅ | ✅ | ✅ | `permitAll()` | `permitAll()` ✅ |
| `/api/auth/refresh` | POST | ✅ | ✅ | ✅ | ✅ | `permitAll()` | `permitAll()` ✅ |
| `/api/auth/logout` | POST | ✅ | ✅ | ✅ | ✅ | `permitAll()` | `permitAll()` ✅ |
| `/api/auth/me` | GET | ✅ JWT | ✅ JWT | ✅ JWT | 🚫 | `authenticated()` + JWT filter | No change needed ✅ |
| `/api/auth/admin/wardens` | POST | ✅ | ✅ | ✅ | ✅ ❌ **GAP** | `permitAll()` | ADMIN only 🔧 |
| `/api/auth/admin/admins` | POST | ✅ | ✅ | ✅ | ✅ ❌ **GAP** | `permitAll()` | ADMIN only 🔧 |
| `/api/auth/token-test` | GET | ⚠️ | ⚠️ | ⚠️ | 🚫 | Auth required, but endpoint is a security risk | **DELETE endpoint** 🔧 |
| `/api/internal/users/{id}` | GET | ✅ | ✅ | ✅ | ✅ ❌ **GAP** | `permitAll()` | Internal secret header only 🔧 |

---

### hostel-service (port 8092) — No auth at all

| Endpoint | Method | Should allow | Currently allows | Gap |
|----------|--------|-------------|-----------------|-----|
| `POST /api/hostels` | POST | ADMIN | Anyone ❌ | No auth on service |
| `GET /api/hostels/{id}` | GET | All roles | Anyone ✅ | Intended |
| `GET /api/hostels` | GET | All roles | Anyone ✅ | Intended |
| `PUT /api/hostels/{id}` | PUT | ADMIN | Anyone ❌ | No auth on service |
| `DELETE /api/hostels/{id}` | DELETE | ADMIN | Anyone ❌ | No auth on service |
| `POST /api/hostels/{id}/rooms` | POST | ADMIN / WARDEN | Anyone ❌ | No auth on service |
| `GET /api/hostels/{id}/rooms` | GET | All roles | Anyone ✅ | Intended |
| `PUT /api/hostels/{id}/rooms/{rid}` | PUT | ADMIN / WARDEN | Anyone ❌ | No auth on service |
| `DELETE /api/hostels/{id}/rooms/{rid}` | DELETE | ADMIN / WARDEN | Anyone ❌ | No auth on service |
| `POST /api/hostels/{id}/rooms/{rid}/beds` | POST | ADMIN / WARDEN | Anyone ❌ | No auth on service |
| `GET /api/internal/beds/{id}` | GET | Internal services only | Any network caller ❌ | No auth; bypasses gateway |
| `PUT /api/internal/beds/{id}/occupy` | PUT | allocation-service only | Any network caller ❌ | No auth; bypasses gateway |
| `PUT /api/internal/beds/{id}/release` | PUT | allocation-service only | Any network caller ❌ | No auth; bypasses gateway |

---

### allocation-service (port 8093) — No auth at all

| Endpoint | Method | Should allow | Currently allows | Gap |
|----------|--------|-------------|-----------------|-----|
| `POST /api/allocations` | POST | ADMIN / WARDEN | Anyone ❌ | No auth on service |
| `GET /api/allocations/{id}` | GET | ADMIN / WARDEN / Owner | Anyone ❌ | No auth on service |
| `GET /api/allocations/student/{id}` | GET | ADMIN / WARDEN / Student (own) | Anyone ❌ | No auth on service |
| `POST /api/allocations/{id}/checkout` | POST | ADMIN / WARDEN | Anyone ❌ | No auth on service |
| `GET /api/internal/allocations/student/{id}` | GET | Internal services only | Any network caller ❌ | No auth; bypasses gateway |

---

### complaint-service (port 8094) — UUID param, impersonable

| Endpoint | Method | Should allow | Currently allows | Gap |
|----------|--------|-------------|-----------------|-----|
| `POST /api/complaints?studentId=X` | POST | Authenticated student X | Any caller supplying a valid student UUID ⚠️ | Impersonation |
| `GET /api/complaints/{id}` | GET | All | Anyone ❌ | Likely acceptable |
| `GET /api/complaints/student/{id}` | GET | ADMIN / WARDEN / Owner | Anyone ❌ | |
| `GET /api/complaints?status=` | GET | ADMIN / WARDEN | Anyone ❌ | |
| `PUT /api/complaints/{id}?studentId=X` | PUT | Authenticated student X (OPEN only) | Impersonable ⚠️ | |
| `PUT /api/complaints/{id}/status?wardenId=X` | PUT | Warden of that hostel | Hostel check runs; `wardenId` unverified ⚠️ | Anyone can supply a valid warden UUID |
| `DELETE /api/complaints/{id}?studentId=X` | DELETE | Authenticated student X (OPEN only) | Impersonable ⚠️ | |

---

### leave-service (port 8095) — UUID param, impersonable

| Endpoint | Method | Should allow | Currently allows | Gap |
|----------|--------|-------------|-----------------|-----|
| `POST /api/leaves?studentId=X` | POST | Authenticated student X | Impersonable ⚠️ | Any caller can apply for leave as any student |
| `GET /api/leaves/{id}` | GET | ADMIN / WARDEN / Owner | Anyone ❌ | |
| `GET /api/leaves/student/{id}` | GET | ADMIN / WARDEN / Owner | Anyone ❌ | |
| `GET /api/leaves/hostel/{id}` | GET | ADMIN / WARDEN of hostel | Anyone ❌ | |
| `GET /api/leaves` | GET | ADMIN / WARDEN | Anyone ❌ | |
| `PUT /api/leaves/{id}?studentId=X` | PUT | Student X (PENDING only) | Impersonable ⚠️ | |
| `DELETE /api/leaves/{id}?studentId=X` | DELETE | Student X (PENDING only) | Impersonable ⚠️ | |
| `PUT /api/leaves/{id}/accept?wardenId=X` | PUT | Warden of student's hostel | Hostel-match checked; `wardenId` unverified ⚠️ | |
| `PUT /api/leaves/{id}/status?wardenId=X` | PUT | Warden of student's hostel | Hostel-match checked; `wardenId` unverified ⚠️ | |

---

## Warden / Admin Assignment Constraints

| Question | Answer |
|----------|--------|
| Can a hostel have more than one warden? | Yes — no constraint exists |
| Can a warden be assigned to more than one hostel? | Yes — `user.hostelId` is a single UUID column; only one hostel |
| Is there a max-wardens-per-hostel rule? | No — none found anywhere in code |
| How does a warden's `hostelId` get set? | **Unknown — no endpoint found that sets `hostelId` on a user at creation time.** `createWarden()` and `createAdmin()` do not accept a `hostelId` parameter. |
| `HostelWarden` entity in hostel-service? | Orphaned — no repository, service, or controller uses it. Has a unique constraint on `(hostel_id, warden_id)` preventing duplicate assignments, but is unreachable. |

---

## Stage 3 Fix Plan

| Fix | Impact |
|----|--------|
| Add JWT validation filter at api-gateway; forward `X-User-Id` + `X-User-Roles` headers | Eliminates gateway pass-through gap; all services receive verified identity |
| Add `spring-boot-starter-security` + `@PreAuthorize` to hostel, allocation, complaint, leave services | Enforce role-based access per endpoint |
| Replace `?studentId=UUID` / `?wardenId=UUID` params with JWT-extracted claims | Eliminates impersonation across complaint and leave services |
| Restrict `/api/internal/**` via shared secret header or mTLS | Closes internal endpoint exposure |
| Move `jwt.secret` to environment variable | Closes committed-secret risk |
| Remove `GET /api/auth/token-test` | Eliminates debug token endpoint |
| Define or remove `SYSTEM_ADMIN` role | Dead code cleanup |

---

## See Also

- [AUTHORIZATION_GAPS.md](AUTHORIZATION_GAPS.md) — full gap inventory with risk levels
- [AUTHENTICATION.md](AUTHENTICATION.md) — JWT flow and token lifecycle
