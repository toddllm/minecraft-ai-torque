#!/bin/bash
# AI Torque Backup Script
# Add to cron: 0 */6 * * * /opt/minecraft/backup.sh

BACKUP_DIR="/opt/minecraft/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=7

mkdir -p $BACKUP_DIR

# Backup survival server
echo "Backing up survival server..."
tar -czf "$BACKUP_DIR/survival_$TIMESTAMP.tar.gz" \
    -C /opt/minecraft/aitorque-survival \
    world world_nether world_the_end plugins server.properties

# Backup creative server
echo "Backing up creative server..."
tar -czf "$BACKUP_DIR/creative_$TIMESTAMP.tar.gz" \
    -C /opt/minecraft/aitorque-creative \
    world world_nether world_the_end plugins server.properties

# Remove old backups
find $BACKUP_DIR -name "*.tar.gz" -mtime +$RETENTION_DAYS -delete

echo "Backup complete: $TIMESTAMP"
