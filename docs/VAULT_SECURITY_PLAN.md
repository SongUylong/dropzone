# HashiCorp Vault Integration & Secrets Management Plan

## Overview
This document outlines the security architecture for migrating application credentials, database passwords, API tokens, and private keys from environment properties to HashiCorp Vault.

## Architecture

```
+-------------------+      Token/AppRole      +-----------------------+
|  Spring Boot      |  ------------------->   |  HashiCorp Vault      |
|  Microservice     |                         |  (kv-v2 secret engine)|
|  (order-service)  |  <-------------------   |                       |
+-------------------+      Loaded Secrets     +-----------------------+
```

## Implementation Strategy

### 1. Spring Cloud Vault Integration
Add dependency to microservices `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-vault-config</artifactId>
</dependency>
```

### 2. Vault Configuration (`bootstrap.yml`)
```yaml
spring:
  cloud:
    vault:
      host: ${VAULT_HOST:vault.dropzone.local}
      port: 8200
      scheme: https
      authentication: APPROLE
      app-role:
        role-id: ${VAULT_ROLE_ID}
        secret-id: ${VAULT_SECRET_ID}
      kv:
        enabled: true
        backend: secret
        profile-separator: '/'
        application-name: ${spring.application.name}
```

### 3. Secret Path Mapping
- Database Credentials: `secret/data/dropzone/database`
- Payment Gateway API Keys: `secret/data/dropzone/payment`
- Keycloak Client Secrets: `secret/data/dropzone/auth`

### 4. Dynamic Database Credential Rotation
Use HashiCorp Vault's PostgreSQL database secret engine to generate dynamic, short-lived database credentials (TTL 1h) per microservice execution.
