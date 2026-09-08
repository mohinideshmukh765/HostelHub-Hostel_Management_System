# Authentication

> How authentication currently works in HostelHub — Stage 1 ground truth.

---

## Flow Overview

```
Client
  │
  ▼
POST /api/auth/login  (through api-gateway → identity-service)
  │
  │  identity-service validates email + BCrypt password
  │  Issues:  access token  (JWT, 15 min, HS256)
  │           refresh token (UUID string, 7 days, stored in DB)
  │
  ▼
Client stores both tokens
  │
  ▼
Subsequent requests → api-gateway → downstream service
  │
  │  api-gateway does NOT validate the token.
  │  It forwards the raw request as-is.
  │
  ▼
Downstream service (hostel, allocation, complaint, leave)
  │
  │  Has NO Spring Security, NO JWT filter, NO token validation.
  │  Accepts the request unconditionally.
  │  Role checks (e.g. "is this studentId a STUDENT?") are performed
  │  by calling identity-service via Feign to fetch the user record.
  │
  ▼
Result: the "caller's identity" is whatever UUID they put in the request param.
```

---

## JWT Structure

Source: `JwtService.java` (`identity-service`)

**Header:** `alg: HS256` (HMAC-SHA256)

**Payload claims:**

| Claim | Value |
|-------|-------|
| `sub` | User's email address |
| `userId` | User's UUID |
| `roles` | `List<String>` e.g. `["STUDENT"]` |
| `iat` | Issued-at timestamp |
| `exp` | Expiry timestamp |

**Signing key:** Base64-decoded value of `jwt.secret` from `identity-service/src/main/resources/application.properties`.

> ⚠️ The JWT secret is hardcoded in the local `application.properties` file:
> `jwt.secret=VGhpc0lzQVNlY3VyZVNlY3JldEtleUZvckpXVFRlc3RpbmdQdXJwb3Nlc09ubHkxMjM0NTY3ODkw`
> It is NOT in config-server, NOT in an environment variable, and NOT gitignored.
> This is a development secret — it must be replaced and externalized before production.

---

## Token Lifecycle

| Token | Expiry | Storage |
|-------|--------|---------|
| Access token (JWT) | 15 minutes (`jwt.access-token-expiration=900000` ms) | Client-side only |
| Refresh token | 7 days (`jwt.refresh-token-expiration=604800000` ms) | DB (`refresh_tokens` table) |

**Refresh flow:** `POST /api/auth/refresh` with `{ "refreshToken": "..." }` → returns a new access token.

**Logout:** `POST /api/auth/logout` with `{ "refreshToken": "..." }` → sets `revoked=true` on the DB record. Does **not** invalidate the access token (it remains valid until expiry).

---

## What identity-service Enforces

Spring Security is configured in `SecurityConfig.java`:

| Path | Security |
|------|----------|
| `/api/auth/register` | Permit all |
| `/api/auth/login` | Permit all |
| `/api/auth/refresh` | Permit all |
| `/api/demo/public` | Permit all |
| `/api/internal/users/**` | Permit all |
| `/api/admin/wardens` | Permit all (**bug: should be ADMIN only**) |
| `/api/admin/admins` | Permit all (**bug: should be ADMIN only**) |
| Any other path | `authenticated()` — JWT required |

The `JwtAuthenticationFilter` validates the Bearer token **only for requests to identity-service itself**.

---

## What All Other Services Enforce

**Nothing.** Hostel-service, allocation-service, complaint-service, and leave-service
have no Spring Security configuration. They accept every request. "Validation" of
the caller's role is done by fetching the claimed user ID from identity-service, which
proves the UUID exists and has a role — but does NOT prove the caller IS that user.

---

## Gateway Filter

The api-gateway does **not** validate tokens. It has no `GlobalFilter`, no
`GatewayFilter`, no `AuthenticationWebFilter`. It is a plain routing proxy.
