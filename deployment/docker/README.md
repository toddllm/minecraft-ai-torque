# Docker Deployment for AI Torque

## Prerequisites

```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt install docker-compose-plugin
```

## Deployment

```bash
# 1. Copy server directories to docker folder
cp -r ~/aitorque-survival deployment/docker/survival
cp -r ~/aitorque-creative deployment/docker/creative

# 2. Start containers
cd deployment/docker
docker-compose up -d

# 3. View logs
docker-compose logs -f survival
docker-compose logs -f creative
```

## Management

```bash
# Start/stop services
docker-compose start
docker-compose stop
docker-compose restart

# Restart specific server
docker-compose restart survival

# View logs
docker-compose logs -f survival
docker-compose logs --tail=100 creative

# Execute commands in container
docker exec -it aitorque-survival bash

# Check status
docker-compose ps
```

## Backups

```bash
# Backup script
docker-compose stop
tar -czf backup-$(date +%Y%m%d).tar.gz survival creative
docker-compose start
```

## Updates

```bash
# Update plugin
docker-compose stop survival
cp ~/aitorque/aitorque-plugin/target/AITorque-1.0.0-ALPHA.jar survival/plugins/
docker-compose start survival
```
