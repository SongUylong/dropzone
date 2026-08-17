#!/usr/bin/env bash

# Run Ansible Provisioning Playbook for Dropzone Staging Lab

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if ! command -v ansible-playbook >/dev/null 2>&1; then
  echo "[ERROR] ansible-playbook is not installed."
  echo "Refer to docs/STEP_30_LAB_SETUP.md for installation commands."
  exit 1
fi

echo "================================================================="
echo "RUNNING DROPZONE ANSIBLE PROVISIONING PLAYBOOK"
echo "================================================================="

ansible-playbook -i inventory.ini site.yml "$@"

echo "================================================================="
echo "ANSIBLE PROVISIONING COMPLETED"
echo "================================================================="
