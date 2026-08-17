#!/usr/bin/env bash

# Dropzone Private Network Connectivity Verification Test (Step 4)

set -e

HOSTS=(
  "edge-01:192.168.56.10"
  "app-01:192.168.56.11"
  "app-02:192.168.56.12"
  "infra-01:192.168.56.13"
)

echo "================================================================="
echo "VERIFYING DROPZONE PRIVATE NETWORK (192.168.56.0/24)"
echo "================================================================="

echo ""
echo "Phase 1: Host -> VM Ping Reachability Test"
for entry in "${HOSTS[@]}"; do
  name="${entry%%:*}"
  ip="${entry#*:}"
  printf "  Checking Host -> %-10s (%s) ... " "$name" "$ip"
  if ping -c 1 -W 2 "$ip" >/dev/null 2>&1; prefix="[✓]"; then
    echo "$prefix OK"
  else
    echo "[✗] UNREACHABLE (Is vagrant up running?)"
  fi
done

echo ""
echo "Phase 2: Inter-VM Mesh Connectivity Matrix (Vagrant SSH)"
if command -v vagrant >/dev/null 2>&1; then
  cd "$(dirname "${BASH_SOURCE[0]}")"
  for source_entry in "${HOSTS[@]}"; do
    src_name="${source_entry%%:*}"
    for target_entry in "${HOSTS[@]}"; do
      target_name="${target_entry%%:*}"
      target_ip="${target_entry#*:}"
      if [ "$src_name" != "$target_name" ]; then
        printf "  Testing %-10s -> %-10s (%s) ... " "$src_name" "$target_name" "$target_ip"
        if vagrant ssh "$src_name" -c "ping -c 1 -W 2 $target_ip" >/dev/null 2>&1; then
          echo "[✓] CONNECTED"
        else
          echo "[✗] FAILED"
        fi
      fi
    done
  done
else
  echo "  [i] Vagrant CLI not detected. Run when Vagrant is installed and VMs are up."
fi

echo ""
echo "================================================================="
echo "NETWORK MATRIX CHECK COMPLETE"
echo "================================================================="
