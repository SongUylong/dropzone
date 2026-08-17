# Step 2 — Lab Tools Installation Guide

This document outlines the required tools for managing the multi-node VM staging lab on your host system (Fedora Linux / RHEL / Ubuntu / macOS / Windows).

## Tool Responsibility Matrix

```text
Host System (Physical Machine / Desktop)
   │
   ├── Vagrant: Creates, starts, and destroys virtual machines (VM lifecycle management)
   │     └── Vagrant VMware Plugin: Provider adapter for VMware Workstation / Fusion
   │
   ├── Ansible: Configures VMs (installs dependencies, configures network, transfers files)
   │
   └── Docker: Runs Dropzone microservices and infrastructure containers inside the VMs
```

---

## 1. Installation Instructions for Fedora Linux / RHEL

### A. Install Ansible & Vagrant via DNF
```bash
sudo dnf install -y vagrant ansible git
```

### B. VMware Workstation & Vagrant VMware Utility Setup
1. **VMware Workstation**: Download and install VMware Workstation Pro / Player for Linux from Broadcom / VMware portal.
2. **Vagrant VMware Utility**: Download and install the Vagrant VMware Utility package from HashiCorp:
   ```bash
   # Download RPM package from HashiCorp
   wget https://releases.hashicorp.com/vagrant-vmware-utility/1.0.22/vagrant-vmware-utility_1.0.22_x86_64.rpm
   sudo dnf localinstall -y vagrant-vmware-utility_1.0.22_x86_64.rpm
   ```
3. **Vagrant VMware Desktop Plugin**:
   ```bash
   vagrant plugin install vagrant-vmware-desktop
   ```

---

## 2. Installation Instructions for Ubuntu / Debian

```bash
# 1. Update and install Ansible, Vagrant, Git
sudo apt-get update
sudo apt-get install -y vagrant ansible git

# 2. Install Vagrant VMware Utility
wget https://releases.hashicorp.com/vagrant-vmware-utility/1.0.22/vagrant-vmware-utility_1.0.22_amd64.deb
sudo dpkg -i vagrant-vmware-utility_1.0.22_amd64.deb

# 3. Install Vagrant VMware Desktop Plugin
vagrant plugin install vagrant-vmware-desktop
```

---

## 3. Verification Checklist

Run the following commands on your host terminal to verify installation:

```bash
# Check Git
git --version

# Check Docker
docker --version

# Check Vagrant
vagrant --version

# Check Ansible
ansible --version

# Check Vagrant VMware Plugin
vagrant plugin list | grep vagrant-vmware-desktop
```
