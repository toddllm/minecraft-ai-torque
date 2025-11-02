package com.aitorque.listeners;

import com.aitorque.AITorquePlugin;
import com.aitorque.entity.AITorqueEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Listens for entity-related events for AI Torque
 */
public class EntityListener implements Listener {

    private final AITorquePlugin plugin;

    public EntityListener(AITorquePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Handle AI Torque being attacked - triggers hostility
        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity entity = (LivingEntity) event.getEntity();

        // Check if damaged entity is an AI Torque instance
        AITorqueEntity torque = plugin.getActiveTorque(entity.getUniqueId());
        if (torque != null) {
            // Check if attacker is a player
            if (event.getDamager() instanceof Player) {
                Player attacker = (Player) event.getDamager();

                // ADD THIS PLAYER TO THE DAMAGED LIST - now AI Torque will attack them!
                torque.addDamagingPlayer(attacker.getUniqueId());

                // If neutral, become hostile
                if (!torque.isHostile()) {
                    torque.setHostile(true);
                    torque.sendChatMessage("§6[AI Torque] §cYou dare strike me? Now face my wrath!");

                    // Visual effect - explosion particles
                    entity.getWorld().spawnParticle(
                        org.bukkit.Particle.EXPLOSION_EMITTER,
                        entity.getLocation(), 5, 1.0, 1.0, 1.0, 0.1
                    );

                    // Sound effect
                    entity.getWorld().playSound(
                        entity.getLocation(),
                        org.bukkit.Sound.ENTITY_WITHER_SPAWN,
                        3.0f, 0.5f
                    );
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // Handle AI Torque damage events
        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity entity = (LivingEntity) event.getEntity();

        // Check if damaged entity is an AI Torque instance
        AITorqueEntity torque = plugin.getActiveTorque(entity.getUniqueId());
        if (torque != null) {
            // Check for transformation progression after damage
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                torque.checkTransformationProgression();
            }, 1L);

            // Apply forcefield damage reduction if active
            if (torque.hasForcefield()) {
                double reduction = 0.5; // 50% damage reduction
                event.setDamage(event.getDamage() * (1 - reduction));
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Prevent AI Torque death if immortality is enabled
        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity entity = (LivingEntity) event.getEntity();

        // Check if entity is an AI Torque instance
        AITorqueEntity torque = plugin.getActiveTorque(entity.getUniqueId());
        if (torque != null) {
            // Cancel death - AI Torque is immortal
            event.setCancelled(true);

            // Instead, enter "exhausted" state at 1 health
            entity.setHealth(1.0);
            torque.setHealth(1.0);

            // Send message
            torque.sendChatMessage("§6[AI Torque] §7I... need... rest...");
            torque.sendChatMessage("§6[AI Torque] §7I will return...");

            // Remove the entity after a delay
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                entity.remove();
                plugin.removeActiveTorque(entity.getUniqueId());
            }, 100L); // 5 seconds
        }
    }
}
