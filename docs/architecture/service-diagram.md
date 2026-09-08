# Service Call Graph

> Mermaid diagram showing all services and their verified call directions.
> Source: Stage 1 source-code analysis — every arrow corresponds to a Feign client or gateway route found in code.

---

## Service-to-Service Call Graph

```mermaid
graph TD
    Client(["👤 External Client"])

    subgraph "Infrastructure"
        CFG["config-server\n:8888"]
        EUR["service-registry / Eureka\n:8761"]
    end

    subgraph "Edge"
        GW["api-gateway\n:8080\npath-based routing only\nno auth filtering"]
    end

    subgraph "Business Services"
        IS["identity-service\n:8081\nJWT · users · roles\nSpring Security ✅"]
        HS["hostel-service\n:8092\nhostels · rooms · beds\nNo auth ❌"]
        AS["allocation-service\n:8093\ncheck-in / check-out\nNo auth ❌"]
        CS["complaint-service\n:8094\ncomplaint lifecycle\nNo auth ❌"]
        LS["leave-service\n:8095\nleave lifecycle\nNo auth ❌"]
        TS["test-service\n:8090\n⚠️ scaffolding only"]
    end

    Client -->|"HTTP"| GW

    GW -->|"/api/auth/**"| IS
    GW -->|"/api/hostels/**"| HS
    GW -->|"/api/allocations/**"| AS
    GW -->|"/api/complaints/**"| CS
    GW -->|"/api/leaves/**"| LS
    GW -->|"/api/test/**"| TS

    AS -->|"Feign\nGET /api/internal/users/{id}"| IS
    AS -->|"Feign\nGET /api/internal/beds/{id}"| HS
    AS -->|"Feign\nPUT /api/internal/beds/{id}/occupy"| HS
    AS -->|"Feign\nPUT /api/internal/beds/{id}/release"| HS

    CS -->|"Feign\nGET /api/internal/users/{userId}"| IS
    CS -->|"Feign\nGET /api/internal/allocations/student/{id}"| AS

    LS -->|"Feign\nGET /api/internal/users/{id}"| IS
    LS -->|"Feign\nGET /api/internal/allocations/student/{id}"| AS

    IS & HS & AS & CS & LS & TS & GW -->|"register / discover"| EUR
    HS & AS & CS & LS -->|"fetch config on startup"| CFG

    style CFG fill:#e8f5e9,stroke:#388e3c,color:#000
    style EUR fill:#e8f5e9,stroke:#388e3c,color:#000
    style TS fill:#ffcccc,stroke:#cc0000,color:#000
    style GW fill:#e3f2fd,stroke:#1565c0,color:#000
    style IS fill:#fff9c4,stroke:#f9a825,color:#000
```

---

## Notes

| Symbol | Meaning |
|--------|---------|
| 🟡 (yellow) | identity-service — the only service with Spring Security |
| 🔴 (red) | test-service — scaffolding; no business logic; should be removed |
| 🔵 (blue) | api-gateway — routes traffic; no auth filtering today |
| 🟢 (green) | Infrastructure services (config-server, Eureka) |

### Key architectural properties (verified from source)

- **No hardcoded IP/port URLs** — all Feign clients resolve by Eureka service name (`@FeignClient(name = "...")`)
- **Gateway does no auth filtering** — it is a pure path-based proxy; tokens (or lack of tokens) pass through unchanged
- **`/api/internal/**` bypasses the gateway** — these endpoints live on direct service ports with no auth whatsoever
- **`hostel-service` has `@EnableFeignClients`** declared but no Feign client interfaces are defined in its source tree (no outbound Feign calls)
- **All four business services (hostel, allocation, complaint, leave) have zero Spring Security** — see [AUTHORIZATION_GAPS.md](../auth/AUTHORIZATION_GAPS.md)

---

## See Also

- [deployment-diagram.md](deployment-diagram.md) — startup order and dependency graph
- [SYSTEM_OVERVIEW.md](SYSTEM_OVERVIEW.md) — full cross-service analysis
- [AUTHORIZATION_GAPS.md](../auth/AUTHORIZATION_GAPS.md) — security gap inventory
