#!/usr/bin/env bash

# Setup Dropzone Fake Domains in Host /etc/hosts (Step 13)

set -e

EDGE_IP="192.168.56.10"

HOSTS_ENTRIES=(
  "$EDGE_IP api.dropzone.local"
  "$EDGE_IP auth.dropzone.local"
  "$EDGE_IP eureka.dropzone.local"
  "$EDGE_IP rabbit.dropzone.local"
)

echo "================================================================="
echo "CONFIGURING DROPZONE STAGING FAKE DOMAINS IN HOST /etc/hosts"
echo "================================================================="

echo ""
echo "Entries to be added/verified:"
for entry in "${HOSTS_ENTRIES[@]}"; do
  echo "  $entry"
done

echo ""
if grep -q "api.dropzone.local" /etc/hosts 2>/dev/null; then
  echo "[✓] Dropzone fake domain entries already exist in /etc/hosts."
else
  echo "[i] Command to append entries to /etc/hosts on Linux/macOS host:"
  echo ""
  echo "cat << 'EOF' | sudo tee -a /etc/hosts"
  echo "# Dropzone Staging VM Lab Fake Domains"
  for entry in "${HOSTS_ENTRIES[@]}"; do
    echo "$entry"
  done
  echo "EOF"
  echo ""
fi

echo "================================================================="
echo "SETUP HOSTS INSTRUCTION COMPLETE"
echo "================================================================="
