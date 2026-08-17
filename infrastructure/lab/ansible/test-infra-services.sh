#!/usr/bin/env bash

# Test & Verify Infrastructure Services on infra-01 (192.168.56.13) (Step 8)

set -e

INFRA_IP="192.168.56.13"

echo "================================================================="
echo "VERIFYING INFRASTRUCTURE SERVICES ON INFRA-01 ($INFRA_IP)"
echo "================================================================="

check_service() {
  local name="$1"
  local port="$2"
  local path="${3:-}"
  printf "  %-20s (Port %-5s) ... " "$name" "$port"
  if nc -z -w 3 "$INFRA_IP" "$port" >/dev/null 2>&1; then
    echo "[✓] UP"
    return 0
  else
    echo "[✗] DOWN / UNREACHABLE"
    return 1
  fi
}

check_service "Eureka Server" "8761"
check_service "Config Server" "8888"
check_service "PostgreSQL" "5432"
check_service "Redis" "6379"
check_service "Kafka Broker" "9092"
check_service "RabbitMQ" "5672"
check_service "Keycloak IAM" "8089"
check_service "MinIO Storage" "9000"

echo "================================================================="
echo "INFRA-01 HEALTH CHECK COMPLETE"
echo "================================================================="
