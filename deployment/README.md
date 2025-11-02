# AI Torque Linux Server Deployment

## Prerequisites

Your Linux server needs:
- Ubuntu 20.04+ or Debian 11+ (or RHEL/CentOS 8+)
- Java 21+ installed
- 16GB+ RAM (for both servers)
- Ports 25565 and 25566 open in firewall

## Installation

### 1. Prepare Linux Server

```bash
# Install Java 21
sudo apt update
sudo apt install openjdk-21-jre-headless

# Configure firewall
sudo ufw allow 25565/tcp comment 'Minecraft Survival'
sudo ufw allow 25566/tcp comment 'Minecraft Creative'
sudo ufw enable
```

### 2. Deploy from Mac

```bash
cd ~/aitorque/deployment
chmod +x deploy.sh
./deploy.sh user@your-server-ip
```

### 3. Manage Services

```bash
# Start/stop/restart servers
sudo systemctl start aitorque-survival
sudo systemctl stop aitorque-creative
sudo systemctl restart aitorque-survival

# View logs (real-time)
sudo journalctl -u aitorque-survival -f
sudo journalctl -u aitorque-creative -f

# View recent logs
sudo journalctl -u aitorque-survival -n 100

# Check status
sudo systemctl status aitorque-survival
sudo systemctl status aitorque-creative
```

### 4. Set Up Backups

```bash
# Copy backup script to server
scp deployment/backup.sh user@server:/tmp/
ssh user@server 'sudo mv /tmp/backup.sh /opt/minecraft/backup.sh'
ssh user@server 'sudo chmod +x /opt/minecraft/backup.sh'

# Add to cron (backup every 6 hours)
ssh user@server 'echo "0 */6 * * * /opt/minecraft/backup.sh" | sudo crontab -'
```

## Alternative: Docker Deployment

For even cleaner management, you can use Docker:

```bash
# Build and run with docker-compose
cd ~/aitorque/deployment/docker
docker-compose up -d

# View logs
docker-compose logs -f survival
docker-compose logs -f creative

# Restart
docker-compose restart survival
```

## Server Management

### systemd Commands
- `systemctl start aitorque-survival` - Start server
- `systemctl stop aitorque-survival` - Stop server
- `systemctl restart aitorque-survival` - Restart server
- `systemctl status aitorque-survival` - Check status
- `systemctl enable aitorque-survival` - Auto-start on boot
- `systemctl disable aitorque-survival` - Disable auto-start

### Logs
- Real-time: `journalctl -u aitorque-survival -f`
- Last 100 lines: `journalctl -u aitorque-survival -n 100`
- Since boot: `journalctl -u aitorque-survival -b`
- Today only: `journalctl -u aitorque-survival --since today`

### Console Access

To send commands to the server console:
```bash
# Using systemd-run
echo "say Hello players!" | sudo systemd-run --unit=aitorque-survival --pipe

# Or use screen/tmux (alternative setup)
screen -r aitorque-survival
# Ctrl+A, D to detach
```

## Monitoring

Set up basic monitoring:
```bash
# CPU/Memory usage
ps aux | grep java

# Disk usage
df -h /opt/minecraft

# Network connections
ss -tulpn | grep java
```

## Updates

To update the plugin:
```bash
# Build new version on Mac
cd ~/aitorque/aitorque-plugin
mvn clean package

# Deploy to server
scp target/AITorque-1.0.0-ALPHA.jar user@server:/tmp/
ssh user@server 'sudo systemctl stop aitorque-survival aitorque-creative'
ssh user@server 'sudo mv /tmp/AITorque-1.0.0-ALPHA.jar /opt/minecraft/aitorque-survival/plugins/'
ssh user@server 'sudo mv /tmp/AITorque-1.0.0-ALPHA.jar /opt/minecraft/aitorque-creative/plugins/'
ssh user@server 'sudo systemctl start aitorque-survival aitorque-creative'
```
