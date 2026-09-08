# Service Registry (Eureka)

> Port: **8761** · DB: none · Config: local only

---

## Responsibility

Netflix Eureka server. Provides service discovery for all other services.
Services register themselves on startup and query Eureka to resolve each other
by name (e.g. `lb://allocation-service`).

---

## Configuration

Source: `service-registry/src/main/resources/application.properties`

```properties
server.port=8761
spring.application.name=service-registry
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

The registry does not register itself as a client — it is a pure server.

---

## Dashboard

Eureka's built-in web dashboard is available at: `http://localhost:8761`

No custom endpoints or code beyond the standard `@EnableEurekaServer` annotation
(on `ServiceRegistryApplication.java`).

---

## Entities / Data Model

N/A — infrastructure service. No database.

---

## Registered Services (at runtime)

| Service | Registers? |
|---------|-----------|
| api-gateway | ✅ |
| identity-service | ✅ |
| hostel-service | ✅ |
| allocation-service | ✅ |
| complaint-service | ✅ |
| leave-service | ✅ |
| test-service | ✅ |
| config-server | ❌ |
| service-registry | ❌ (self) |

---

## Service Dependencies

| Direction | Service | Reason |
|-----------|---------|--------|
| Registered by | All business services + gateway + test-service | Enable client-side load balancing and discovery |
