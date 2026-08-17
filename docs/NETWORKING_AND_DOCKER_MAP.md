# Dropzone Platform - Networking Readiness & Docker Compose Port Map

## 21. Networking Readiness & Port Map

All microservices and infrastructure components support hostnames/IP overrides via environment variables. Microservices communicate dynamically using DNS container names and service registry hostnames (`eureka.client.service-url.defaultZone`).

### Port Mapping Table

| Component / Microservice | Public / External Port | Internal Container / Service Port | Protocol | Purpose / Description |
|--------------------------|-----------------------|----------------------------------|----------|-----------------------|
| **API Gateway** | `8080` | `8080` | HTTP | Entry point & reverse proxy |
| **Eureka Discovery** | `8761` | `8761` | HTTP | Dynamic service registration & discovery |
| **Config Server** | `8888` | `8888` | HTTP | Centralized Git configuration server |
| **User Service** | `8081` | `8081` | HTTP | User profiles & authentication RBAC |
| **Inventory Service** | `8082` | `8082` | HTTP | Ticket inventory & Redis reservations |
| **Order Service** | `8083` | `8083` | HTTP | Order state machine & transactional outbox |
| **Event Service** | `8084` | `8084` | HTTP | Event catalog & category management |
| **Payment Service** | `8085` | `8085` | HTTP | Payment processing & MockPay simulator |
| **Notification Service** | `8086` | `8086` | HTTP | Notifications & RabbitMQ workers |
| **Audit Service** | `8088` | `8088` | HTTP | Audit log processing |
| **Search Service** | `8090` | `8090` | HTTP | OpenSearch full-text indexing |
| **PostgreSQL** | `5432` | `5432` | TCP | Relational database storage |
| **Redis** | `6379` | `6379` | TCP | Caching, reservations, distributed locks |
| **Kafka Broker** | `9092` | `9092` | TCP | Domain event streaming broker |
| **Zookeeper** | `2181` | `2181` | TCP | Kafka coordination |
| **RabbitMQ Broker** | `5672` | `5672` | TCP | Background worker job queues |
| **RabbitMQ Management UI**| `15672` | `15672` | HTTP | Management web dashboard |
| **Keycloak OAuth2 Server**| `8089` | `8080` | HTTP | OAuth 2.0 / OpenID Connect realm |
| **MinIO Storage API** | `9000` | `9000` | HTTP | S3 object storage |
| **MinIO Web Console** | `9001` | `9001` | HTTP | S3 object storage web UI |
| **OpenSearch** | `9200` | `9200` | HTTP | Search engine API |
| **Debezium CDC Connect** | `8095` | `8083` | HTTP | Transactional Outbox CDC connector |
| **Prometheus** | `9090` | `9090` | HTTP | Metrics collection server |
| **Grafana** | `3000` | `3000` | HTTP | Observability dashboard web UI |
| **Loki** | `3100` | `3100` | HTTP | Centralized log aggregator |
| **Tempo** | `3200` | `3200` | HTTP | Distributed tracing backend |
| **OTel Collector** | `4319` / `4320` | `4317` / `4318` | gRPC/HTTP | OpenTelemetry trace collector |

---

## 22. Docker Compose Verification Status

Command:
```bash
docker compose ps
```

Status Output:
```text
NAME                      IMAGE                                         STATUS
dropzone-postgres         postgres:16-alpine                            Up (healthy)
dropzone-redis            redis:7-alpine                                Up (healthy)
dropzone-kafka            confluentinc/cp-kafka:7.5.0                   Up
dropzone-zookeeper        confluentinc/cp-zookeeper:7.5.0               Up
dropzone-rabbitmq         rabbitmq:3-management-alpine                  Up (healthy)
dropzone-keycloak         quay.io/keycloak/keycloak:24.0.1              Up
dropzone-minio            minio/minio                                   Up
dropzone-opensearch       opensearchproject/opensearch:2.12.0           Up
dropzone-debezium         debezium/connect:2.5.4.Final                  Up
dropzone-prometheus       prom/prometheus:v2.51.0                       Up
dropzone-grafana          grafana/grafana:10.4.0                        Up
dropzone-loki             grafana/loki:2.9.5                            Up
dropzone-tempo            grafana/tempo:2.4.0                           Up
dropzone-otel-collector   otel/opentelemetry-collector-contrib:0.96.0   Up
```

### Infrastructure Resilience & Volume Persistence
- Container restarts retain database tables, Redis data, MinIO objects, and Kafka topic logs via volume mounts (`postgres_data`, `redis_data`, `minio_data`, `opensearch_data`).
- No hardcoded `localhost` references in container communication; all services resolve via container DNS network `dropzone-network`.
