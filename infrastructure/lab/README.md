# Dropzone VM Staging Lab Infrastructure

This directory contains the Vagrant and Ansible automation for deploying the Dropzone microservices platform across a multi-node VM staging lab using VMware Workstation / Fusion and Ubuntu Server 24.04 LTS.

## VM Staging Topology

| VM Name | IP Address | OS | Hosted Services / Infrastructure |
|---------|------------|----|-----------------------------------|
| **`edge-01`** | `192.168.56.10` | Ubuntu Server 24.04 LTS | NGINX, API Gateway |
| **`app-01`**  | `192.168.56.11` | Ubuntu Server 24.04 LTS | `user-service`, `event-service`, `inventory-service` |
| **`app-02`**  | `192.168.56.12` | Ubuntu Server 24.04 LTS | `order-service`, `payment-service`, `notification-service` |
| **`infra-01`** | `192.168.56.13` | Ubuntu Server 24.04 LTS | Eureka Server, Config Server, Keycloak, PostgreSQL, Redis, Kafka, RabbitMQ |

## Directory Structure

```text
infrastructure/lab/
├── vagrant/     # Multi-VM Vagrantfile and provider configuration
├── ansible/     # Provisioning playbooks, inventory, and role configurations
└── README.md    # Lab architecture documentation
```
