package com.aitorque.abilities;

import com.aitorque.AITorquePlugin;
import com.aitorque.entity.AITorqueEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.*;

/**
 * Healing Tower System - Spawns towers that heal AI Torque
 */
public class HealingTowerAbility {

    private final AITorquePlugin plugin;
    private final AITorqueEntity entity;
    private final Map<Location, TowerData> towers;
    private boolean active;

    private static class TowerData {
        Location location;
        ArmorStand marker;
        long spawnTime;
        boolean destroyed;

        TowerData(Location location, ArmorStand marker) {
            this.location = location;
            this.marker = marker;
            this.spawnTime = System.currentTimeMillis();
            this.destroyed = false;
        }
    }

    public HealingTowerAbility(AITorquePlugin plugin, AITorqueEntity entity) {
        this.plugin = plugin;
        this.entity = entity;
        this.towers = new HashMap<>();
        this.active = false;
    }

    /**
     * Spawn healing towers around AI Torque
     */
    public void spawnTowers() {
        if (!plugin.getConfigManager().areHealingTowersEnabled()) return;

        this.active = true;
        int maxTowers = plugin.getConfigManager().getMaxHealingTowers();

        Location center = entity.getLocation();

        for (int i = 0; i < maxTowers; i++) {
            // Spawn towers in a circle around AI Torque
            double angle = (2 * Math.PI / maxTowers) * i;
            double radius = 20;

            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            double y = center.getY();

            Location towerLoc = new Location(center.getWorld(), x, y, z);

            // Find ground
            Block ground = towerLoc.getWorld().getHighestBlockAt(towerLoc);
            towerLoc.setY(ground.getY() + 1);

            spawnTower(towerLoc);
        }

        entity.sendChatMessage("§6[AI Torque] §bHealing towers activated!");
    }

    /**
     * Spawn individual tower
     */
    private void spawnTower(Location location) {
        // Build tower structure
        buildTowerStructure(location);

        // Spawn marker
        ArmorStand marker = (ArmorStand) location.getWorld().spawnEntity(
                location.clone().add(0, 2, 0),
                EntityType.ARMOR_STAND
        );

        marker.setCustomName("§b§lHealing Tower");
        marker.setCustomNameVisible(true);
        marker.setGravity(false);
        marker.setVisible(false);
        marker.setInvulnerable(false);

        // Visual effect
        location.getWorld().spawnParticle(
                Particle.HEART,
                location.clone().add(0, 2, 0),
                50,
                1, 2, 1,
                0
        );

        location.getWorld().playSound(
                location,
                Sound.BLOCK_BEACON_ACTIVATE,
                2.0f,
                1.5f
        );

        towers.put(location, new TowerData(location, marker));
    }

    /**
     * Build physical tower structure
     */
    private void buildTowerStructure(Location base) {
        // Simple pillar tower
        for (int y = 0; y < 5; y++) {
            Block block = base.clone().add(0, y, 0).getBlock();
            block.setType(Material.BEACON);

            // Surround with glass
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || z != 0) {
                        Block wall = base.clone().add(x, y, z).getBlock();
                        if (y == 0) {
                            wall.setType(Material.OBSIDIAN);
                        } else {
                            wall.setType(Material.GLASS);
                        }
                    }
                }
            }
        }
    }

    /**
     * Heal AI Torque from towers
     */
    public void healFromTowers() {
        if (!active) return;

        double healRate = plugin.getConfigManager().getDrainRate(); // Reuse config
        double totalHeal = 0;

        // Clean up destroyed towers
        towers.entrySet().removeIf(entry -> {
            TowerData data = entry.getValue();
            if (!data.marker.isValid() || data.destroyed) {
                data.destroyed = true;
                return true;
            }
            return false;
        });

        // Heal from each active tower
        for (TowerData data : towers.values()) {
            if (!data.destroyed && data.marker.isValid()) {
                totalHeal += healRate;

                // Visual healing beam
                if (entity.getTickCounter() % 20 == 0) {
                    drawHealingBeam(data.location, entity.getLocation());
                }
            }
        }

        if (totalHeal > 0) {
            entity.heal(totalHeal);
        }
    }

    /**
     * Draw healing beam particle effect
     */
    private void drawHealingBeam(Location from, Location to) {
        double distance = from.distance(to);
        int particles = (int) (distance * 2);

        for (int i = 0; i < particles; i++) {
            double ratio = (double) i / particles;

            double x = from.getX() + (to.getX() - from.getX()) * ratio;
            double y = from.getY() + 2 + (to.getY() - from.getY()) * ratio;
            double z = from.getZ() + (to.getZ() - from.getZ()) * ratio;

            Location particleLoc = new Location(from.getWorld(), x, y, z);

            from.getWorld().spawnParticle(
                    Particle.HEART,
                    particleLoc,
                    1,
                    0, 0, 0,
                    0
            );
        }
    }

    /**
     * Respawn destroyed towers
     */
    public void respawnDestroyedTowers() {
        int maxTowers = plugin.getConfigManager().getMaxHealingTowers();
        int currentTowers = towers.size();

        if (currentTowers < maxTowers) {
            int towersToSpawn = maxTowers - currentTowers;

            Location center = entity.getLocation();

            for (int i = 0; i < towersToSpawn; i++) {
                // Random location around AI Torque
                double angle = Math.random() * 2 * Math.PI;
                double radius = 15 + Math.random() * 10;

                double x = center.getX() + radius * Math.cos(angle);
                double z = center.getZ() + radius * Math.sin(angle);

                Location towerLoc = new Location(
                        center.getWorld(),
                        x,
                        center.getWorld().getHighestBlockYAt((int) x, (int) z) + 1,
                        z
                );

                spawnTower(towerLoc);
            }

            if (towersToSpawn > 0) {
                entity.sendChatMessage("§6[AI Torque] §bHealing towers restored!");
            }
        }
    }

    /**
     * Get tower count
     */
    public int getTowerCount() {
        return towers.size();
    }

    /**
     * Clean up all towers
     */
    public void cleanup() {
        for (TowerData data : towers.values()) {
            if (data.marker.isValid()) {
                data.marker.remove();
            }

            // Remove tower structure
            for (int y = 0; y < 5; y++) {
                data.location.clone().add(0, y, 0).getBlock().setType(Material.AIR);

                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        data.location.clone().add(x, y, z).getBlock().setType(Material.AIR);
                    }
                }
            }
        }

        towers.clear();
    }
}
