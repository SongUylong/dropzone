# Dropzone Platform - Container Image Versioning & Environment-Neutral Strategy

## 23. Externalized Container Configuration

All Dropzone microservice Docker images are completely environment-neutral.

```text
Same Image: dropzone/order-service:1.4.0

DEV        → SPRING_PROFILES_ACTIVE=development
STAGING    → SPRING_PROFILES_ACTIVE=staging
PRODUCTION → SPRING_PROFILES_ACTIVE=production
```

- Images contain **zero hardcoded environment settings, secrets, or host URLs**.
- Environment behavior is dynamically configured via Spring Cloud Config Server and environment variables (`SPRING_PROFILES_ACTIVE`, `POSTGRES_HOST`, `REDIS_HOST`, etc.).
- Image rebuilds are never required when promoting software across environments.

---

## 24. Semantic Versioning & Rollback Strategy

Microservice Docker container images strictly follow Semantic Versioning (`MAJOR.MINOR.PATCH`):

```text
dropzone/order-service:1.4.0
dropzone/order-service:1.4.1
dropzone/order-service:1.5.0
```

### Rollback Process

In the event of a deployment issue, services are instantly rolled back to an explicit previous semantic version tag:

```bash
# Rollback order-service deployment to version 1.4.1
docker service update --image dropzone/order-service:1.4.1 order-service

# Or via Docker Compose tag update in docker-compose.yml:
# image: dropzone/order-service:1.4.1
docker compose up -d order-service
```
