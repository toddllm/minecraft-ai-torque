#!/bin/bash
# AI Torque Minecraft Server Deployment Script
# Usage: ./deploy.sh [user@]hostname

set -e

SERVER=$1
if [ -z "$SERVER" ]; then
    echo "Usage: $0 [user@]hostname"
    echo "Example: $0 user@192.168.1.100"
    exit 1
fi

echo "🚀 Deploying AI Torque to $SERVER..."

# Create directory structure on remote server
echo "📁 Creating directories..."
ssh $SERVER 'sudo mkdir -p /opt/minecraft/{aitorque-survival,aitorque-creative}'
ssh $SERVER 'sudo useradd -r -m -U -d /opt/minecraft -s /bin/bash minecraft || true'

# Copy server files
echo "📦 Copying survival server..."
rsync -avz --progress ~/aitorque-survival/ $SERVER:/tmp/aitorque-survival/
ssh $SERVER 'sudo mv /tmp/aitorque-survival/* /opt/minecraft/aitorque-survival/'

echo "📦 Copying creative server..."
rsync -avz --progress ~/aitorque-creative/ $SERVER:/tmp/aitorque-creative/
ssh $SERVER 'sudo mv /tmp/aitorque-creative/* /opt/minecraft/aitorque-creative/'

# Set permissions
echo "🔒 Setting permissions..."
ssh $SERVER 'sudo chown -R minecraft:minecraft /opt/minecraft'
ssh $SERVER 'sudo chmod -R 755 /opt/minecraft'

# Install systemd services
echo "⚙️  Installing systemd services..."
scp deployment/systemd/aitorque-survival.service $SERVER:/tmp/
scp deployment/systemd/aitorque-creative.service $SERVER:/tmp/
ssh $SERVER 'sudo mv /tmp/aitorque-*.service /etc/systemd/system/'
ssh $SERVER 'sudo systemctl daemon-reload'

# Enable and start services
echo "▶️  Starting services..."
ssh $SERVER 'sudo systemctl enable aitorque-survival aitorque-creative'
ssh $SERVER 'sudo systemctl start aitorque-survival aitorque-creative'

# Check status
echo "✅ Deployment complete! Checking status..."
ssh $SERVER 'sudo systemctl status aitorque-survival --no-pager'
ssh $SERVER 'sudo systemctl status aitorque-creative --no-pager'

echo ""
echo "📊 Useful commands on the server:"
echo "  sudo systemctl status aitorque-survival"
echo "  sudo systemctl restart aitorque-creative"
echo "  sudo journalctl -u aitorque-survival -f"
echo "  sudo journalctl -u aitorque-creative -f"
