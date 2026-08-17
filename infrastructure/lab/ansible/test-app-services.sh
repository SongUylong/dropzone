#!/usr/bin/env bash

# Test & Verify Application Microservice Containers on app-01 & app-02 (Step 10)

set -e

APP1_IP="192.168.56.11"
APP2_IP="192.168.56.12"

echo "================================================================="
echo "VERIFYING APPLICATION SERVICES ON APP-01 AND APP-02"
echo "================================================================="

check_app_health() {
  local name="$1"
  local ip="$2"
  local port="$3"
  printf "  %-22s (%s:%s) ... " "$name" "$ip" "$port"
  if curl -s -f --connect-timeout 3 "http://$ip:$port/actuator/health" >/dev/null 2>&1; then
    echo "[✓] UP"
    return 0
  else
    echo "[✗] DOWN / UNREACHABLE"
    return 1
  fi
}

echo ""
echo "app-01 (192.168.56.11):"
check_app_health "User Service" "$APP1_IP" "8081"
check_app_health "Event Service" "$APP1_IP" "8084"
check_app_health "Inventory Service" "$APP1_IP" "8082"

echo ""
echo "app-02 (192.168.56.12):"
check_app_health "Order Service" "$APP2_IP" "8083"
check_app_health "Payment Service" "$APP2_IP" "8085"
check_app_health "Notification Service" "$APP2_IP" "8086"

echo "================================================================="
echo "APPLICATION SERVICES HEALTH CHECK COMPLETE"
echo "================================================================="
