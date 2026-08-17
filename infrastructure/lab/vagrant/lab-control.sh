#!/usr/bin/env bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

command_exists() {
  command -v "$1" >/dev/null 2>&1
}

if ! command_exists vagrant; then
  echo "[ERROR] Vagrant is not installed or not in PATH."
  echo "Please refer to docs/STEP_30_LAB_SETUP.md for installation instructions."
  exit 1
fi

ACTION="${1:-status}"

case "$ACTION" in
  start|up)
    echo "=================================================="
    echo "Starting Dropzone Multi-Node VM Lab..."
    echo "=================================================="
    vagrant up
    echo ""
    echo "VM Status Summary:"
    vagrant status
    ;;
  stop|halt)
    echo "=================================================="
    echo "Stopping Dropzone Multi-Node VM Lab..."
    echo "=================================================="
    vagrant halt
    ;;
  restart|reload)
    echo "=================================================="
    echo "Reloading Dropzone Multi-Node VM Lab..."
    echo "=================================================="
    vagrant reload
    ;;
  status)
    echo "=================================================="
    echo "Dropzone Multi-Node VM Lab Status:"
    echo "=================================================="
    vagrant status
    ;;
  destroy)
    echo "=================================================="
    echo "Destroying Dropzone Multi-Node VM Lab..."
    echo "=================================================="
    vagrant destroy -f
    ;;
  ssh)
    VM_NAME="${2:-edge-01}"
    echo "Connecting SSH to $VM_NAME..."
    vagrant ssh "$VM_NAME"
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status|destroy|ssh [vm-name]}"
    exit 1
    ;;
esac
