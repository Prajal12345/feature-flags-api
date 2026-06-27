#!/usr/bin/env bash
set -euo pipefail

# One-time setup for a fresh Ubuntu Droplet.
# Run as root: curl -fsSL ... | bash   OR   bash droplet-setup.sh

if [[ "${EUID}" -ne 0 ]]; then
  echo "Run this script as root."
  exit 1
fi

echo "Installing Git and Docker..."
apt-get update
apt-get install -y git
curl -fsSL https://get.docker.com | sh
systemctl enable docker
systemctl start docker

echo "Configuring firewall..."
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

mkdir -p /opt/feature-flags-api

echo "Droplet setup complete."
echo "Next:"
echo "  1. Add your GitHub Actions SSH public key to /root/.ssh/authorized_keys"
echo "  2. Add GitHub secrets: DROPLET_HOST, DROPLET_USER, DROPLET_SSH_KEY"
echo "  3. Push to main to trigger deployment"
