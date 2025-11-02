package com.aitorque.abilities;

import com.aitorque.AITorquePlugin;
import com.aitorque.entity.AITorqueEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;

import java.util.*;

/**
 * Cloning System - Spawns clones that generate Void energy
 */
public class CloningAbility {

    private final AITorquePlugin plugin;
    private final AITorqueEntity entity;
    private final Map<UUID, CloneData> clones;
    private boolean active;

    private static class CloneData {
        Entity cloneEntity;
        double voidEnergy;
        Location spawnLocation;

        CloneData(Entity entity, Location location) {
            this.cloneEntity = entity;
            this.voidEnergy = 0;
            this.spawnLocation = location;
        }
    }

    public CloningAbility(AITorquePlugin plugin, AITorqueEntity entity) {
        this.plugin = plugin;
        this.entity = entity;
        this.clones = new HashMap<>();
        this.active = false;
    }

    /**
     * Initiate cloning system
     */
    public void initiate() {
        this.active = true;
        entity.sendChatMessage("§6[AI Torque] §5I multiply...");
    }

    /**
     * Spawn a clone near a village
     */
    public void spawnCloneNearVillage() {
        if (!active || !plugin.getConfigManager().isCloningEnabled()) return;

        int maxClones = plugin.getConfigManager().getMaxClones();
        if (clones.size() >= maxClones) return;

        Location loc = entity.getLocation();

        // Find nearby villages (look for villagers)
        Collection<Entity> villagers = loc.getWorld().getNearbyEntities(
                loc, 128, 128, 128,
                e -> e instanceof Villager
        );

        if (!villagers.isEmpty()) {
            // Pick random villager location
            Entity randomVillager = villagers.stream()
                    .skip((int) (Math.random() * villagers.size()))
                    .findFirst()
                    .orElse(null);

            if (randomVillager != null) {
                Location cloneLocation = randomVillager.getLocation().clone().add(
                        (Math.random() - 0.5) * 20,
                        5,
                        (Math.random() - 0.5) * 20
                );

                spawnClone(cloneLocation);
            }
        } else {
            // Spawn near main entity if no villages found
            Location cloneLocation = loc.clone().add(
                    (Math.random() - 0.5) * 50,
                    0,
                    (Math.random() - 0.5) * 50
            );
            spawnClone(cloneLocation);
        }
    }

    /**
     * Spawn a clone at location
     */
    private void spawnClone(Location location) {
        // Spawn a phantom as clone (floats, looks ominous)
        Phantom clone = (Phantom) location.getWorld().spawnEntity(
                location,
                EntityType.PHANTOM
        );

        clone.setCustomName("§5AI Torque Clone");
        clone.setCustomNameVisible(true);
        clone.setSize(3);

        // Visual effect
        location.getWorld().spawnParticle(
                Particle.PORTAL,
                location,
                100,
                2, 2, 2,
                0.5
        );

        location.getWorld().playSound(
                location,
                Sound.ENTITY_ENDERMAN_TELEPORT,
                2.0f,
                0.5f
        );

        // Store clone
        clones.put(clone.getUniqueId(), new CloneData(clone, location));
    }

    /**
     * Generate void energy from clones
     */
    public void generateVoidEnergy() {
        if (!active) return;

        for (CloneData data : clones.values()) {
            if (data.cloneEntity.isValid()) {
                // Generate void energy
                data.voidEnergy += 10.0;

                // Visual effect
                if (Math.random() < 0.1) {
                    data.cloneEntity.getWorld().spawnParticle(
                            Particle.DRAGON_BREATH,
                            data.cloneEntity.getLocation(),
                            5,
                            0.5, 0.5, 0.5,
                            0.01
                    );
                }
            }
        }
    }

    /**
     * Consume void from clones
     */
    public double consumeVoidFromClones() {
        if (!active) return 0;

        double totalVoid = 0;

        for (CloneData data : clones.values()) {
            if (data.cloneEntity.isValid()) {
                totalVoid += data.voidEnergy;
                data.voidEnergy = 0; // Reset after consumption

                // Visual drain effect
                data.cloneEntity.getWorld().spawnParticle(
                        Particle.SOUL,
                        data.cloneEntity.getLocation(),
                        20,
                        1, 1, 1,
                        0.1
                );
            }
        }

        if (totalVoid > 0) {
            entity.sendChatMessage("§6[AI Torque] §5Void energy consumed: §f" + (int) totalVoid);
        }

        return totalVoid;
    }

    /**
     * Consume ALL clones for massive power boost
     */
    public double consumeAllClones() {
        if (clones.isEmpty()) return 0;

        double totalPower = 0;

        for (CloneData data : clones.values()) {
            if (data.cloneEntity.isValid()) {
                totalPower += data.voidEnergy + 10000; // Base power per clone

                // Remove clone with effect
                Location loc = data.cloneEntity.getLocation();
                data.cloneEntity.getWorld().spawnParticle(
                        Particle.EXPLOSION_EMITTER,
                        loc,
                        5,
                        0, 0, 0,
                        0
                );

                data.cloneEntity.remove();
            }
        }

        clones.clear();

        entity.sendChatMessage("§6[AI Torque] §4§l§nALL CLONES CONSUMED! MASSIVE POWER SURGE!");

        return totalPower;
    }

    /**
     * Get clone count
     */
    public int getCloneCount() {
        // Clean up invalid clones
        clones.entrySet().removeIf(entry -> !entry.getValue().cloneEntity.isValid());
        return clones.size();
    }

    /**
     * Clean up all clones
     */
    public void cleanup() {
        for (CloneData data : clones.values()) {
            if (data.cloneEntity.isValid()) {
                data.cloneEntity.remove();
            }
        }
        clones.clear();
    }
}
