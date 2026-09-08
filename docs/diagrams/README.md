# Diagrams

This folder holds standalone diagram documents using [Mermaid](https://mermaid.js.org/) syntax, rendered automatically by GitHub.

| Diagram | What it shows |
|---------|---------------|
| [Entity Relationship Diagram](#entity-relationship-diagram) | All entities across all 5 databases and their cross-service UUID references |
| [Full Request Data-Flow](#full-request-data-flow) | End-to-end flow: Register → Allocate → Leave → Complaint |
| [JWT Authentication Flow](#jwt-authentication-flow) | Token issuance, refresh, and validation lifecycle |

> **Note:** Detailed service-call graphs and deployment order are in [`architecture/`](../architecture/).

---

## Entity Relationship Diagram

> Cross-service UUID references are marked with `||--o{` (one-to-many) or annotated in notes.
> Because each service owns a separate PostgreSQL database, these are **logical** relationships — no database-level foreign keys cross service boundaries.

```mermaid
erDiagram
    %% ─── identity_db ───────────────────────────────────────
    USER {
        UUID   id           PK
        string firstName
        string lastName
        string email        UK
        string passwordHash
        UUID   hostelId     "logical ref → hostel_db.hostel.id (not enforced)"
        enum   role         "STUDENT | WARDEN | ADMIN | SYSTEM_ADMIN"
    }

    ROLE {
        UUID   id   PK
        string name "STUDENT | WARDEN | ADMIN | SYSTEM_ADMIN"
    }

    REFRESH_TOKEN {
        UUID      id        PK
        string    token     UK
        boolean   revoked
        timestamp expiresAt
        UUID      userId    FK
    }

    USER ||--o{ REFRESH_TOKEN : "has"

    %% ─── hostel_db ──────────────────────────────────────────
    HOSTEL {
        UUID   id       PK
        string name
        string address
        UUID   adminId  "logical ref → identity_db.user.id"
    }

    ROOM {
        UUID    id       PK
        string  roomNo
        int     capacity
        UUID    hostelId FK
    }

    BED {
        UUID    id       PK
        string  bedNo
        enum    status   "AVAILABLE | OCCUPIED"
        UUID    roomId   FK
        UUID    hostelId "denormalized for lookup speed"
    }

    HOSTEL_WARDEN {
        UUID id       PK
        UUID hostelId FK
        UUID wardenId "logical ref → identity_db.user.id"
    }

    HOSTEL ||--o{ ROOM        : "has"
    ROOM   ||--o{ BED         : "has"
    HOSTEL ||--o{ HOSTEL_WARDEN : "assigned"

    %% ─── allocation_db ──────────────────────────────────────
    ALLOCATION {
        UUID      id          PK
        UUID      studentId   "logical ref → identity_db.user.id ✅ Feign-verified"
        UUID      bedId       "logical ref → hostel_db.bed.id   ✅ Feign-verified"
        UUID      hostelId    "logical ref → hostel_db.hostel.id ❌ copied, not verified"
        UUID      roomId      "logical ref → hostel_db.room.id  ❌ copied, not verified"
        date      checkInDate
        date      checkOutDate
        enum      status      "ACTIVE | CHECKED_OUT"
        int       version     "@Version optimistic lock"
    }

    %% ─── complaint_db ───────────────────────────────────────
    COMPLAINT {
        UUID      id               PK
        UUID      studentId        "logical ref → identity_db.user.id ✅ Feign-verified"
        UUID      hostelId         "logical ref → hostel_db.hostel.id ❌ from allocation"
        UUID      roomId           "logical ref → hostel_db.room.id  ❌ convention"
        UUID      bedId            "logical ref → hostel_db.bed.id   ❌ convention"
        string    title
        string    description
        enum      status           "PENDING | IN_PROGRESS | RESOLVED | CLOSED"
        UUID      assignedWardenId "logical ref → identity_db.user.id"
        UUID      closedBy         "not yet populated"
        string    rejectionReason  "not yet populated"
        boolean   escalated        "not yet populated"
        timestamp createdAt
        timestamp updatedAt
    }

    %% ─── hostelhub_leave_db ─────────────────────────────────
    LEAVE_REQUEST {
        UUID      id          PK
        UUID      studentId   "logical ref → identity_db.user.id ✅ Feign-verified"
        UUID      hostelId    "logical ref → hostel_db.hostel.id ❌ from allocation"
        UUID      roomId      "logical ref → hostel_db.room.id  ❌ from allocation"
        UUID      bedId       "logical ref → hostel_db.bed.id   ❌ from allocation"
        date      startDate
        date      endDate
        string    reason
        enum      status      "PENDING | APPROVED | REJECTED | CANCELLED"
        UUID      reviewedBy  "logical ref → identity_db.user.id"
        string    rejectionReason "not yet populated"
        boolean   escalated       "not yet populated"
        int       version         "@Version optimistic lock"
        timestamp createdAt
        timestamp updatedAt
    }
```

### Cross-Service UUID Reference Legend

| Symbol | Meaning |
|--------|---------|
| ✅ Feign-verified | At write time, a Feign call to the owning service confirms the ID exists |
| ❌ convention only | UUID is stored as a plain column; no verification at write time |

---

## Full Request Data-Flow

> Traces the most common student journey from registration to submitting a complaint.

```mermaid
flowchart TD
    A([👤 Student / Admin / Warden]) --> B

    subgraph "Step 1 — Identity"
        B["POST /api/auth/register\n(identity-service)"]
        B --> C["POST /api/auth/login\n→ returns accessToken + refreshToken"]
    end

    subgraph "Step 2 — Hostel Setup (Admin)"
        D["POST /api/hostels\n(hostel-service)"]
        D --> E["POST /api/hostels/{id}/rooms\nCreate room"]
        E --> F["POST /api/rooms/{id}/beds\nCreate bed"]
    end

    subgraph "Step 3 — Allocation (Admin / Warden)"
        G["POST /api/allocations\n(allocation-service)\nbody: studentId + bedId"]
        G --> G1["Feign → identity-service\nvalidate student exists"]
        G --> G2["Feign → hostel-service\nvalidate bed exists + mark OCCUPIED"]
        G1 & G2 --> G3["Save allocation\nstatus = ACTIVE"]
    end

    subgraph "Step 4 — Leave Request (Student)"
        H["POST /api/leaves\n(leave-service)\nbody: studentId + dates + reason"]
        H --> H1["Feign → identity-service\nvalidate student"]
        H --> H2["Feign → allocation-service\nfetch active allocation → get hostelId/roomId/bedId"]
        H1 & H2 --> H3["Save leave request\nstatus = PENDING"]
        H3 --> H4["PATCH /api/leaves/{id}/approve\n(Warden action)\nstatus = APPROVED"]
    end

    subgraph "Step 5 — Complaint (Student)"
        I["POST /api/complaints\n(complaint-service)\nbody: studentId + title + description"]
        I --> I1["Feign → identity-service\nvalidate student"]
        I --> I2["Feign → allocation-service\nfetch active allocation → hostelId/roomId/bedId"]
        I1 & I2 --> I3["Save complaint\nstatus = PENDING"]
        I3 --> I4["PATCH /api/complaints/{id}/assign\n(Warden assigns themselves)"]
        I4 --> I5["PATCH /api/complaints/{id}/resolve\nstatus = RESOLVED"]
    end

    subgraph "Step 6 — Check-out"
        J["PUT /api/allocations/{id}/checkout\n(allocation-service)"]
        J --> J1["Feign → hostel-service\nmark bed AVAILABLE"]
        J1 --> J2["Allocation status = CHECKED_OUT"]
    end

    C --> D
    D -.->|"Admin creates hostel"| G
    C -.->|"Student token"| G
    G3 --> H
    G3 --> I
    H4 & I5 --> J

    style A fill:#e3f2fd,stroke:#1565c0,color:#000
    style G1 fill:#fff9c4,stroke:#f9a825,color:#000
    style G2 fill:#fff9c4,stroke:#f9a825,color:#000
    style H1 fill:#fff9c4,stroke:#f9a825,color:#000
    style H2 fill:#fff9c4,stroke:#f9a825,color:#000
    style I1 fill:#fff9c4,stroke:#f9a825,color:#000
    style I2 fill:#fff9c4,stroke:#f9a825,color:#000
    style J1 fill:#fff9c4,stroke:#f9a825,color:#000
```

> 🟡 Yellow nodes = Feign calls to another service.

---

## JWT Authentication Flow

```mermaid
sequenceDiagram
    participant C  as Client
    participant GW as api-gateway :8080
    participant IS as identity-service :8081
    participant BS as business-service :8092-8095

    rect rgb(227, 242, 253)
        Note over C,IS: Registration & Login
        C->>GW: POST /api/auth/register
        GW->>IS: forward (no auth check)
        IS-->>C: 201 Created

        C->>GW: POST /api/auth/login
        GW->>IS: forward
        IS-->>C: { accessToken (15 min), refreshToken (7 days) }
    end

    rect rgb(255, 249, 196)
        Note over C,BS: Authenticated Request (current state — auth gap)
        C->>GW: GET /api/hostels (Bearer token)
        Note right of GW: ⚠️ Gateway does NOT validate JWT today
        GW->>BS: forward request as-is
        Note right of BS: ⚠️ Business service has no Spring Security
        BS-->>C: 200 OK (no auth enforced)
    end

    rect rgb(232, 245, 233)
        Note over C,IS: Token Refresh
        C->>GW: POST /api/auth/refresh (refreshToken)
        GW->>IS: forward
        IS-->>C: { new accessToken (15 min) }
    end

    rect rgb(255, 204, 204)
        Note over C,IS: Token Expiry
        C->>GW: Any request (expired token)
        GW->>IS: forward (identity endpoints)
        IS-->>C: 401 Unauthorized (filter rejects)
        Note right of C: ⚠️ Non-identity endpoints bypass this
    end
```

> The red section illustrates the current authorization gap documented in [`docs/auth/AUTHORIZATION_GAPS.md`](../auth/AUTHORIZATION_GAPS.md). Stage 3 (in progress) will add JWT validation at the gateway and `@PreAuthorize` guards in all business services.

---

## See Also

- [service-diagram.md](../architecture/service-diagram.md) — full service-to-service call graph
- [deployment-diagram.md](../architecture/deployment-diagram.md) — startup order
- [SYSTEM_OVERVIEW.md](../architecture/SYSTEM_OVERVIEW.md) — complete source analysis
- [AUTHORIZATION_GAPS.md](../auth/AUTHORIZATION_GAPS.md) — security gap inventory
