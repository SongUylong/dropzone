# Dropzone — High-Concurrence Event Ticketing & Microservices Platform

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud 2023](https://img.shields.io/badge/Spring%20Cloud-2023.0.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.6-black.svg)](https://kafka.apache.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-orange.svg)](https://www.rabbitmq.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.2-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2409ba.svg)](https://www.docker.com/)

**Dropzone** is an enterprise-grade, distributed microservices platform engineered for high-concurrency event ticketing and flash-sale demand spikes. Designed with resilient event-driven architecture, triple-layer idempotency, transactional outbox streaming, and fault-tolerant circuit breakers, Dropzone guarantees high availability, zero overselling, and automated self-healing.

---

## 🏛️ System Architecture

```text
                               ┌───────────────────────────────────┐
                               │       NGINX Ingress Proxy         │
                               │        (edge-01 : 80)             │
                               └─────────────────┬─────────────────┘
                                                 │
                               ┌─────────────────▼─────────────────┐
                               │         API Gateway (8080)        │
                               │  (Centralized Security & JWT)    │
                               └─────────────────┬─────────────────┘
                                                 │
            ┌───────────────────────────┬────────┴───────────┬───────────────────────────┐
            │                           │                    │                           │
  ┌─────────▼──────────┐      ┌─────────▼──────────┐  ┌──────▼───────────┐      ┌─────────▼──────────┐
  │  User Service      │      │  Event Service     │  │ Inventory Service│      │  Order Service     │
  │  (8081 - JPA DB)   │      │  (8084 - MinIO)    │  │ (8082 - Redis ZSet)    │  (8083 - Outbox)   │
  └────────────────────┘      └────────────────────┘  └──────────────────┘      └─────────┬──────────┘
                                                                                          │
                                                                                ┌─────────▼──────────┐
                                                                                │  Payment Service   │
                                                                                │  (8085 - Resilience│
                                                                                └─────────┬──────────┘
                                                                                          │
 ┌────────────────────────────────────────────────────────────────────────────────────────┴─────────────────────────────────┐
 │                                                EVENT-DRIVEN & MESSAGING INFRASTRUCTURE                                    │
 │                                                                                                                          │
 │  ┌──────────────────────────────────────────────┐              ┌──────────────────────────────────────────────────────┐  │
 │  │             Apache Kafka Broker              │              │                 RabbitMQ Worker Queues               │  │
 │  │  Topics: user, order, payment, inventory     │              │  Queues: ticket-generation, email, sms               │  │
 │  └──────────────────────┬───────────────────────┘              └──────────────────────────┬───────────────────────────┘  │
 └─────────────────────────┼─────────────────────────────────────────────────────────────────┼──────────────────────────────┘
                           │                                                                 │
                 ┌─────────▼──────────┐                                            ┌─────────▼──────────┐
                 │   Audit Service    │                                            │Notification Service│
                 │ (8086 - Postgres)  │                                            │ (8087 - PDF / QR)  │
                 └────────────────────┘                                            └────────────────────┘
```

---

## 🚀 Microservices Breakdown

| Service | Port | Description | Persistence / Tech |
|---|---|---|---|
| `api-gateway` | 8080 | Centralized Ingress Gateway, Rate Limiting & JWT Auth | Spring Cloud Gateway, Keycloak |
| `user-service` | 8081 | User Account Management & Profiles | PostgreSQL, Spring Data JPA |
| `inventory-service` | 8082 | High-Concurrency Flash Sale Waiting Room & Stock Reservations | Redis ZSet / Lua, PostgreSQL |
| `order-service` | 8083 | Order Lifecycle State Machine & Transactional Outbox Pattern | PostgreSQL, Debezium CDC, Kafka |
| `event-service` | 8084 | Event Catalog & Banner Uploads | PostgreSQL, MinIO Storage |
| `payment-service` | 8085 | Payment Processing & Resilience4j Fault Tolerance | PostgreSQL, Resilience4j |
| `notification-service` | 8086 | PDF Ticket Generation, QR Codes, Email & SMS Dispatch | RabbitMQ, MinIO, iText, ZXing |
| `audit-service` | 8087 | Centralized Kafka Event Auditing & Activity Ledger | PostgreSQL, Kafka Listeners |
| `search-service` | 8088 | Real-Time Event Search & Indexing | OpenSearch / Elasticsearch |
| `eureka-server` | 8761 | Dynamic Service Registration & Discovery | Spring Cloud Netflix Eureka |
| `config-server` | 8888 | Git-backed Centralized Configuration Server | Spring Cloud Config |

---

## 🛠️ Key Architectural Patterns

1. **Triple-Layer Idempotency**:
   - Layer 1: Distributed Redis Key Caching (`idempotency:{key}`).
   - Layer 2: Database `UNIQUE` constraints (`order_number`, `idempotency_key`).
   - Layer 3: Payment Callback Deduplication.
2. **Transactional Outbox Pattern**:
   - Atomic database transactions save `Order` and `OutboxEvent` in PostgreSQL before publishing to Kafka, eliminating dual-write inconsistency.
3. **Resilience4j Fault Tolerance**:
   - Circuit Breaker, Rate Limiter, Retry, and TimeLimiter wrappers safeguard downstream payment calls against thread exhaustion.
4. **Flash Sale Virtual Waiting Room**:
   - Redis Sorted Sets (`ZSet`) manage queue placement, position tracking, and rate-limited admission tokens (`WR_TOKEN_*`).
5. **RabbitMQ Worker Queues**:
   - Asynchronous worker jobs (`ticket-generation.queue`, `email.queue`, `sms.queue`) decouple heavy PDF ticket compilation and QR code generation from core transaction paths.

---

## ⚙️ Staging VM Lab Map (4 Ubuntu 24.04 VMs)

| Hostname | IP Address | Roles & Services |
|---|---|---|
| `edge-01` | `192.168.56.10` | NGINX Ingress Proxy (`:80`), API Gateway (`:8080`) |
| `app-01` | `192.168.56.11` | `user-service`, `event-service`, `inventory-service` |
| `app-02` | `192.168.56.12` | `order-service`, `payment-service`, `notification-service` |
| `infra-01` | `192.168.56.13` | Eureka (`:8761`), Config Server (`:8888`), Postgres (`:5432`), Redis (`:6379`), Kafka (`:9092`), RabbitMQ (`:5672`), Keycloak (`:8089`), MinIO (`:9000`) |

---

## 🚦 Quickstart & Local Execution

### Prerequisites
- **JDK 21**
- **Docker & Docker Compose**
- **Maven 3.9+**

### 1. Start Infrastructure Stack
```bash
docker compose up -d
```

### 2. Build & Run Microservices
```bash
# Compile and build all services
./mvnw clean package -DskipTests

# Run via Docker Compose or spring-boot:run
```

### 3. Verify Health Checks
```bash
curl http://localhost:8080/actuator/health
```

---

## 🧪 Automated Testing & Chaos Lab

- **CI Pipeline**: Automated build, Testcontainers integration tests, SonarQube quality gate, Trivy security scan, container build, and Ansible deployment.
- **Rollback Verification**: `infrastructure/lab/ansible/test-rollback-deployment.py`
- **Network Chaos**: `infrastructure/lab/ansible/test-network-chaos.py`
- **Full Staging Checkout Test**: `infrastructure/lab/ansible/test-staging-checkout-flow.py`

---

## 📜 License
Licensed under the Apache License 2.0.
