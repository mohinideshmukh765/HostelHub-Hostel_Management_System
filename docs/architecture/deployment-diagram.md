# Deployment Diagram

> Mermaid diagram showing the required startup order and dependency relationships.
> Source: Stage 1 source-code analysis of `spring.config.import` and `eureka.client.service-url.defaultZone` settings found in each service.

---

## Startup Order & Dependency Graph

```mermaid
graph TD
    subgraph "Wave 1 — Start First (no dependencies)"
        CFG["① config-server\n:8888\nno dependencies\nnative profile from classpath:/config"]
        EUR["② service-registry / Eureka\n:8761\nno config-server dependency\nregister-with-eureka=false"]
    end

    subgraph "Wave 2 — Business Services (start after Wave 1)"
        IS["③ identity-service\n:8081\n⚠️ does NOT use config-server\nall config local\nneeds Eureka only"]
        HS["③ hostel-service\n:8092\nneeds config-server + Eureka"]
        AS["③ allocation-service\n:8093\nneeds config-server + Eureka"]
        CS["③ complaint-service\n:8094\nneeds config-server + Eureka"]
        LS["③ leave-service\n:8095\nneeds config-server + Eureka"]
    end

    subgraph "Wave 3 — Start Last"
        GW["④ api-gateway\n:8080\ndoes NOT use config-server\nneeds Eureka to resolve lb:// routes"]
    end

    CFG -->|"provides config to"| HS
    CFG -->|"provides config to"| AS
    CFG -->|"provides config to"| CS
    CFG -->|"provides config to"| LS

    EUR -->|"discovery for"| IS
    EUR -->|"discovery for"| HS
    EUR -->|"discovery for"| AS
    EUR -->|"discovery for"| CS
    EUR -->|"discovery for"| LS
    EUR -->|"discovery for"| GW

    style CFG fill:#e8f5e9,stroke:#388e3c,color:#000
    style EUR fill:#e8f5e9,stroke:#388e3c,color:#000
    style IS fill:#fff9c4,stroke:#f9a825,color:#000
    style GW fill:#e3f2fd,stroke:#1565c0,color:#000
```

---

## Startup Order — Quick Reference Table

| Order | Service | Port | Config Source | Eureka |
|-------|---------|------|---------------|--------|
| ① | config-server | 8888 | Local `application.properties` | ❌ not registered |
| ② | service-registry | 8761 | Local `application.properties` | ❌ self (server) |
| ③ | identity-service | 8081 | **Local only** — no `spring.config.import` | ✅ |
| ③ | hostel-service | 8092 | config-server → `hostel-service.properties` | ✅ |
| ③ | allocation-service | 8093 | config-server → `allocation-service.properties` | ✅ |
| ③ | complaint-service | 8094 | config-server → `complaint-service.properties` | ✅ |
| ③ | leave-service | 8095 | config-server → `leave-service.properties` | ✅ |
| ④ | api-gateway | 8080 | Local `application.properties` | ✅ |

> Wave 3 services (HS, AS, CS, LS) use `optional:configserver:http://localhost:8888` — the `optional:` prefix means they **start without error** if config-server is down, but may have missing DB/Eureka config.

---

## Key Observations

| Observation | Detail |
|-------------|--------|
| `identity-service` skips config-server | It does NOT set `spring.config.import`. All config (port, DB URL, JWT secret, Eureka) is in local `application.properties`. It is the only business service that does this. |
| `api-gateway` skips config-server | All route definitions are in local `application.properties`. |
| `service-registry` skips config-server | Pure Eureka server; no dynamic config needed. |
| `optional:` import prefix | Business services start without error if config-server is unavailable. They may silently miss DB config and fail at first DB operation. No `fail-fast=true` or retry config found. |
| No health-check wait mechanism | Nothing in the codebase delays service startup until dependencies are confirmed healthy. Manual ordering is required when starting locally. |
| Wave 3 services can start in parallel | hostel, allocation, complaint, and leave services have no dependency on each other at startup — only on config-server and Eureka. |

---

## See Also

- [service-diagram.md](service-diagram.md) — service-to-service call graph
- [config-server.md](../services/config-server.md) — what config-server centralises
- [SYSTEM_OVERVIEW.md](SYSTEM_OVERVIEW.md) — full cross-service analysis
