DROPZONE

Complete Codebase Learning Guide

How to re-read, understand, verify, debug, explain, and own an AI-built Java enterprise microservices system

Target architectureJava + Spring Boot + Spring CloudEureka + Config Server + API GatewayKeycloak + OAuth2/OIDC/JWTPostgreSQL + JPA/Hibernate + FlywayRedis + Kafka + RabbitMQResilience4j + Outbox + DebeziumOpenSearch + MinIOOpenTelemetry + Prometheus + Grafana + Loki + TempoDocker + CI/CD + VM deployment

Purpose: by the end of this guide, you should be able to open any important file in DropZone and explain why it exists, who calls it, what data it owns, what happens on success, what happens on failure, and how you prove that it works.

How to Use This Guide

Do not read the repository randomly. Follow the chapters in order and keep the code open beside this document. The guide is written for an AI-built codebase where the system appears to work, but you want to understand and personally own every important decision.

The rule for every file you read

Question

What you must write down

1. Why does this file exist?

The responsibility in one sentence.

2. Who calls or loads it?

Framework, controller, service, listener, config loader, CI runner, etc.

3. What does it depend on?

Classes, database, Redis, Kafka, RabbitMQ, HTTP service, configuration.

4. What state does it change?

Database rows, cache entries, messages, files, nothing.

5. What happens if it fails?

HTTP error, retry, rollback, event retry, DLQ, partial failure.

6. How is it tested?

Unit test, integration test, contract test, manual test, load test.

7. What breaks if I delete it?

This proves you understand its actual role.

Recommended reading order inside every Spring service

→ pom.xml / build.gradle

→ application.yml and environment configuration

→ Application main class

→ Configuration classes

→ Security configuration

→ Controllers / API entry points

→ Request and response DTOs

→ Validation rules

→ Application/service layer

→ Domain models and state transitions

→ Repositories

→ JPA entities

→ Flyway migrations

→ HTTP clients

→ Kafka producers/consumers

→ RabbitMQ publishers/workers

→ Cache/Redis logic

→ Exception handling

→ Observability/logging

→ Tests

What not to do

Do not ask AI to explain the whole repository in one paragraph and accept that as learning.

Do not memorize annotations without tracing what happens at runtime.

Do not mark a concept learned until you can predict what happens when a dependency is stopped.

Do not skip tests. Tests often reveal the intended design more clearly than implementation code.

Do not trust names such as PaymentService or OrderProcessor. Trace the real behavior.

Learning Map

1. Reconstruct the whole architecture before reading implementation

2. Java concepts you must understand in this repository

3. Maven and dependency management

4. Spring Boot startup and dependency injection

5. Standard Spring service structure

6. Configuration, profiles, environment variables, and secrets

7. Eureka service discovery

8. Spring Cloud Config Server

9. API Gateway and routing

10. Keycloak, Spring Security, OAuth2, OIDC, and JWT

11. User Service

12. Event Service

13. PostgreSQL, JPA, Hibernate, and Flyway

14. Inventory Service

15. Redis

16. Order Service and state machines

17. Idempotency

18. Payment Service

19. Resilience4j and synchronous failure handling

20. Kafka and event-driven communication

21. Event contracts, consumer groups, retries, and duplicates

22. Transactional Outbox

23. Debezium and CDC

24. Notification Service

25. RabbitMQ and worker queues

26. Ticket generation and MinIO

27. Search Service and OpenSearch

28. Audit Service

29. Distributed transactions and eventual consistency

30. Waiting room and flash-sale concurrency

31. Logging, errors, and correlation IDs

32. OpenTelemetry, Prometheus, Grafana, Loki, Tempo, Alertmanager

33. Docker and container behavior

34. Docker Compose and local networking

35. Testing from unit to load tests

36. CI/CD, SonarQube, Trivy, Harbor, and versioning

37. VM deployment, NGINX, Ansible, Terraform, and staging

38. Scaling, high availability, and failure recovery

39. Security audit from client to database

40. AI-generated code audit

41. End-to-end request and event tracing exercises

42. Debugging playbook

43. What you should be able to explain in an interview

44. Suggested 30-day learning schedule

45. Final ownership checklist and glossary

1. Reconstruct the Whole Architecture Before Reading Implementation

First, rebuild the architecture in your own words. Do this without looking at class-level implementation. Your goal is to understand responsibilities and boundaries before syntax.

Client / Browser      |      vNGINX / Edge      |      vSpring Cloud Gateway      |      +--> Keycloak for identity/JWT      |      +--> Eureka for service discovery      |      +--> User / Event / Inventory / Order / Payment servicesBusiness data  -> PostgreSQLFast temporary state -> RedisDomain events -> KafkaBackground jobs -> RabbitMQFiles -> MinIOSearch index -> OpenSearchConfiguration -> Spring Cloud ConfigTelemetry -> OpenTelemetry -> Prometheus/Loki/Tempo -> Grafana

Write an ownership table

Component

Owns / answers

Must not own

Gateway

Routing, edge authentication enforcement, CORS, rate limiting, headers

Order/payment business decisions

Eureka

Where service instances are

Business configuration or data

Config Server

Normal environment configuration

Business records

Keycloak

Identity, login, roles, token issuance

DropZone user profile business data

User Service

Business profile

Passwords

Event Service

Events, ticket categories, pricing definitions

Reservations or payments

Inventory Service

Stock and reservations

Payment truth

Order Service

Order lifecycle

Raw identity credentials

Payment Service

Payment attempts/status

Ticket inventory

Kafka

Durable event stream

Synchronous request/response

RabbitMQ

Work queue / jobs

Long-term business source of truth

PostgreSQL

Durable transactional state

Cross-service direct table sharing

Redis

Fast temporary/distributed state

Only copy of paid order truth

MinIO

Objects/files

Relational business relationships

OpenSearch

Search-optimized index

System of record

Learning exercise

☐ Draw the architecture from memory.

☐ For each arrow, write the protocol: HTTP, Kafka, RabbitMQ, JDBC, Redis protocol, object storage API.

☐ Circle every place where data is durable.

☐ Mark every place where duplicate delivery is possible.

☐ Mark every synchronous dependency that can time out.

☐ Mark every boundary where authentication or authorization is checked.

2. Java Concepts You Must Understand in This Repository

You do not need to master every part of Java before reading DropZone, but you should recognize the following concepts whenever they appear.

Concept

Why it appears

What to verify in code

Class

Main unit of implementation

Responsibility is focused, not a god class

Interface

Contract/abstraction

Which implementation is injected and why

Record / DTO

API/event data carrier

Immutable where appropriate; correct validation

Enum

States such as order/payment status

All states/transitions handled

Annotation

Spring metadata

Know which framework behavior it activates

Constructor injection

Dependency injection

Dependencies explicit and testable

Generic types

Repository/result/container types

Understand actual type flowing through

Optional

Absence handling

Not used to hide errors or as entity fields carelessly

Exception

Failure control flow

Mapped to correct API/event behavior

Stream API

Collection processing

Readable and not hiding side effects

CompletableFuture/reactive types

Async work

Know thread model and error handling

synchronized/locks

Concurrency

Does not pretend to be distributed locking

equals/hashCode

Collections/entity comparison

Correct especially for value objects

Serialization

JSON/Kafka messages

Stable fields and compatibility

Annotations to recognize immediately

@SpringBootApplication: starts Spring Boot, component scanning, auto-configuration.

@RestController: HTTP controller returning serialized responses.

@RequestMapping/@GetMapping/@PostMapping/etc.: HTTP routing.

@Service: application/business service bean.

@Repository: persistence adapter bean and exception translation boundary.

@Entity: JPA-managed database entity.

@Transactional: transaction boundary, usually via Spring proxy.

@Configuration/@Bean: explicit Spring bean definitions.

@ConfigurationProperties: binds external configuration into typed objects.

@Valid/@Validated: activates bean validation.

@KafkaListener: Kafka consumer endpoint.

@RabbitListener: RabbitMQ consumer/worker endpoint.

@PreAuthorize: method-level authorization.

@ControllerAdvice/@ExceptionHandler: central HTTP error translation.

Questions to answer when reading Java

☐ Is this object stateful or stateless?

☐ Can multiple requests call this bean concurrently?

☐ Does this method change durable state?

☐ Does it make a network call?

☐ Is it inside a database transaction?

☐ Can it be retried safely?

☐ Can it receive the same message twice?

☐ Does it assume a single JVM instance?

3. Maven and Dependency Management

Read the root pom.xml before service code. The build file tells you the platform, Java version, dependency families, modules, plugins, test stack, and how artifacts are packaged.

Root POM

groupId/artifactId/version: project identity.

packaging=pom: aggregator/parent behavior.

modules: which services belong to the build.

java.version: language/runtime target.

Spring Boot parent/BOM: dependency versions managed centrally.

Spring Cloud BOM: ensures Eureka/Gateway/Config compatibility.

pluginManagement: compiler, tests, packaging, coverage, quality plugins.

Per-service POM

☐ Identify every starter and explain why the service needs it.

☐ Find unused dependencies.

☐ Check whether test dependencies are test-scoped.

☐ Check whether database driver is runtime-scoped.

☐ Understand Spring Boot Maven plugin and executable JAR creation.

Commands to understand

mvn cleanmvn testmvn verifymvn packagemvn dependency:treemvn -pl services/order-service test

Learning goal: when a build fails, you should know whether the failure happened in compilation, unit tests, integration tests, packaging, dependency resolution, or a quality gate.

4. Spring Boot Startup and Dependency Injection

Open one simple service and trace startup. Spring Boot is doing a lot for you automatically, so you need to know which behavior comes from your code and which comes from the framework.

Startup sequence to understand

→ JVM starts the main method.

→ SpringApplication.run creates the application context.

→ Component scanning finds controllers, services, repositories, configs, listeners.

→ Auto-configuration examines dependencies and properties.

→ Beans are created and dependencies injected.

→ DataSource/JPA/Flyway may initialize.

→ Kafka/Rabbit listeners may be registered.

→ Eureka client registers the service.

→ Embedded HTTP server starts.

→ Actuator health becomes available.

Dependency injection

Prefer constructor injection. When you see a constructor with Repository, Client, Producer, or another Service, treat it as the class declaring what it needs to operate.

☐ Find the bean definition for every constructor dependency.

☐ Check whether the dependency is local code or network adapter.

☐ Check whether circular dependencies exist.

☐ Understand singleton bean concurrency: one bean instance may serve many requests.

5. Standard Spring Service Structure

service/  Application.java  config/  controller/  dto/  domain/ or model/  service/  repository/  entity/  client/  messaging/  exception/  mapper/  resources/    application.yml    db/migration/  test/

Read from the boundary inward

→ Controller: what can callers ask the service to do?

→ DTO: what data is accepted/returned?

→ Validation: what input is rejected before business logic?

→ Service/application layer: what use case is executed?

→ Domain/entity: what rules and state exist?

→ Repository: what data is loaded/saved?

→ Messaging/client: what external effects occur?

→ Tests: what behavior is intended?

6. Configuration, Profiles, Environment Variables, and Secrets

Find every application.yml, bootstrap/config import, Config Server file, environment variable, and secret reference. Configuration is part of the runtime architecture.

Know the precedence

Spring configuration can come from packaged files, profile-specific files, Config Server, environment variables, command-line arguments, and secret systems. You do not need to memorize every precedence rule, but you must know which source wins in your deployment.

Classify every property

Type

Examples

Where it belongs

Normal config

timeouts, feature flags, topic names

Config Server / environment config

Environment address

DB host, Kafka broker, Eureka URL

Environment-specific config

Secret

password, API secret, private key

Secret store/env injection, not Git

Build-time value

artifact version

Build metadata

Business data

ticket price, order state

Database, not configuration

Profiles

dev      -> local developer dependenciesstaging  -> VM mimic environmentprod     -> real production settings

☐ Verify the same Docker image can run under all profiles.

☐ Find every localhost and explain why it is dev-only or a bug.

☐ Find every credential and ensure it is not committed.

☐ Understand what happens if Config Server is unavailable at startup.

7. Eureka Service Discovery

Study Eureka before service-to-service clients. Its job is not messaging or configuration. It is a registry of service instances and their status.

Trace registration

ORDER-SERVICE starts   -> knows Eureka URL from config   -> registers name + host + port + metadata   -> sends heartbeatsPAYMENT-SERVICE does the sameORDER-SERVICE wants PAYMENT-SERVICE   -> discovery/load-balancer obtains an instance   -> HTTP request goes to selected instance

Read these areas

☐ Eureka server application/config.

☐ eureka.client.* settings in services.

☐ spring.application.name for every service.

☐ Health/lease settings if customized.

☐ Gateway lb:// routes and HTTP client load balancing.

Failure questions

☐ What happens if Eureka is temporarily down but services already have cached registry data?

☐ How long before a dead instance disappears?

☐ What happens when two Payment Service instances register?

☐ Does any code still hardcode a Payment Service URL?

8. Spring Cloud Config Server

Config Server centralizes external configuration. Trace how each service imports config and how dev/staging/prod values differ.

Read

☐ Config Server main application.

☐ Backend selection: Git/native/etc.

☐ Config repository naming conventions.

☐ Per-service profile files.

☐ Client config import settings.

☐ Authentication/encryption if configured.

Questions

☐ Which properties are common to all services?

☐ Which properties differ by service?

☐ Which differ by environment?

☐ What is the startup behavior if Config Server cannot be reached?

☐ Can a config change be rolled back through Git history?

9. API Gateway and Routing

The Gateway is the public edge into the microservice system. Read route configuration before filters/security so you understand where traffic goes.

Trace one request

GET /api/orders/123   -> NGINX (in staging/prod)   -> API Gateway   -> route predicate matches /api/orders/**   -> authentication/security filters   -> optional rate limiting / headers / tracing   -> lb://ORDER-SERVICE   -> Eureka/load balancer resolves instance   -> Order Service controller

Inspect

☐ Static routes vs DiscoveryClient-generated routes.

☐ Path rewrite/StripPrefix filters.

☐ CORS.

☐ Authentication resource-server config.

☐ Rate limiter.

☐ Correlation/trace header handling.

☐ Public health endpoints.

☐ Whether business logic accidentally lives in Gateway.

10. Keycloak, Spring Security, OAuth2, OIDC, and JWT

Separate authentication from authorization. Keycloak proves who the caller is and issues tokens. Each service still decides whether that identity may perform a business action.

Terms

Term

Meaning in DropZone

OAuth 2.0

Authorization framework used to obtain/access protected resources

OIDC

Identity layer on OAuth2; login/user identity

Access token

Short-lived token presented to APIs

Refresh token

Used by client to obtain new access tokens

JWT

Common signed token format carrying claims

Realm

Keycloak security domain

Client

Application registered with Keycloak

Role

USER / ORGANIZER / SUPPORT / ADMIN

Resource Server

Gateway/service validating access tokens

Trace login

Browser -> Keycloak loginKeycloak authenticates userKeycloak -> access token (+ refresh token depending on flow)Browser -> Gateway with Bearer access tokenGateway validates issuer/signature/expiryService validates authorization for business action

Inspect security code

☐ SecurityFilterChain / SecurityWebFilterChain.

☐ JWT issuer URI/JWK validation.

☐ Role/authority mapping.

☐ @PreAuthorize or controller security.

☐ Public vs authenticated routes.

☐ Ownership checks such as user can only read own ticket/order.

☐ Token forwarding for service-to-service calls if used.

Security exercises

☐ Call a protected API without a token.

☐ Call with expired token.

☐ Call ORGANIZER endpoint as USER.

☐ Modify userId/orderId in request and verify ownership protection.

☐ Confirm User Service does not store password hashes.

11. User Service

Read User Service as the simplest business service. It should be a good place to learn controller → service → repository → database flow.

Trace

HTTP request -> UserController -> validated DTO -> UserApplicationService/UserService -> repository -> JPA/Hibernate -> PostgreSQL -> entity/DTO mapping -> response

Understand

☐ How Keycloak subject/user ID maps to internal profile.

☐ Which fields are editable by user vs admin.

☐ Database constraints on email/keycloak ID.

☐ How not-found and duplicate-email errors are returned.

☐ Whether any service directly reads User Service tables (it should not).

12. Event Service

Event Service owns events and sale definitions, not live reservations or payment state.

Trace organizer event creation

ORGANIZER token -> Gateway -> EventController -> authorization + validation -> EventService -> Event entity / ticket categories -> PostgreSQL -> optional EventCreated Kafka event -> response

Learn

☐ Aggregate boundary: Event + ticket categories.

☐ Price representation: avoid floating-point money mistakes.

☐ Sale start/end validation.

☐ Published/draft/cancelled status rules.

☐ Event images metadata vs MinIO object bytes.

☐ How event updates reach Search Service if event indexing exists.

13. PostgreSQL, JPA, Hibernate, and Flyway

This chapter applies to every service with a database.

JPA/Hibernate mental model

Controller/Service    |Repository interface    |Spring Data implementation    |Hibernate ORM    |JDBC/DataSource/connection pool    |PostgreSQL

Inspect entities

☐ @Entity and @Table names.

☐ @Id strategy and UUID handling.

☐ @Column constraints.

☐ Indexes and unique constraints in migrations.

☐ Relationships and fetch strategy.

☐ Enum storage strategy.

☐ Timestamps/time zones.

☐ Optimistic locking @Version where used.

☐ Avoid exposing entities directly as API DTOs.

Transactions

@Transactional normally works through Spring proxy boundaries. Understand where a transaction begins, which repository operations participate, when commit happens, and which exceptions trigger rollback.

Flyway

☐ Read migrations in version order.

☐ Compare migration schema with current entities.

☐ Recreate an empty database and verify migrations alone produce a working schema.

☐ Understand why production schema should not rely on Hibernate ddl-auto=create/update.

14. Inventory Service

Inventory is where concurrency and correctness become serious. Understand the difference between durable stock truth and temporary reservations.

Model

Total stock = fixed/controlled amountAvailable = can still be reservedReserved = temporarily heldSold = permanently consumed after successful checkoutInvariant example:Total = Available + Reserved + Sold

Read carefully

☐ Reservation creation path.

☐ Stock decrement/check operation.

☐ Reservation identifier and owner.

☐ Expiration path.

☐ Conversion from reserved to sold.

☐ Release after payment failure/cancellation.

☐ Database constraints and locking.

☐ Redis atomic operations/Lua if used.

☐ Reconciliation logic between Redis and PostgreSQL.

Concurrency exercise

Run 100 buyers against one remaining ticket. Then step through the exact code that guarantees only one reservation succeeds. If the explanation ends with Java synchronized, ask how it works with two Inventory Service instances.

15. Redis

Redis is a shared in-memory data store. In DropZone it should improve speed or coordinate temporary/distributed state, not replace durable order/payment truth.

Map every key pattern

Key example

Purpose

Expected lifetime

reservation:{id}

Temporary ticket reservation

TTL, e.g. minutes

idempotency:order:{key}

Duplicate request protection

Configured window

waiting:{event}:{user}

Waiting-room state

Sale/session lifetime

rate:{user/ip}

Rate limit counter

Short window

cache:event:{id}

Hot event cache

Cache TTL

Understand

☐ String vs Hash vs Set vs Sorted Set usage.

☐ TTL behavior.

☐ Atomic commands and Lua scripts.

☐ What survives Redis restart and what does not.

☐ Why local Java Map is not equivalent in a multi-instance system.

☐ Failure mode when Redis is unavailable.

16. Order Service and State Machines

Order Service owns the customer purchase lifecycle. Read the state model before any controller.

PENDING  -> RESERVED  -> PAYMENT_PENDING  -> PAID  -> CONFIRMEDFailure/cancel paths may include:PAYMENT_PENDING -> FAILEDRESERVED -> EXPIRED/CANCELLED

For every state transition

Question

Example

Who triggers it?

HTTP command, Kafka event, scheduler

Precondition?

Current status must be PAYMENT_PENDING

Database change?

Update order status

Side effect?

Write outbox event

Duplicate behavior?

Second PaymentCompleted must be harmless

Failure behavior?

Transaction rollback / retry

Read

☐ Order creation.

☐ Order amount calculation.

☐ Inventory reservation reference.

☐ Payment initiation.

☐ Payment event handlers.

☐ Confirmation logic.

☐ Cancellation/expiration.

☐ Outbox writes.

☐ Unique constraints/idempotency.

17. Idempotency

Idempotency means repeating the same logical request does not repeat the business effect. It is essential for checkout, payments, callbacks, and at-least-once messaging.

Understand the pattern

Client sends Idempotency-Key KFirst request:  no stored K -> execute -> store result under K -> return resultSecond request with same K:  find stored K -> return same/known result -> do NOT execute again

Check idempotency at multiple levels

☐ HTTP order creation.

☐ Reservation creation.

☐ Payment creation.

☐ Payment provider callback.

☐ Kafka consumers.

☐ RabbitMQ workers.

☐ Ticket generation.

Do not rely on Redis alone

For financially or permanently important uniqueness, database unique constraints are often the final guard. Redis may accelerate or coordinate, but the durable database should enforce critical uniqueness where appropriate.

18. Payment Service

Even with MockPay, treat the service like real money infrastructure. The backend must calculate/validate trusted amount and must not trust client-provided payment truth.

Trace payment

Order Service has trusted order total -> Payment Service create payment -> payment attempt persisted -> provider/mock provider invoked -> success/failure/processing -> PaymentCompleted or PaymentFailed event -> Order Service updates lifecycle

Inspect

☐ Money type/precision.

☐ Server-side amount source.

☐ Payment attempt ID.

☐ Order ID uniqueness rules.

☐ Idempotency key.

☐ Callback signature/auth simulation if present.

☐ Duplicate callback handling.

☐ Timeout/unknown outcome handling.

☐ Payment status transitions.

☐ Audit trail.

19. Resilience4j and Synchronous Failure Handling

Resilience4j protects synchronous calls. It does not magically make unsafe retries safe.

Patterns

Pattern

Purpose

Main risk

Timeout

Stop waiting forever

Too short causes false failures

Retry

Repeat transient safe operation

Duplicate side effects

Circuit Breaker

Stop calling repeatedly failing dependency

Incorrect thresholds

Bulkhead

Limit resource consumption by one dependency

Too restrictive capacity

Rate Limiter

Limit request rate

Rejecting legitimate bursts

Trace a failure

Order -> PaymentPayment starts timing outTimeout records failuresCircuit breaker threshold reachedCircuit opensNew calls fail fast / fallbackAfter wait period, half-open trial calls occurSuccess closes or failure re-opens

Read configuration and metrics

☐ Which methods are protected?

☐ Which exceptions count as failures?

☐ Retry count/backoff.

☐ Circuit window/threshold.

☐ Fallback behavior.

☐ How metrics are exposed.

20. Kafka and Event-Driven Communication

Kafka decouples producers from consumers through durable topics. Read events as business facts that have already happened, not remote procedure calls.

Mental model

Producer -> Topic partitions -> Consumer Group(s)Payment Service publishes PaymentCompletedOrder Service group consumes itAudit Service group also consumes itNotification Service may consume itEach group gets its own logical copy of the stream

Study

☐ Bootstrap server configuration.

☐ Topic names and creation.

☐ Producer serialization.

☐ Event envelope: eventId, type, timestamp, aggregateId, payload/version.

☐ Consumer deserialization.

☐ Consumer group IDs.

☐ Offset commit/ack behavior.

☐ Retries and dead-letter strategy.

☐ Ordering assumptions and partition key.

☐ Duplicate delivery handling.

21. Event Contracts, Consumer Groups, Retries, and Duplicates

Create an event catalog from the code

Event

Producer

Consumers

Why it exists

OrderCreated

Order Service

Inventory/Audit/etc.

New order fact

InventoryReserved

Inventory Service

Order/Audit

Reservation succeeded

PaymentCompleted

Payment Service

Order/Audit/Notification

Payment succeeded

PaymentFailed

Payment Service

Order/Audit

Payment failed

OrderConfirmed

Order Service

Notification/Ticket/Audit

Purchase finalized

EventUpdated

Event Service

Search Service

Refresh search index

Questions for each event class

☐ Can old consumers read a newer event version?

☐ What field is used as partition key?

☐ Can the same event be processed twice?

☐ If consumer crashes after DB commit but before offset commit, what happens?

☐ Where does poison/bad data go?

☐ Is the event a fact (PaymentCompleted) rather than a command disguised as a fact?

22. Transactional Outbox

The outbox solves the dual-write problem: updating PostgreSQL and publishing Kafka are two separate systems and cannot normally commit atomically together.

The bug without outbox

BEGIN DB transactionINSERT orderCOMMITProcess crashes before Kafka publishResult:Order exists but downstream services never hear OrderCreated

Outbox solution

BEGIN ONE DB transactionINSERT orderINSERT outbox_eventCOMMITSeparate publisher / Debezium observes outbox -> publishes to Kafka -> consumers process idempotently

Inspect

☐ Outbox table schema.

☐ Event payload and metadata.

☐ Same @Transactional boundary as business write.

☐ Publisher or Debezium connector.

☐ Cleanup/retention strategy.

☐ Duplicate publication behavior.

23. Debezium and CDC

Debezium reads database change logs and turns committed changes into Kafka events. In the outbox pattern it can publish outbox rows without the application directly doing a second network write.

Trace

Order transaction commits outbox row -> PostgreSQL WAL contains change -> Debezium connector reads change -> transforms/routes outbox record -> Kafka topic -> consumers

Understand

☐ Connector configuration.

☐ Database permissions/replication settings.

☐ Which table is captured.

☐ Topic routing.

☐ Offset/state storage.

☐ Restart behavior.

☐ Schema evolution.

☐ Why consumers still need idempotency.

24. Notification Service

Notification Service should react to business facts and convert them into communication/jobs. It should not determine whether an order is paid.

Trace

OrderConfirmed Kafka event -> Notification consumer -> decide required notifications/jobs -> publish email/ticket/SMS jobs to RabbitMQ -> workers perform slow/external work

Inspect

☐ Which Kafka events it consumes.

☐ What deduplication key it uses.

☐ How notification records/status are persisted if applicable.

☐ How jobs are published to RabbitMQ.

☐ What happens when email provider fails.

25. RabbitMQ and Worker Queues

RabbitMQ is used for work distribution where one job should normally be handled by one worker, such as PDF generation or email delivery.

Understand

Concept

Meaning

Exchange

Receives published messages and routes them

Queue

Stores jobs until consumed

Binding

Routing relationship between exchange and queue

Routing key

Routing value used by exchange

Ack

Consumer confirms successful handling

Nack/reject

Consumer indicates failure

DLQ

Queue for messages that cannot be processed normally

Prefetch

How many unacked jobs a consumer may receive

Trace a ticket job

Notification Service publishes ticket job -> exchange -> ticket-generation.queue -> Ticket Worker receives -> generates file -> stores MinIO object -> updates status -> ACK

Failure exercise

Kill the Ticket Worker after it receives a message but before acknowledgement. Observe whether the message is re-delivered and whether the worker is idempotent.

26. Ticket Generation and MinIO

Separate metadata from bytes

PostgreSQL  ticket_id  order_id  object_key  mime_type  statusMinIO  actual PDF / QR / image bytes

Read

☐ Ticket ID generation.

☐ QR payload/verification design.

☐ PDF rendering.

☐ MinIO bucket and object naming.

☐ Upload retry behavior.

☐ Access control / signed URL or API download path.

☐ Duplicate job behavior.

☐ What happens if file upload succeeds but DB update fails, or reverse.

27. Search Service and OpenSearch

Search is a projection/index optimized for query. PostgreSQL/Event Service remains the source of truth.

Trace indexing

Event Service changes event -> EventUpdated Kafka event -> Search Service consumes -> transforms document -> indexes in OpenSearchUser searches -> Search API -> OpenSearch -> results

Learn

☐ Index mapping.

☐ Document ID.

☐ Full-text fields vs keyword fields.

☐ Pagination and sorting.

☐ Reindex process if index is lost.

☐ Handling unpublished/deleted events.

☐ Eventual delay between database update and search visibility.

28. Audit Service

Audit Service is useful because Kafka already carries important business facts. Audit records should answer who did what, when, and to which entity.

Inspect

☐ Consumed event list.

☐ Actor/user identity fields.

☐ Event ID and timestamp.

☐ Aggregate/entity ID.

☐ Immutability expectations.

☐ Pagination/search of audit records.

☐ Difference between audit log and application debug log.

29. Distributed Transactions and Eventual Consistency

Microservices usually do not share one ACID transaction across Order, Inventory, and Payment databases. Instead, each service commits locally and the overall workflow converges through events and compensation.

Example saga

1. Order created locally2. Inventory reserves locally3. Payment succeeds locally4. Order confirms locallyIf payment fails:- Order moves to failed/cancelled state- Inventory reservation is releasedThese are separate transactions coordinated by workflow/events.

Concepts to learn

Local ACID transaction.

Eventual consistency.

Saga choreography vs orchestration.

Compensating action.

Idempotent consumer.

Outbox.

Reconciliation job.

Why two services should not directly mutate each other's database.

30. Waiting Room and Flash-Sale Concurrency

The waiting room protects downstream systems from a huge burst. It is admission control, not inventory truth.

Waiting room flow

50,000 users arrive -> waiting-room state in Redis -> ordered/fair admission policy -> issue short-lived admission token -> allow controlled rate into checkout -> inventory still enforces correctness

Concurrency topics

Race condition.

Atomic operation.

Optimistic locking.

Pessimistic locking.

Redis Lua atomic script.

Distributed lock tradeoffs.

Backpressure.

Rate limiting.

Hot keys.

Database contention.

Load-test interpretation.

Proof

10,000 tickets50,000 buyersExpected:Sold = 10,000Oversold = 0Negative inventory = 0Duplicate orders = 0Duplicate payments = 0

31. Logging, Errors, and Correlation IDs

HTTP error path

Controller/service throws domain/validation exception -> @ControllerAdvice maps exception -> consistent HTTP status + safe error body -> log contains traceId and entity context -> no secrets or internal stack trace exposed to client

Logging checklist

☐ Timestamp.

☐ Service name.

☐ Log level.

☐ Trace/correlation ID.

☐ Order/payment/event ID where relevant.

☐ Useful reason/error code.

☐ No password/token/private secrets.

☐ Avoid logging entire sensitive request bodies.

Know HTTP statuses

Status

Typical meaning

400

Invalid input

401

No/invalid authentication

403

Authenticated but not allowed

404

Resource not found

409

Conflict such as duplicate/state conflict

429

Rate limited

500

Unexpected internal failure

502/503/504

Upstream/unavailable/timeout depending on layer

32. OpenTelemetry, Prometheus, Grafana, Loki, Tempo, Alertmanager

Observability means being able to answer what happened without attaching a debugger to production.

Three signals

Signal

Tool

Question answered

Metrics

Prometheus

How much/how often/how slow?

Logs

Loki

What did the application say happened?

Traces

Tempo

Where did this request spend time across services?

Dashboards

Grafana

How do we visualize all of it?

Alerts

Alertmanager

When should a human be notified?

Instrumentation

OpenTelemetry

How telemetry is generated/propagated

Trace one checkout

traceId=abcGateway span  Order Service span    Inventory HTTP span    Payment HTTP span  Kafka produce span    Kafka consume span      Notification span

Metrics to understand

Request rate.

Error rate.

P50/P95/P99 latency.

JVM heap/GC/threads.

DB pool usage.

Kafka consumer lag.

RabbitMQ queue depth.

Payment success rate.

Orders per minute.

Inventory reservation rate.

33. Docker and Container Behavior

A Docker image should package one deployable service and remain environment-neutral.

Read Dockerfile line by line

☐ Base image and Java runtime.

☐ Build stage vs runtime stage.

☐ Artifact copied into image.

☐ User permissions/non-root.

☐ Entrypoint/command.

☐ Exposed port documentation.

☐ Healthcheck if present.

☐ No secrets baked in.

☐ Image versioning.

Understand runtime isolation

localhost inside a container means the container itself. Other containers/services require Docker DNS/container names or external network addresses. Persistent data needs volumes/external services, not the writable container layer.

34. Docker Compose and Local Networking

Read docker-compose.yml as a local topology definition: services, images/builds, networks, volumes, ports, environment, dependencies, and health checks.

For every Compose service

☐ What image runs?

☐ What host ports are published?

☐ What internal port is used?

☐ Which network?

☐ Which volumes?

☐ Which environment variables?

☐ Which healthcheck?

☐ What other services does it depend on?

Networking

Host browser -> published port -> containerContainer -> another container by service DNS nameDo not confuse host localhost with container localhost.

35. Testing from Unit to Load Tests

Test type

Tool

What it should prove

Unit

JUnit + Mockito

Pure business logic/state rules

Integration

Testcontainers

Real DB/Redis/Kafka/RabbitMQ behavior

HTTP/API

REST Assured

Endpoint contract and security

External HTTP simulation

WireMock

Provider/client behavior

Contract

Pact

Consumer/provider compatibility

Load/concurrency

k6/JMeter

Throughput, latency, correctness under load

Read tests before trusting implementation

☐ What scenario is named?

☐ What setup creates state?

☐ What action is executed?

☐ What is asserted?

☐ Does the test use a real dependency or mock?

☐ Does it cover failure/duplicate behavior?

Important test scenarios

100 users buy last ticket.

Duplicate order request.

Duplicate payment callback.

Payment timeout.

Kafka redelivery.

RabbitMQ worker crash.

Reservation TTL.

Service restart.

Outbox recovery.

Authorization ownership violation.

36. CI/CD, SonarQube, Trivy, Harbor, and Versioning

Read CI configuration as executable deployment policy.

Pipeline

Git push / PR -> checkout -> compile -> unit tests -> integration tests -> quality analysis -> vulnerability scanning -> Docker image build -> image tag/version -> push to registry -> deploy to environment (when configured)

Tools

Tool

Purpose

GitHub Actions

CI workflow runner

SonarQube

Code quality/static analysis

Trivy

Dependency/image vulnerability scanning

Harbor

Container image registry

Git tag/image tag

Rollback/deployment identity

Audit for fake CI

☐ No `|| true` hiding test failures.

☐ Quality gate actually blocks when required.

☐ Security scan policy meaningful.

☐ Image is built from current artifact, not stale output.

☐ Deploy uses immutable version tag, not only latest.

37. VM Deployment, NGINX, Ansible, Terraform, and Staging

The VM lab is your production mimic. The goal is to prove the same image works on separate Linux machines with real networking.

Topology

edge-01   -> NGINX + Gatewayapp-01    -> User + Event + Inventoryapp-02    -> Order + Payment + Notificationinfra-01  -> Eureka + Config + PostgreSQL + Redis + Kafka + RabbitMQ + Keycloak

Tool responsibilities

Tool

Responsibility

VMware/Vagrant

Create/manage local VMs

Ansible

Configure OS and deploy services

Docker

Run application/infrastructure containers

NGINX/HAProxy

Edge proxy/load balancing

Terraform

Provision real/cloud infrastructure later

Deployment flow

GitHub -> CI -> versioned Docker image -> Registry -> Ansible pulls image on VM -> starts/replaces container -> health check -> Eureka registration -> traffic

Learn by failure

☐ Stop one service container.

☐ Stop app-02 completely.

☐ Run two Order Service instances.

☐ Deploy new version.

☐ Roll back to previous version.

☐ Add latency/packet loss and observe Resilience4j.

38. Scaling, High Availability, and Failure Recovery

Scaling means multiple identical service instances can handle work without corrupting shared state. High availability means critical service continues through component failure.

Questions for every service

☐ Can I run two instances simultaneously?

☐ Is any important state stored in local memory?

☐ Is any lock only JVM-local?

☐ Are scheduled jobs duplicated across instances?

☐ Are Kafka consumer groups configured correctly?

☐ Can either instance process a request safely?

☐ What dependency remains a single point of failure?

Infrastructure HA concepts to learn later

Eureka peers.

PostgreSQL primary/replica and backups.

Redis Sentinel/Cluster.

Kafka brokers/replication factor.

RabbitMQ quorum queues/cluster.

Multiple Gateways behind load balancer.

Keycloak clustering.

39. Security Audit from Client to Database

Threat-oriented reading

Threat

Where to inspect

User changes price/amount

Order/Payment backend trusted calculation

User reads another ticket

Ownership authorization

Fake admin role

JWT validation/role mapping; never trust body role

SQL injection

JPA/prepared queries; custom native query review

Secret leak

Git/config/logs/Docker image

Token leak

Logs/browser/storage/URLs

Mass assignment

DTO mapping; protected fields not accepted

Replay/duplicate request

Idempotency

DoS/flash burst

Gateway/rate limiting/waiting room

Internal service bypass

Network exposure + service authorization

Read every permitAll

Search for permitAll, disabled CSRF, wildcard CORS, anonymous routes, default passwords, and hardcoded secrets. Each occurrence needs an intentional reason.

40. AI-Generated Code Audit

This chapter is specifically for code written by AI. Working happy-path behavior is not enough. Search for signs of incomplete or locally convenient implementation.

Search repository

TODOFIXMEHACKTEMPPLACEHOLDERUnsupportedOperationExceptionreturn nulldummymockfakeexamplelocalhost127.0.0.1synchronizedstatic MapConcurrentHashMapcatch (ExceptionpermitAll

Common AI failure patterns

☐ Controller returns success before durable work completed.

☐ Exception caught and ignored.

☐ Retry added to unsafe payment call.

☐ Redis used as only durable truth.

☐ Local in-memory Map used for idempotency or distributed state.

☐ Java synchronized used as if it protected multiple instances.

☐ Kafka consumer has no duplicate handling.

☐ RabbitMQ worker acknowledges before work succeeds.

☐ DTO allows client to set server-owned status/price/userId.

☐ Entity schema and Flyway migration disagree.

☐ Test mocks the exact behavior it claims to verify.

☐ README claims a feature that code does not actually use.

☐ Unused infrastructure included for appearance.

Three-proof rule

For each major feature require: (1) implementation evidence, (2) automated/manual behavior test, and (3) runtime evidence such as DB row/message/trace/log. If one is missing, treat the feature as not fully understood.

41. End-to-End Request and Event Tracing Exercises

These exercises are the fastest way to turn an AI-built project into your own knowledge.

Exercise A: Browse event

Browser -> Gateway -> Event Service -> Event Repository -> PostgreSQL -> response

☐ Find every class touched.

☐ Find SQL/query produced.

☐ Find security rule.

☐ Find trace/log entries.

Exercise B: Successful purchase

Login -> Event -> Inventory reservation -> Order creation -> Payment -> PaymentCompleted Kafka -> Order confirmed -> OrderConfirmed Kafka -> Notification -> RabbitMQ ticket job -> Ticket Worker -> MinIO

☐ Write every HTTP endpoint.

☐ Write every database table changed.

☐ Write every Redis key changed.

☐ Write every Kafka topic/event.

☐ Write every RabbitMQ queue.

☐ Write every state transition.

☐ Write failure behavior at every arrow.

Exercise C: Failed payment

Repeat the same trace and prove exactly how inventory is released and why no ticket is generated.

Exercise D: duplicate callback

Send PaymentCompleted/callback twice. Trace the second execution and prove it cannot double-confirm, double-charge, or double-generate.

Exercise E: restart

Kill Order Service during checkout, restart it, and explain what durable state/event causes the workflow to recover or what manual reconciliation is needed.

42. Debugging Playbook

Start with the symptom, then move inward

Symptom

First places to check

404 at Gateway

Route predicate/path rewrite, Eureka service name, controller mapping

401/403

Token issuer/expiry/roles, gateway/service security rules

503 from Gateway

Eureka registration, health, network connectivity

DB error

Config URL/credentials, Flyway, connection pool, SQL constraints

Redis reservation missing

TTL, key pattern, serialization, Redis connectivity

Kafka event missing

Producer logs, topic, serialization, broker, outbox/Debezium

Kafka consumer not reacting

Group/topic, deserialization, offset, exception/retry

Rabbit job stuck

Queue depth, consumer count, ack/nack, DLQ

Order/payment mismatch

Event sequence, idempotency, transaction boundaries, reconciliation

Slow request

Trace spans, DB latency, downstream HTTP, Redis, JVM, queue lag

Debug order

→ Reproduce consistently.

→ Record traceId/orderId/eventId.

→ Check Gateway/HTTP status.

→ Check service health.

→ Check logs for the same trace/entity ID.

→ Check distributed trace.

→ Check database state.

→ Check Redis state.

→ Check Kafka/RabbitMQ state.

→ Only then inspect code at the failing boundary.

43. What You Should Be Able to Explain in an Interview

Architecture

☐ Why microservices instead of one monolith for this learning project?

☐ Why Eureka before Kubernetes?

☐ Why Gateway?

☐ Why Config Server?

☐ Why Keycloak?

☐ Why separate service databases?

☐ Why Kafka and RabbitMQ are used for different jobs?

☐ Why Redis is not the source of truth?

☐ Why outbox/Debezium?

☐ Why idempotency is required?

Failure questions

☐ What happens if Payment Service is down?

☐ What happens if Kafka is down?

☐ What happens if Redis loses data?

☐ How do you prevent overselling?

☐ How do you prevent double payment?

☐ What happens if the same Kafka event is delivered twice?

☐ How do you roll back a bad deployment?

☐ How do you trace one failed checkout across services?

Strong explanation format

Answer with: requirement → chosen mechanism → exact flow → failure handling → tradeoff → how you tested it. Avoid just listing technologies.

44. Suggested 30-Day Learning Schedule

Days

Focus

Deliverable

1-2

Architecture + repo map

Architecture drawn from memory

3-4

Java + Maven + Spring startup

Explain boot sequence and dependencies

5

Config + profiles + secrets

Property/source map

6

Eureka

Trace registration and discovery

7

Gateway

Trace one routed request

8-9

Keycloak/Security

Login/token/role flow explained

10

User Service

Controller-to-DB trace

11

Event Service

Organizer create/update flow

12-13

PostgreSQL/JPA/Flyway

Schema/migration map

14-15

Inventory + Redis

Prove no oversell in small concurrency test

16

Order state machine

All transitions diagrammed

17

Idempotency

Duplicate tests explained

18

Payment + Resilience4j

Failure/timeout demo

19-20

Kafka

Event catalog + consumer groups

21

Outbox + Debezium

Dual-write problem explained

22

RabbitMQ + Notification + Ticket

Job lifecycle trace

23

MinIO + Search + Audit

Projection/storage roles explained

24

Observability

One full distributed trace

25

Docker/Compose

Explain networks/volumes/ports

26

Testing

Read and improve critical tests

27

CI/CD

Pipeline explained step by step

28

VM deployment

Staging topology and deploy flow

29

AI audit/security audit

Remove/understand suspicious patterns

30

Full system oral exam

Explain successful + failed purchase without notes

45. Final Ownership Checklist and Glossary

You own the codebase when you can do all of these without AI

☐ Build every service and explain the build lifecycle.

☐ Start the platform and explain startup order.

☐ Draw the full architecture from memory.

☐ Explain one HTTP request from Gateway to database.

☐ Explain login/token validation and role authorization.

☐ Explain Eureka registration/discovery.

☐ Explain Config Server/property sources.

☐ Explain every service ownership boundary.

☐ Explain JPA/Hibernate/Flyway responsibilities.

☐ Explain inventory correctness under concurrency.

☐ Explain Redis key patterns and TTLs.

☐ Explain the complete order state machine.

☐ Explain and demonstrate idempotency.

☐ Explain Payment Service trusted amount and duplicate safety.

☐ Explain Resilience4j timeout/retry/circuit breaker behavior.

☐ List every Kafka event, producer, consumer, and consumer group.

☐ Explain outbox and Debezium from database commit to Kafka.

☐ Explain every RabbitMQ queue and acknowledgement strategy.

☐ Explain ticket generation and MinIO storage.

☐ Explain OpenSearch as a projection, not source of truth.

☐ Explain audit log vs application log.

☐ Explain eventual consistency and compensation.

☐ Explain waiting room vs inventory correctness.

☐ Find a request in logs/traces using traceId.

☐ Explain Prometheus/Loki/Tempo/Grafana roles.

☐ Explain every Dockerfile and Compose network.

☐ Explain test layers and what each proves.

☐ Explain CI stages and what blocks a bad build.

☐ Deploy a versioned image to your VM lab.

☐ Run two instances of one service and prove correctness.

☐ Kill a dependency and predict/reproduce system behavior.

☐ Roll back a bad service version.

☐ Find and remove or justify every suspicious AI-generated stub/hardcode.

☐ Explain the full successful checkout without looking at code.

☐ Explain the full failed-payment checkout without looking at code.

Core glossary

Term

Meaning

ACID

Database transaction properties: atomicity, consistency, isolation, durability.

Aggregate

Domain consistency boundary manipulated as a unit.

At-least-once delivery

A message may be delivered more than once; consumers must tolerate duplicates.

Backpressure

Controlling incoming work so downstream systems are not overwhelmed.

Bean

Object managed by Spring container.

Circuit breaker

Stops repeated calls to a failing dependency temporarily.

Consumer group

Kafka consumers that share work for a topic/partitions as one logical consumer.

Correlation/Trace ID

Identifier used to connect work across services.

DTO

Data Transfer Object used at boundaries.

Eventual consistency

Different services may temporarily disagree but converge through events/workflows.

Eureka

Service registry/discovery component.

Flyway

Versioned database migration tool.

Idempotency

Repeating an operation produces no additional business effect.

JWT

Signed token format carrying claims.

Offset

Kafka consumer position in a partition.

Optimistic locking

Detect conflicting concurrent updates, often using a version column.

Outbox

Database table storing events in the same transaction as business changes.

P95/P99

Latency percentile values; 95%/99% of requests are at or below the value.

Projection

Derived read model such as OpenSearch index.

Saga

Multi-service workflow using local transactions and compensation.

Source of truth

Authoritative durable system for a piece of data.

TTL

Time to live; automatic expiry duration.

WAL

PostgreSQL write-ahead log used for durability/CDC.

Final oral exam

Close your IDE. On a blank page, draw DropZone and explain a successful purchase, a failed payment, a duplicate payment callback, a service restart, and a 50,000-user flash sale. Then reopen the code and correct anything you got wrong. Repeat until your explanation matches the implementation.

Appendix A. Per-Service Code Review Worksheet

Copy this worksheet for each service while you study.

Service name: ______________________________________________________________________

Business responsibility: ______________________________________________________________________

Public endpoints: ______________________________________________________________________

Internal endpoints: ______________________________________________________________________

Database/schema: ______________________________________________________________________

Redis keys: ______________________________________________________________________

Kafka produced events: ______________________________________________________________________

Kafka consumed events: ______________________________________________________________________

RabbitMQ queues: ______________________________________________________________________

HTTP dependencies: ______________________________________________________________________

Security roles: ______________________________________________________________________

Main configuration: ______________________________________________________________________

Transaction boundaries: ______________________________________________________________________

Idempotency mechanism: ______________________________________________________________________

Failure/retry behavior: ______________________________________________________________________

Health/metrics: ______________________________________________________________________

Important tests: ______________________________________________________________________

What breaks if service is down: ______________________________________________________________________

What I still do not understand: ______________________________________________________________________

Appendix B. End-to-End Flow Worksheet

Step

Component

Code entry point

Data changed

Message/event

Failure behavior

1

Gateway

2

Service controller

3

Business service

4

Repository/DB

5

Redis

6

Kafka producer

7

Kafka consumer

8

RabbitMQ

9

Worker/MinIO

10

Final response/state

Appendix C. Questions to Ask AI Without Letting AI Replace Learning

Use AI as a tutor after you first inspect the code. Ask questions that force precise explanation rather than generation.

Explain only this method. Identify every side effect and transaction boundary.

What assumptions does this code make about running one instance vs multiple instances?

Show me the exact path by which this controller request reaches PostgreSQL.

If this Kafka event is delivered twice, what exact lines prevent a duplicate business effect?

What happens if the process crashes after this database save but before the next line?

Which fields in this DTO should never be trusted from the client?

What test would prove this method is safe under concurrency?

What code here is framework magic vs code we wrote?

What would break if I removed this dependency?

Give me a quiz about this file; do not give the answers until I answer.


---

# 📌 APPENDIX: DropZone Verified Architecture & Platform Setup (Fact-Checked)

This appendix reflects the exact, verified technical implementation of the DropZone platform in this repository.

## 1. Microservices Map & Assigned Ports

| Microservice | Port | Database / Persistence | Primary Responsibilities |
|---|---|---|---|
| `api-gateway` | `8080` | None (Keycloak Client) | Ingress routing, central JWT validation, rate limiting |
| `user-service` | `8081` | PostgreSQL (`V1__init_user_schema.sql`) | User accounts, profiles, registration |
| `inventory-service` | `8082` | PostgreSQL + Redis ZSet/Lua | Stock reservations, flash sale waiting room, event listeners |
| `order-service` | `8083` | PostgreSQL (`V1-V3`) + Outbox | Order state machine, transactional outbox pattern |
| `event-service` | `8084` | PostgreSQL + MinIO | Event catalogs, ticket categories, image uploads |
| `payment-service` | `8085` | PostgreSQL (`V1-V2`) | Payment processing, Resilience4j fault tolerance |
| `notification-service` | `8086` | PostgreSQL (`V1__init_notification_schema.sql`) + MinIO | Async PDF ticket compilation, QR codes, Email/SMS workers |
| `audit-service` | `8087` | PostgreSQL (`V1__init_audit_schema.sql`) | Kafka domain event activity logging & paged querying |
| `search-service` | `8088` | OpenSearch / Elasticsearch | Event catalog search indexing |
| `eureka-server` | `8761` | In-Memory Registry | Dynamic service discovery & heartbeats |
| `config-server` | `8888` | Git (`dropzone-config-repo`) | Centralized external configuration |

---

## 2. Staging VM Lab Topology (4 Ubuntu 24.04 VMs)

| Node | IP Address | Deployed Services & Containers |
|---|---|---|
| `edge-01` | `192.168.56.10` | NGINX Ingress Proxy (`:80`), API Gateway (`:8080`) |
| `app-01` | `192.168.56.11` | `user-service:8081`, `event-service:8084`, `inventory-service:8082`, `order-service-scaled:8083` |
| `app-02` | `192.168.56.12` | `order-service:8083`, `payment-service:8085`, `notification-service:8086` |
| `infra-01` | `192.168.56.13` | Eureka (`:8761`), Config Server (`:8888`), Postgres (`:5432`), Redis (`:6379`), Kafka (`:9092`), RabbitMQ (`:5672`), Keycloak (`:8089`), MinIO (`:9000`) |

---

## 3. Verified Architecture & Design Patterns

1. **PostgreSQL Persistence across All Services**:
   - `audit-service` & `notification-service` use PostgreSQL + Flyway migrations (`V1__init_audit_schema.sql`, `V1__init_notification_schema.sql`).
2. **Automated Inventory Confirmation & Release**:
   - `InventoryEventListener` listens to Kafka `payment-events` (`PaymentCompleted` -> `confirmReservation`, `PaymentFailed` -> `cancelReservation`) and `order-events` (`OrderConfirmed` -> `confirmReservation`, `OrderCancelled` -> `cancelReservation`).
3. **Real Concurrent Flash Sale Concurrency**:
   - `simulateFlashSale()` executes real multi-threaded worker pools with atomic Redis decrements (`opsForValue().decrement`) and `CountDownLatch` coordination.
4. **Global Exception Handling & Validation**:
   - All 8 microservices expose `@RestControllerAdvice` (`GlobalExceptionHandler`) returning standardized `ErrorResponse` DTOs with Bean Validation (`@Valid`, `@NotBlank`, `@NotNull`, `@Min`, `@DecimalMin`).
5. **Dynamic Ticket Workers**:
   - `TicketWorker` and `JobPayload` dynamically construct PDF tickets and QR codes from real event data, category names, seat numbers, and timestamps.
6. **GitHub Actions CI/CD Pipeline**:
   - `.github/workflows/ci.yml` uses Java 21 (`actions/setup-java@v5`), Maven wrapper (`./mvnw`), Testcontainers, Pact, Trivy security scanning, Docker build, and Ansible deployment.
