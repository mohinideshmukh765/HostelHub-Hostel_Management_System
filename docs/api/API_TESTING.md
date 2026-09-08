# API Testing Guide

> Full endpoint inventory across all 8 services with curl examples, sample request bodies, expected success responses, and realistic failure cases.
> All examples are grounded in the actual entity fields and validation rules found in source code (Stage 1 analysis).

---

## Prerequisites

```bash
BASE_URL="http://localhost:8080"          # via api-gateway (recommended)
IDENTITY_URL="http://localhost:8081"      # direct (if gateway is not running)

# Obtain a JWT token (replace with real credentials after registering):
TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"student@example.com","password":"password123"}' \
  | jq -r '.accessToken')

echo "Token: $TOKEN"
```

> ⚠️ **Current state:** The API gateway does not validate JWT tokens. Downstream business services (hostel, allocation, complaint, leave) also have no auth. The `Authorization` header is shown in examples for correctness — but today it is not enforced beyond identity-service.

---

## Endpoint Inventory — All Services

| # | Service | Method | Path | Auth needed (intended) | Notes |
|---|---------|--------|------|------------------------|-------|
| 1 | identity-service | POST | `/api/auth/register` | None | Creates STUDENT role |
| 2 | identity-service | POST | `/api/auth/login` | None | Returns JWT + refresh token |
| 3 | identity-service | POST | `/api/auth/refresh` | None | Exchange refresh token |
| 4 | identity-service | POST | `/api/auth/logout` | None | Revoke refresh token |
| 5 | identity-service | GET | `/api/auth/me` | JWT | Own profile |
| 6 | identity-service | POST | `/api/auth/admin/wardens` | ADMIN (currently open) | Create warden account |
| 7 | identity-service | POST | `/api/auth/admin/admins` | ADMIN (currently open) | Create admin account |
| 8 | identity-service | GET | `/api/internal/users/{id}` | Internal (currently open) | User lookup for Feign |
| 9 | hostel-service | POST | `/api/hostels` | ADMIN (currently open) | Create hostel |
| 10 | hostel-service | GET | `/api/hostels/{id}` | None | Get hostel |
| 11 | hostel-service | GET | `/api/hostels` | None | Search hostels (paginated) |
| 12 | hostel-service | PUT | `/api/hostels/{id}` | ADMIN (currently open) | Update hostel |
| 13 | hostel-service | DELETE | `/api/hostels/{id}` | ADMIN (currently open) | Delete hostel |
| 14 | hostel-service | POST | `/api/hostels/{id}/rooms` | ADMIN/WARDEN (currently open) | Create room |
| 15 | hostel-service | GET | `/api/hostels/{id}/rooms` | None | List/filter rooms |
| 16 | hostel-service | GET | `/api/hostels/{id}/rooms/{rid}` | None | Get room |
| 17 | hostel-service | PUT | `/api/hostels/{id}/rooms/{rid}` | ADMIN/WARDEN (currently open) | Update room |
| 18 | hostel-service | DELETE | `/api/hostels/{id}/rooms/{rid}` | ADMIN/WARDEN (currently open) | Delete room |
| 19 | hostel-service | POST | `/api/hostels/{id}/rooms/{rid}/beds` | ADMIN/WARDEN (currently open) | Create bed |
| 20 | hostel-service | GET | `/api/hostels/{id}/rooms/{rid}/beds` | None | List beds |
| 21 | hostel-service | GET | `/api/hostels/{id}/rooms/{rid}/beds/{bid}` | None | Get bed |
| 22 | hostel-service | PUT | `/api/hostels/{id}/rooms/{rid}/beds/{bid}` | ADMIN/WARDEN (currently open) | Update bed |
| 23 | hostel-service | DELETE | `/api/hostels/{id}/rooms/{rid}/beds/{bid}` | ADMIN/WARDEN (currently open) | Delete bed |
| 24 | allocation-service | POST | `/api/allocations` | ADMIN/WARDEN (currently open) | Create allocation |
| 25 | allocation-service | GET | `/api/allocations/{id}` | Auth (currently open) | Get allocation |
| 26 | allocation-service | GET | `/api/allocations/student/{studentId}` | Auth (currently open) | Student's allocations |
| 27 | allocation-service | POST | `/api/allocations/{id}/checkout` | ADMIN/WARDEN (currently open) | Checkout student |
| 28 | complaint-service | POST | `/api/complaints` | Student (currently: UUID param) | File complaint |
| 29 | complaint-service | GET | `/api/complaints/{id}` | None | Get complaint |
| 30 | complaint-service | GET | `/api/complaints/student/{id}` | Auth (currently open) | Student's complaints |
| 31 | complaint-service | GET | `/api/complaints` | ADMIN/WARDEN (currently open) | All complaints |
| 32 | complaint-service | PUT | `/api/complaints/{id}` | Student owner (currently: UUID param) | Update complaint |
| 33 | complaint-service | PUT | `/api/complaints/{id}/status` | Warden (currently: UUID param) | Update complaint status |
| 34 | complaint-service | DELETE | `/api/complaints/{id}` | Student owner (currently: UUID param) | Delete complaint |
| 35 | leave-service | POST | `/api/leaves` | Student (currently: UUID param) | Apply for leave |
| 36 | leave-service | GET | `/api/leaves/{id}` | Auth (currently open) | Get leave |
| 37 | leave-service | GET | `/api/leaves/student/{id}` | Auth (currently open) | Student's leaves |
| 38 | leave-service | GET | `/api/leaves/hostel/{id}` | ADMIN/WARDEN (currently open) | Hostel's leaves |
| 39 | leave-service | GET | `/api/leaves` | ADMIN/WARDEN (currently open) | All leaves |
| 40 | leave-service | PUT | `/api/leaves/{id}` | Student owner (currently: UUID param) | Update leave |
| 41 | leave-service | DELETE | `/api/leaves/{id}` | Student owner (currently: UUID param) | Cancel leave |
| 42 | leave-service | PUT | `/api/leaves/{id}/accept` | Warden (currently: UUID param) | Move to IN_REVIEW |
| 43 | leave-service | PUT | `/api/leaves/{id}/status` | Warden (currently: UUID param) | Approve/Reject |

---

## identity-service (port 8081)

### POST /api/auth/register

```bash
curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Priya",
    "lastName": "Sharma",
    "email": "priya.sharma@example.com",
    "password": "securePass1"
  }'
```

**Success (201):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "user": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "firstName": "Priya",
    "lastName": "Sharma",
    "email": "priya.sharma@example.com",
    "roles": ["STUDENT"],
    "status": "ACTIVE"
  }
}
```

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| Email already registered | 409 | `{"error": "Email already registered"}` |
| Password shorter than 8 chars | 400 | `{"error": "Validation failed", "details": {"password": "size must be between 8 and 2147483647"}}` |
| Missing required field | 400 | `{"error": "Validation failed", "details": {"email": "must not be blank"}}` |

---

### POST /api/auth/login

```bash
curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "priya.sharma@example.com",
    "password": "securePass1"
  }'
```

**Success (200):** Same shape as `/register` response.

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| Wrong password | 401 | `{"error": "Invalid credentials"}` |
| Email not found | 401 | `{"error": "Invalid credentials"}` |
| Account LOCKED or INACTIVE | 401 | `{"error": "Account is not active"}` |

---

### POST /api/auth/refresh

```bash
curl -s -X POST "$BASE_URL/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "550e8400-e29b-41d4-a716-446655440000"}'
```

**Success (200):** `{"accessToken": "eyJhbGci..."}` (new access token)

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| Token not found | 404 | `{"error": "Refresh token not found"}` |
| Token revoked | 401 | `{"error": "Refresh token has been revoked"}` |
| Token expired (>7 days) | 401 | `{"error": "Refresh token has expired"}` |

---

### POST /api/auth/logout

```bash
curl -s -X POST "$BASE_URL/api/auth/logout" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "550e8400-e29b-41d4-a716-446655440000"}'
```

**Success (200):** `{"message": "Logged out successfully"}`

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| Token not found | 404 | `{"error": "Refresh token not found"}` |
| Token already revoked | 400 | `{"error": "Refresh token has already been revoked"}` |

---

### GET /api/auth/me

```bash
curl -s "$BASE_URL/api/auth/me" \
  -H "Authorization: Bearer $TOKEN"
```

**Success (200):**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "firstName": "Priya",
  "lastName": "Sharma",
  "email": "priya.sharma@example.com",
  "roles": ["STUDENT"],
  "status": "ACTIVE",
  "hostelId": null
}
```

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| No token | 403 | Spring Security 403 |
| Expired token | 401 | `{"error": "JWT token expired"}` |

---

### POST /api/auth/admin/wardens

> ⚠️ Currently `permitAll()` — no token required. Should be ADMIN only.

```bash
curl -s -X POST "$BASE_URL/api/auth/admin/wardens" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Rajesh",
    "lastName": "Kumar",
    "email": "rajesh.kumar@hostel.edu",
    "password": "wardenPass1"
  }'
```

**Success (201):**
```json
{
  "accessToken": null,
  "refreshToken": null,
  "user": null
}
```
> ⚠️ Known issue: response body is null-filled. Warden is created successfully, but the response is a bug — the warden receives no tokens and the caller gets a null body.

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| Email already registered | 409 | `{"error": "Email already registered"}` |
| Password < 8 chars | 400 | Validation error |

---

## hostel-service (port 8092)

### POST /api/hostels

```bash
curl -s -X POST "$BASE_URL/api/hostels" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Girls Hostel Block A",
    "description": "Ground floor wing, AC rooms",
    "type": "GIRLS",
    "status": "ACTIVE",
    "address": "Campus Road, Block A",
    "city": "Pune",
    "state": "Maharashtra",
    "pincode": "411001",
    "totalFloors": 4,
    "adminId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
  }'
```

**Success (201):** Hostel object with generated `id`.

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| Missing required field (`name`) | 400 | Validation error |
| Invalid `type` value | 400 | Validation error |
| Invalid `status` value | 400 | Validation error |

---

### POST /api/hostels/{hostelId}/rooms

```bash
HOSTEL_ID="a1b2c3d4-e5f6-7890-abcd-ef1234567890"

curl -s -X POST "$BASE_URL/api/hostels/$HOSTEL_ID/rooms" \
  -H "Content-Type: application/json" \
  -d '{
    "roomNumber": "101",
    "floor": 1,
    "type": "DOUBLE",
    "capacity": 2,
    "status": "ACTIVE"
  }'
```

**Success (201):** Room object with generated `id`.

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| Floor exceeds hostel `totalFloors` | 400 | `{"error": "Floor 5 exceeds hostel totalFloors 4"}` |
| `capacity` doesn't match `type` (e.g. DOUBLE + capacity=3) | 400 | `{"error": "Capacity 3 does not match room type DOUBLE (expected 2)"}` |
| Room number already exists in hostel | 409 | `{"error": "Room number 101 already exists in this hostel"}` |
| Hostel ID not found | 404 | `{"error": "Hostel not found"}` |

---

### POST /api/hostels/{hostelId}/rooms/{roomId}/beds

```bash
ROOM_ID="b2c3d4e5-f6a7-8901-bcde-f12345678901"

curl -s -X POST "$BASE_URL/api/hostels/$HOSTEL_ID/rooms/$ROOM_ID/beds" \
  -H "Content-Type: application/json" \
  -d '{
    "bedNumber": "A",
    "status": "AVAILABLE"
  }'
```

**Success (201):** Bed object with generated `id` and `status: "AVAILABLE"`.

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| Bed number already exists in room | 409 | Unique constraint violation |
| Room ID not found | 404 | `{"error": "Room not found"}` |

---

### DELETE /api/hostels/{hostelId}/rooms/{roomId}

```bash
curl -s -X DELETE "$BASE_URL/api/hostels/$HOSTEL_ID/rooms/$ROOM_ID"
```

**Success (204):** Empty body.

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| Room still has beds | 400 | `{"error": "Cannot delete room that still has beds"}` |
| Room not found | 404 | `{"error": "Room not found"}` |

---

## allocation-service (port 8093)

### POST /api/allocations

```bash
STUDENT_ID="3fa85f64-5717-4562-b3fc-2c963f66afa6"
BED_ID="c3d4e5f6-a7b8-9012-cdef-123456789012"

curl -s -X POST "$BASE_URL/api/allocations" \
  -H "Content-Type: application/json" \
  -d "{
    \"studentId\": \"$STUDENT_ID\",
    \"bedId\": \"$BED_ID\",
    \"remarks\": \"New intake 2026\"
  }"
```

**Success (201):**
```json
{
  "id": "d4e5f6a7-b8c9-0123-defa-234567890123",
  "studentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "bedId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "hostelId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "allocatedDate": "2026-08-29",
  "checkoutDate": null,
  "status": "ACTIVE",
  "remarks": "New intake 2026"
}
```

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| Student not found in identity-service | 404 | `{"error": "Student not found"}` |
| Student is not ACTIVE | 400 | `{"error": "Student account is not active"}` |
| User does not have STUDENT role | 400 | `{"error": "User is not a student"}` |
| Student already has an ACTIVE allocation | 409 | `{"error": "Student already has an active allocation"}` |
| Bed not found in hostel-service | 404 | `{"error": "Bed not found"}` |
| Bed is not AVAILABLE | 409 | `{"error": "Bed is not available"}` |
| Bed already has an active allocation | 409 | `{"error": "Bed is already allocated"}` |

---

### POST /api/allocations/{id}/checkout

```bash
ALLOCATION_ID="d4e5f6a7-b8c9-0123-defa-234567890123"

curl -s -X POST "$BASE_URL/api/allocations/$ALLOCATION_ID/checkout"
```

**Success (200):** Updated allocation object with `status: "COMPLETED"` and `checkoutDate` set.

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| Allocation not found | 404 | `{"error": "Allocation not found"}` |
| Allocation is not ACTIVE | 400 | `{"error": "Allocation is not active"}` |

---

## complaint-service (port 8094)

### POST /api/complaints

> `?studentId=UUID` — currently the only identity mechanism (see [Authorization Gaps](../auth/AUTHORIZATION_GAPS.md))

```bash
STUDENT_ID="3fa85f64-5717-4562-b3fc-2c963f66afa6"

curl -s -X POST "$BASE_URL/api/complaints?studentId=$STUDENT_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "category": "PLUMBING",
    "title": "Leaking tap in bathroom",
    "description": "The cold water tap in bathroom 2 has been leaking for 3 days.",
    "priority": "HIGH"
  }'
```

**Valid `category` values:** `ELECTRICITY`, `PLUMBING`, `CLEANLINESS`, `FOOD`, `FURNITURE`, `ROOM`, `WIFI`, `SECURITY`, `OTHER`

**Success (201):**
```json
{
  "id": "e5f6a7b8-c9d0-1234-efab-345678901234",
  "studentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "hostelId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "roomId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "bedId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "category": "PLUMBING",
  "title": "Leaking tap in bathroom",
  "description": "The cold water tap in bathroom 2 has been leaking for 3 days.",
  "priority": "HIGH",
  "status": "OPEN",
  "handledBy": null,
  "resolvedAt": null,
  "createdAt": "2026-08-29T09:15:00"
}
```

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| `studentId` not found in identity-service | 404 | `{"error": "Student not found"}` |
| User is not a STUDENT | 400 | `{"error": "User is not a student"}` |
| Student has no active allocation (no hostelId available) | 404 | `{"error": "No active allocation found for student"}` |
| Missing required field (`title`) | 400 | Validation error |

---

### PUT /api/complaints/{id}/status

```bash
COMPLAINT_ID="e5f6a7b8-c9d0-1234-efab-345678901234"
WARDEN_ID="f6a7b8c9-d0e1-2345-fabc-456789012345"

curl -s -X PUT "$BASE_URL/api/complaints/$COMPLAINT_ID/status?wardenId=$WARDEN_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS",
    "resolutionRemarks": "Plumber dispatched"
  }'
```

**Valid status transitions:** `OPEN → IN_PROGRESS`, `OPEN → REJECTED`, `IN_PROGRESS → RESOLVED`, `IN_PROGRESS → REJECTED`

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| `wardenId` not found or not a WARDEN | 404 / 400 | `{"error": "Warden not found"}` |
| Warden's hostelId ≠ complaint's hostelId | 403 | `{"error": "Warden is not assigned to this hostel"}` |
| Invalid status transition (e.g. RESOLVED → IN_PROGRESS) | 400 | `{"error": "Invalid status transition"}` |
| Complaint already handled by another warden | 409 | `{"error": "Complaint already accepted by another warden"}` |

---

## leave-service (port 8095)

### POST /api/leaves

```bash
STUDENT_ID="3fa85f64-5717-4562-b3fc-2c963f66afa6"

curl -s -X POST "$BASE_URL/api/leaves?studentId=$STUDENT_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2026-09-05",
    "endDate": "2026-09-10",
    "reason": "Family function — sister'\''s wedding"
  }'
```

**Success (201):**
```json
{
  "id": "f7a8b9c0-d1e2-3456-abcd-567890123456",
  "studentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "hostelId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "startDate": "2026-09-05",
  "endDate": "2026-09-10",
  "reason": "Family function — sister's wedding",
  "status": "PENDING",
  "handledBy": null,
  "createdAt": "2026-08-29T10:00:00"
}
```

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| `startDate` in the past | 400 | `{"error": "Start date cannot be in the past"}` |
| `endDate` before `startDate` | 400 | `{"error": "End date must be after start date"}` |
| Overlapping PENDING or IN_REVIEW leave exists | 409 | `{"error": "Overlapping leave request already exists"}` |
| Student has no active allocation | 500 | NPE — known bug; allocation returns null without null check |
| User is not a STUDENT | 400 | `{"error": "User is not a student"}` |

---

### PUT /api/leaves/{id}/accept

```bash
LEAVE_ID="f7a8b9c0-d1e2-3456-abcd-567890123456"
WARDEN_ID="f6a7b8c9-d0e1-2345-fabc-456789012345"

curl -s -X PUT "$BASE_URL/api/leaves/$LEAVE_ID/accept?wardenId=$WARDEN_ID"
```

**Success (200):** Updated leave with `status: "IN_REVIEW"` and `handledBy` set to warden UUID.

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| `wardenId` not found | 404 | `{"error": "Warden not found"}` |
| Warden has no `hostelId` | 400 | `{"error": "Warden is not assigned to a hostel"}` |
| Warden's hostelId ≠ leave's hostelId | 403 | `{"error": "Warden is not assigned to this hostel"}` |
| Leave is not in PENDING status | 400 | `{"error": "Invalid status transition"}` |
| Concurrent accept — optimistic lock conflict | 409 | `{"error": "Conflict: leave was already accepted by another warden"}` |

---

### PUT /api/leaves/{id}/status

```bash
curl -s -X PUT "$BASE_URL/api/leaves/$LEAVE_ID/status?wardenId=$WARDEN_ID" \
  -H "Content-Type: application/json" \
  -d '{"status": "APPROVED", "remarks": "Approved for family function"}'
```

**Valid transitions:** `IN_REVIEW → APPROVED`, `IN_REVIEW → REJECTED`

**Failure cases:**

| Scenario | Status | Response |
|----------|--------|----------|
| Leave not IN_REVIEW | 400 | `{"error": "Invalid status transition"}` |
| Warden hostel mismatch | 403 | `{"error": "Warden is not assigned to this hostel"}` |
| Invalid `status` value | 400 | Validation error |

---

## Postman Collection

Import [`postman/HostelHub.postman_collection.json`](postman/HostelHub.postman_collection.json) into Postman.

Set environment variables:
- `baseUrl` = `http://localhost:8080` (via gateway)
- `gatewayUrl` = `http://localhost:8080`
- `studentId` = a valid student UUID after registration
- `wardenId` = a valid warden UUID after creation
- `token` = JWT access token from login

---

## See Also

- [ROLES_AND_PERMISSIONS.md](../auth/ROLES_AND_PERMISSIONS.md) — who should be allowed to call what
- [AUTHORIZATION_GAPS.md](../auth/AUTHORIZATION_GAPS.md) — why current auth is insufficient
- [Per-service docs](../services/) — entity fields and business rule detail
