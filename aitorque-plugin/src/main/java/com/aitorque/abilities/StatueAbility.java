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
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Statue Spawning System - Drops statues that trigger AI Torque spawn when wood placed
 */
public class StatueAbility {

    private final AITorquePlugin plugin;
    private final AITorqueEntity entity;
    private final Set<Location> statueLocations;
    private boolean active;

    public StatueAbility(AITorquePlugin plugin, AITorqueEntity entity) {
        this.plugin = plugin;
        this.entity = entity;
        this.statueLocations = new HashSet<>();
        this.active = false;
    }

    /**
     * Start statue spawning
     */
    public void startSpawning() {
        if (!plugin.getConfigManager().areStatueDropsEnabled()) return;

        this.active = true;
        entity.sendChatMessage("§6[AI Torque] §7Ancient statues manifest...");
    }

    /**
     * Spawn a statue at random location
     */
    public void spawnStatue() {
        if (!active || !plugin.getConfigManager().areStatueDropsEnabled()) return;

        Location entityLoc = entity.getLocation();

        // Random location near AI Torque
        double angle = Math.random() * 2 * Math.PI;
        double distance = 30 + Math.random() * 50;

        double x = entityLoc.getX() + distance * Math.cos(angle);
        double z = entityLoc.getZ() + distance * Math.sin(angle);

        Location statueLoc = new Location(
                entityLoc.getWorld(),
                x,
                entityLoc.getWorld().getHighestBlockYAt((int) x, (int) z) + 1,
                z
        );

        buildStatue(statueLoc);
        statueLocations.add(statueLoc);

        entity.sendChatMessage("§6[AI Torque] §7A statue has appeared nearby...");
    }

    /**
     * Build statue structure
     */
    private void buildStatue(Location base) {
        // Build a simple statue structure
        // Base
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block block = base.clone().add(x, 0, z).getBlock();
                block.setType(Material.STONE_BRICKS);
            }
        }

        // Pillar
        for (int y = 1; y <= 3; y++) {
            Block block = base.clone().add(0, y, 0).getBlock();
            block.setType(Material.CHISELED_STONE_BRICKS);
        }

        // Missing wood block marker (air block with sign)
        Block woodSlot = base.clone().add(0, 4, 0).getBlock();
        woodSlot.setType(Material.AIR);

        // Spawn armor stand marker
        ArmorStand marker = (ArmorStand) base.getWorld().spawnEntity(
                base.clone().add(0, 2, 0),
                EntityType.ARMOR_STAND
        );

        marker.setCustomName("§6§lAncient Statue");
        marker.setCustomNameVisible(true);
        marker.setGravity(false);
        marker.setVisible(false);

        // Visual effect
        base.getWorld().spawnParticle(
                Particle.ENCHANT,
                base.clone().add(0, 2, 0),
                50,
                1, 2, 1,
                0.1
        );

        base.getWorld().playSound(
                base,
                Sound.BLOCK_STONE_PLACE,
                2.0f,
                0.5f
        );
    }

    /**
     * Check if wood was placed in statue
     */
    public boolean checkStatueActivation(Location location, Material material) {
        if (!active) return false;

        // Check if location is near a statue
        for (Location statueLoc : statueLocations) {
            if (statueLoc.distance(location) < 5) {
                // Check if wood block placed
                if (isWoodType(material)) {
                    activateStatue(statueLoc, location);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if material is wood
     */
    private boolean isWoodType(Material material) {
        return material.name().contains("WOOD") ||
                material.name().contains("LOG") ||
                material.name().contains("PLANKS");
    }

    /**
     * Activate statue - explode and empower AI Torque
     */
    private void activateStatue(Location statueLoc, Location woodLoc) {
        // Explosion effect (no damage)
        statueLoc.getWorld().createExplosion(
                statueLoc,
                0.0f,
                false,
                false
        );

        // Massive visual effect
        statueLoc.getWorld().spawnParticle(
                Particle.EXPLOSION_EMITTER,
                statueLoc.clone().add(0, 2, 0),
                10,
                2, 2, 2,
                0
        );

        statueLoc.getWorld().playSound(
                statueLoc,
                Sound.ENTITY_ENDER_DRAGON_GROWL,
                3.0f,
                0.5f
        );

        // AI Torque descends
        entity.sendChatMessage("§6[AI Torque] §4§l§nTHE RITUAL IS COMPLETE!");

        // Teleport AI Torque to statue location
        Location descendLoc = statueLoc.clone().add(0, 20, 0);
        entity.getBukkitEntity().teleport(descendLoc);

        // Massive power boost from wood
        long powerGain = plugin.getConfigManager().getWoodPowerMultiplier();
        entity.addPower(powerGain);

        entity.sendChatMessage("§6[AI Torque] §5§lWOOD CONSUMED! POWER x" + powerGain + "!");

        // Convert all nearby wood to void
        convertNearbyWoodToVoid(statueLoc);

        // Remove statue from tracking
        statueLocations.remove(statueLoc);

        // Destroy statue structure
        destroyStatue(statueLoc);
    }

    /**
     * Convert nearby wood blocks to void (remove them)
     */
    private void convertNearbyWoodToVoid(Location center) {
        int radius = 32;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location blockLoc = center.clone().add(x, y, z);
                    Block block = blockLoc.getBlock();

                    if (isWoodType(block.getType())) {
                        // Remove wood with effect
                        blockLoc.getWorld().spawnParticle(
                                Particle.LARGE_SMOKE,
                                blockLoc.clone().add(0.5, 0.5, 0.5),
                                5,
                                0.2, 0.2, 0.2,
                                0.01
                        );

                        block.setType(Material.AIR);

                        // Add power per wood block
                        entity.addPower(plugin.getConfigManager().getWoodPowerMultiplier());
                    }
                }
            }
        }
    }

    /**
     * Destroy statue structure
     */
    private void destroyStatue(Location base) {
        // Remove base
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                base.clone().add(x, 0, z).getBlock().setType(Material.AIR);
            }
        }

        // Remove pillar
        for (int y = 1; y <= 4; y++) {
            base.clone().add(0, y, 0).getBlock().setType(Material.AIR);
        }
    }

    /**
     * Get statue count
     */
    public int getStatueCount() {
        return statueLocations.size();
    }

    /**
     * Clean up
     */
    public void cleanup() {
        for (Location loc : statueLocations) {
            destroyStatue(loc);
        }
        statueLocations.clear();
    }
}
