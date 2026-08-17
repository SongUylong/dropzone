#!/usr/bin/env bash

# Test Connectivity from app-01 (192.168.56.11) & app-02 (192.168.56.12) to infra-01 (192.168.56.13) (Step 9)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

INFRA_IP="192.168.56.13"

echo "================================================================="
echo "VERIFYING APP-01 & APP-02 -> INFRA-01 REMOTE CONNECTIVITY"
echo "================================================================="

if command -v ansible-playbook >/dev/null 2>&1; then
  ansible-playbook -i inventory.ini test-app-to-infra.yml "$@"
else
  echo "[i] Ansible not installed on host. Running raw SSH verification commands..."
  if command -v vagrant >/dev/null 2>&1; then
    cd "$SCRIPT_DIR/../vagrant"
    for node in app-01 app-02; do
      echo ""
      echo "Testing from $node -> infra-01 ($INFRA_IP):"
      vagrant ssh "$node" -c "
        for port in 5432 6379 9092 5672 8761 8888 8089; do
          nc -z -w 3 $INFRA_IP \$port && echo '  Port '\$port' -> [✓] CONNECTED' || echo '  Port '\$port' -> [✗] FAILED'
        done
      "
    done
  else
    echo "[!] Neither Ansible nor Vagrant CLI found locally. Verification script ready for execution."
  fi
fi

echo "================================================================="
echo "REMOTE INFRASTRUCTURE CONNECTIVITY TEST COMPLETE"
echo "================================================================="
