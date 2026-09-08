# Config Server

> Port: **8888** · DB: none · Profile: `native`

---

## Responsibility

Centralised configuration provider for allocation-service, hostel-service,
complaint-service, and leave-service. Serves properties from the local classpath
(`classpath:/config`), not from a Git repository.

---

## Configuration Sources

Source: `config-server/src/main/resources/application.properties`

```properties
spring.profiles.active=native
spring.cloud.config.server.native.search-locations=classpath:/config
```

### Files served

| File | Consumed by | Key properties |
|------|------------|----------------|
| `config/application.properties` | All services (global fallback) | `hostelhub.message`, actuator exposure |
| `config/allocation-service.properties` | allocation-service | port 8093, `allocation_db` DB, JPA, Eureka |
| `config/hostel-service.properties` | hostel-service | port 8092, `hostel_db` DB, JPA, Eureka |
| `config/complaint-service.properties` | complaint-service | port 8094, `complaint_db` DB, JPA, Eureka instance |
| `config/leave-service.properties` | leave-service | port 8095, `hostelhub_leave_db` DB, JPA, Eureka |

### Services NOT using config-server

| Service | Reason |
|---------|--------|
| identity-service | Uses local `application.properties` only — does not set `spring.config.import` |
| api-gateway | Uses local `application.properties` only |
| service-registry | Uses local `application.properties` only |

---

## Inconsistencies Found

| Issue | Detail |
|-------|--------|
| `leave-service.properties` uses `spring.datasource.password=postgres` | All other services use `password=mohini` |
| `complaint-service` duplicates `eureka.client.service-url.defaultZone` locally | Same value as in config-server; redundant |
| `hostel-service.properties` repeats `spring.application.name=hostel-service` | Already set in local `application.properties` |

---

## Service Dependencies

| Direction | Service | Reason |
|-----------|---------|--------|
| Called by | allocation-service | Fetch config on startup |
| Called by | hostel-service | Fetch config on startup |
| Called by | complaint-service | Fetch config on startup |
| Called by | leave-service | Fetch config on startup |

> All clients use `optional:configserver:http://localhost:8888` — if config-server
> is unavailable, services start without error but may have missing/default values.
