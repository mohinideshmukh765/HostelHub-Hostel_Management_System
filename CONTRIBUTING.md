# Contributing to HostelHub

Thank you for your interest in contributing! This guide explains how to get a full local development environment running and how to submit changes.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Repository Structure](#repository-structure)
- [Local Setup](#local-setup)
  - [1. Clone the repository](#1-clone-the-repository)
  - [2. Set up PostgreSQL databases](#2-set-up-postgresql-databases)
  - [3. Configure environment variables](#3-configure-environment-variables)
  - [4. Start services in order](#4-start-services-in-order)
- [Code Style](#code-style)
- [Pull Request Guidelines](#pull-request-guidelines)
- [Roadmap / Open Issues](#roadmap--open-issues)

---

## Prerequisites

| Tool | Minimum version | Notes |
|------|----------------|-------|
| Java (JDK) | 17 | LTS release. [Adoptium Temurin 17](https://adoptium.net/) recommended. |
| Maven | 3.9+ | Or use the bundled `mvnw` / `mvnw.cmd` wrapper in each service folder. |
| PostgreSQL | 14+ | Must be running locally on port `5432`. |
| Git | 2.x | — |
| IntelliJ IDEA | Any recent | Community Edition works. Mark each `src` directory as "Sources Root". |

---

## Repository Structure

```
HostelHub/
├── config-server/        # Spring Cloud Config Server (Wave 1)
├── service-registry/     # Netflix Eureka Server (Wave 1)
├── identity-service/     # JWT auth, users, roles (Wave 2)
├── hostel-service/       # Hostels, rooms, beds (Wave 2)
├── allocation-service/   # Check-in / check-out (Wave 2)
├── complaint-service/    # Complaint lifecycle (Wave 2)
├── leave-service/        # Leave lifecycle (Wave 2)
├── api-gateway/          # Spring Cloud Gateway MVC (Wave 3)
├── test-service/         # ⚠️ Scaffolding — will be removed
└── docs/                 # All documentation and diagrams
    ├── architecture/     # System overview, service diagram, deployment diagram
    ├── auth/             # Authentication flow, authorization gaps, role matrix
    ├── api/              # API testing guide, Postman collection
    ├── diagrams/         # ER diagram, data-flow, JWT sequence diagrams
    └── services/         # Per-service technical deep dives
```

Each microservice is an independent Spring Boot application with its own Maven build.

---

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/HostelHub.git
cd HostelHub
```

### 2. Set up PostgreSQL databases

Run the following SQL in your PostgreSQL client (psql, pgAdmin, DBeaver, etc.):

```sql
-- Create all databases required by HostelHub services
CREATE DATABASE identity_db;
CREATE DATABASE hostel_db;
CREATE DATABASE allocation_db;
CREATE DATABASE complaint_db;
CREATE DATABASE hostelhub_leave_db;
```

> **Default credentials expected by config-server:**
> - Host: `localhost:5432`
> - Username: `postgres`
> - Password: `mohini` (all services except `leave-service` which uses `postgres` — see Known Issues in README)
>
> Override these by creating a local `application-local.properties` in the relevant service's `src/main/resources/` directory (this file is in `.gitignore` and will not be committed).

### 3. Configure environment variables

The JWT secret is currently hardcoded in `identity-service/src/main/resources/application.properties`.
Before running in any environment beyond local dev, move it to an environment variable:

```properties
# identity-service/src/main/resources/application-local.properties
# (kept out of git by .gitignore)
jwt.secret=<your-256-bit-secret>
spring.datasource.password=<your-db-password>
```

See [`SETUP.md`](SETUP.md) for the full environment variable table.

### 4. Start services in order

Services must start in dependency order because downstream services need config-server and Eureka before they can register.

**Wave 1 — Infrastructure (start first, no dependencies):**
```bash
# Terminal 1
cd config-server && ./mvnw spring-boot:run

# Terminal 2
cd service-registry && ./mvnw spring-boot:run
# Wait until http://localhost:8761 loads the Eureka dashboard
```

**Wave 2 — Business Services (start in parallel after Wave 1):**
```bash
cd identity-service  && ./mvnw spring-boot:run
cd hostel-service    && ./mvnw spring-boot:run
cd allocation-service && ./mvnw spring-boot:run
cd complaint-service && ./mvnw spring-boot:run
cd leave-service     && ./mvnw spring-boot:run
# Wait until all 5 services appear registered in Eureka
```

**Wave 3 — API Gateway (start last):**
```bash
cd api-gateway && ./mvnw spring-boot:run
```

**Verify everything is up:**

| Check | URL |
|-------|-----|
| Eureka dashboard | http://localhost:8761 |
| Gateway health | http://localhost:8080/actuator/health |
| Identity health | http://localhost:8081/actuator/health |
| Register a student | `POST http://localhost:8080/api/auth/register` |

---

## Code Style

- **Java 17** — use `var` where it aids readability; avoid where it obscures type.
- **Lombok** is used for boilerplate (`@Data`, `@Builder`, `@RequiredArgsConstructor`). Keep constructor injection (no field injection with `@Autowired`).
- **Package structure** per service: `controller`, `service`, `repository`, `entity`, `dto`, `config`, `security` (where applicable).
- **DTOs** separate from entities — no `@Entity` classes used directly in controller request/response bodies.
- **No hardcoded URLs** — all inter-service calls use Feign clients resolved via Eureka (`@FeignClient(name = "service-name")`).
- **Exception handling** — use `@ControllerAdvice` / `@ExceptionHandler`; return structured error responses.

---

## Pull Request Guidelines

1. **Branch from `main`** using a descriptive name: `feature/gateway-jwt-filter`, `fix/leave-null-pointer`, `docs/er-diagram`.
2. **One concern per PR** — do not mix feature, fix, and docs changes.
3. **Update docs** — if you add/change an endpoint, update the relevant file in `docs/services/` and `docs/api/API_TESTING.md`.
4. **Update CHANGELOG.md** — add an entry under `[Unreleased]`.
5. **Describe the PR** — explain _why_, not just _what_.

---

## Roadmap / Open Issues

The biggest open task is **Stage 3: Authorization Hardening**. See [`CHANGELOG.md`](CHANGELOG.md) for the full to-do list. Key items:

- JWT validation filter at `api-gateway`
- `@PreAuthorize` guards in all business services
- Replace `?studentId=UUID` param with JWT-extracted principal
- Move `jwt.secret` to an environment variable
- Remove `test-service`

See [Known Issues / Roadmap](README.md#known-issues--roadmap) in the root README for the full picture.
