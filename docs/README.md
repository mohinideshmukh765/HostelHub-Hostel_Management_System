# HostelHub — Documentation Index

> Complete technical documentation for the HostelHub microservice backend.
> Start here to navigate to any doc.

---

## Reading Order for New Contributors

If you're new to the project, read in this order:

1. [Root README](../README.md) — Project overview, highlights, architecture diagram, quick start
2. [System Overview](architecture/SYSTEM_OVERVIEW.md) — Detailed cross-service analysis from source
3. [Service Call Graph](architecture/service-diagram.md) — Who calls whom (Mermaid diagram)
4. [Deployment Order](architecture/deployment-diagram.md) — Startup dependency graph
5. [Authentication Flow](auth/AUTHENTICATION.md) — JWT lifecycle
6. [Authorization Gaps](auth/AUTHORIZATION_GAPS.md) — Current security state and roadmap
7. Per-service docs (see below) — Deep dives into each microservice

---

## Architecture

| Document | Description |
|----------|-------------|
| [SYSTEM_OVERVIEW.md](architecture/SYSTEM_OVERVIEW.md) | Cross-service architecture, service map, config analysis, startup order, domain summary, auth findings |
| [service-diagram.md](architecture/service-diagram.md) | Mermaid diagram: all services, call directions, gateway routes |
| [deployment-diagram.md](architecture/deployment-diagram.md) | Mermaid diagram: startup dependency graph, config sources per service |

---

## Diagrams

| Diagram | Description |
|---------|-------------|
| [Entity Relationship Diagram](diagrams/README.md#entity-relationship-diagram) | All entities across all 5 PostgreSQL databases with cross-service UUID reference annotations |
| [Full Request Data-Flow](diagrams/README.md#full-request-data-flow) | End-to-end flowchart: Register → Allocate → Leave → Complaint |
| [JWT Authentication Flow](diagrams/README.md#jwt-authentication-flow) | Sequence diagram: token issuance, refresh, and current auth gaps |

---

## Per-Service Documentation

| Service | Port | Document |
|---------|------|----------|
| `identity-service` | 8081 | [identity-service.md](services/identity-service.md) |
| `hostel-service` | 8092 | [hostel-service.md](services/hostel-service.md) |
| `allocation-service` | 8093 | [allocation-service.md](services/allocation-service.md) |
| `complaint-service` | 8094 | [complaint-service.md](services/complaint-service.md) |
| `leave-service` | 8095 | [leave-service.md](services/leave-service.md) |
| `api-gateway` | 8080 | [api-gateway.md](services/api-gateway.md) |
| `config-server` | 8888 | [config-server.md](services/config-server.md) |
| `service-registry` | 8761 | [service-registry.md](services/service-registry.md) |

Each service doc covers:
- Entities / data model
- Endpoints (HTTP method, path, request/response summary)
- Business rules enforced
- Feign dependencies (outbound calls)
- Current authorization state

---

## Authentication & Authorization

| Document | Description |
|----------|-------------|
| [AUTHENTICATION.md](auth/AUTHENTICATION.md) | JWT structure, token lifecycle, security config in identity-service |
| [AUTHORIZATION_GAPS.md](auth/AUTHORIZATION_GAPS.md) | Full gap inventory: 7 findings, risk levels, planned fixes (Stage 3) |
| [ROLES_AND_PERMISSIONS.md](auth/ROLES_AND_PERMISSIONS.md) | Role × Action × Service permission matrix (current state vs. target state) |

---

## API Reference

| Document | Description |
|----------|-------------|
| [API_TESTING.md](api/API_TESTING.md) | Full endpoint inventory across all 8 services with `curl` examples and failure cases |
| [Postman Collection](api/postman/) | Import-ready collection with environment variable support |

---

## Other Top-Level Files

| File | Description |
|------|-------------|
| [README.md](../README.md) | Project overview, highlights, architecture, quick start, tech stack |
| [SETUP.md](../SETUP.md) | Detailed DB setup SQL, credentials, env var templates, troubleshooting |
| [CONTRIBUTING.md](../CONTRIBUTING.md) | Dev environment setup, code style, PR guidelines |
| [CHANGELOG.md](../CHANGELOG.md) | Version history and Stage 3 planned work |
