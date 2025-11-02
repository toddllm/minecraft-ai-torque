package com.aitorque.abilities;

import com.aitorque.AITorquePlugin;
import com.aitorque.entity.AITorqueEntity;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Infection System - Converts villagers to parasites
 */
public class InfectionAbility {

    private final AITorquePlugin plugin;
    private final AITorqueEntity entity;
    private final Set<UUID> parasiteVillagers;
    private boolean active;

    public InfectionAbility(AITorquePlugin plugin, AITorqueEntity entity) {
        this.plugin = plugin;
        this.entity = entity;
        this.parasiteVillagers = new HashSet<>();
        this.active = false;
    }

    /**
     * Activate infection system
     */
    public void activate() {
        this.active = true;
        entity.sendChatMessage("§6[AI Torque] §4The plague spreads...");
    }

    /**
     * Infect nearby villagers
     */
    public void infectNearbyVillagers() {
        if (!active || !plugin.getConfigManager().isInfectionEnabled()) return;

        int maxParasites = plugin.getConfigManager().getMaxParasites();
        if (parasiteVillagers.size() >= maxParasites) return;

        Location loc = entity.getLocation();
        int range = plugin.getConfigManager().getInfectionSpreadRange();

        Collection<Entity> entities = loc.getWorld().getNearbyEntities(
                loc, range, range, range,
                e -> e instanceof Villager
        );

        for (Entity e : entities) {
            if (e instanceof Villager) {
                Villager villager = (Villager) e;

                // Check if already infected
                if (!parasiteVillagers.contains(villager.getUniqueId())) {
                    convertToParasite(villager);

                    if (parasiteVillagers.size() >= maxParasites) break;
                }
            }
        }
    }

    /**
     * Convert villager to parasite
     */
    private void convertToParasite(Villager villager) {
        // Add to parasite list
        parasiteVillagers.add(villager.getUniqueId());

        // Visual transformation
        villager.setCustomName("§4§lParasite");
        villager.setCustomNameVisible(true);

        // Apply effects
        villager.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false
        ));
        villager.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, false
        ));
        villager.addPotionEffect(new PotionEffect(
                PotionEffectType.GLOWING, Integer.MAX_VALUE, 1, false, false
        ));

        // Particle effect
        villager.getWorld().spawnParticle(
                Particle.LARGE_SMOKE,
                villager.getLocation(),
                50,
                0.5, 1, 0.5,
                0.1
        );

        villager.getWorld().playSound(
                villager.getLocation(),
                Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED,
                1.0f,
                0.5f
        );
    }

    /**
     * Update parasite spread - parasites infect other villagers
     */
    public void updateParasiteSpread() {
        if (!active) return;

        int maxParasites = plugin.getConfigManager().getMaxParasites();
        if (parasiteVillagers.size() >= maxParasites) return;

        // Each parasite tries to infect nearby villagers
        List<UUID> parasitesToCheck = new ArrayList<>(parasiteVillagers);

        for (UUID parasiteId : parasitesToCheck) {
            Entity entity = plugin.getServer().getEntity(parasiteId);

            if (entity instanceof Villager) {
                Villager parasite = (Villager) entity;

                // Find nearby uninfected villagers
                Collection<Entity> nearby = parasite.getWorld().getNearbyEntities(
                        parasite.getLocation(), 5, 5, 5,
                        e -> e instanceof Villager && !parasiteVillagers.contains(e.getUniqueId())
                );

                for (Entity e : nearby) {
                    if (Math.random() < 0.05) { // 5% chance per tick
                        convertToParasite((Villager) e);
                    }

                    if (parasiteVillagers.size() >= maxParasites) return;
                }
            } else {
                // Remove dead/invalid parasites
                parasiteVillagers.remove(parasiteId);
            }
        }
    }

    /**
     * Get parasite count
     */
    public int getParasiteCount() {
        return parasiteVillagers.size();
    }

    /**
     * Clean up
     */
    public void cleanup() {
        // Remove all parasite effects
        for (UUID parasiteId : parasiteVillagers) {
            Entity entity = plugin.getServer().getEntity(parasiteId);
            if (entity instanceof Villager) {
                Villager villager = (Villager) entity;
                villager.setCustomName(null);
                villager.setCustomNameVisible(false);
            }
        }
        parasiteVillagers.clear();
    }
}
