package com.aitorque.abilities;

import com.aitorque.AITorquePlugin;
import com.aitorque.entity.AITorqueEntity;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Pokemon Type System - Cycles through different elemental types
 */
public class TypeSystemAbility {

    private final AITorquePlugin plugin;
    private final AITorqueEntity entity;
    private boolean active;

    private ElementType currentType;
    private int typeDamageTaken;
    private final List<ElementType> defeatedTypes;
    private int fallRecoveryTimer;

    public enum ElementType {
        FIRE(Color.RED, Particle.FLAME, PotionEffectType.FIRE_RESISTANCE),
        WATER(Color.BLUE, Particle.DRIPPING_WATER, PotionEffectType.WATER_BREATHING),
        ELECTRIC(Color.YELLOW, Particle.ELECTRIC_SPARK, PotionEffectType.SPEED),
        GRASS(Color.GREEN, Particle.HAPPY_VILLAGER, PotionEffectType.REGENERATION),
        ICE(Color.AQUA, Particle.SNOWFLAKE, PotionEffectType.SLOW_FALLING),
        DARK(Color.fromRGB(50, 0, 50), Particle.LARGE_SMOKE, PotionEffectType.BLINDNESS),
        PSYCHIC(Color.PURPLE, Particle.ENCHANT, PotionEffectType.LEVITATION),
        DRAGON(Color.fromRGB(128, 0, 255), Particle.DRAGON_BREATH, PotionEffectType.STRENGTH);

        final Color color;
        final Particle particle;
        final PotionEffectType effect;

        ElementType(Color color, Particle particle, PotionEffectType effect) {
            this.color = color;
            this.particle = particle;
            this.effect = effect;
        }
    }

    public TypeSystemAbility(AITorquePlugin plugin, AITorqueEntity entity) {
        this.plugin = plugin;
        this.entity = entity;
        this.active = false;
        this.defeatedTypes = new ArrayList<>();
        this.typeDamageTaken = 0;
        this.fallRecoveryTimer = 0;
    }

    /**
     * Activate type system
     */
    public void activate() {
        if (!plugin.getConfigManager().isTypeSystemEnabled()) return;

        this.active = true;
        changeToNextType();
        entity.sendChatMessage("§6[AI Torque] §e§lElemental transformation activated!");
    }

    /**
     * Change to next type
     */
    public void changeToNextType() {
        if (!active) return;

        // Find next undefeated type
        ElementType nextType = null;
        for (ElementType type : ElementType.values()) {
            if (!defeatedTypes.contains(type)) {
                nextType = type;
                break;
            }
        }

        if (nextType == null) {
            // All types defeated! Sky fall event
            allTypesDefeated();
            return;
        }

        currentType = nextType;
        typeDamageTaken = 0;

        // Visual transformation
        Location loc = entity.getLocation();
        loc.getWorld().spawnParticle(
                Particle.EXPLOSION,
                loc,
                20,
                2, 2, 2,
                0
        );

        // Type-specific effects
        entity.getBukkitEntity().addPotionEffect(new PotionEffect(
                currentType.effect,
                Integer.MAX_VALUE,
                1,
                false,
                false
        ));

        entity.sendChatMessage("§6[AI Torque] §eType: §f" + currentType.name());
    }

    /**
     * Use current type attack
     */
    public void useTypeAttack() {
        if (!active || currentType == null) return;

        Location loc = entity.getLocation();

        // Spawn type particles
        loc.getWorld().spawnParticle(
                currentType.particle,
                loc.clone().add(0, 2, 0),
                30,
                2, 2, 2,
                0.1
        );

        // Type-specific attack
        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(
                loc, 15, 15, 15,
                e -> e instanceof LivingEntity && !(e.equals(entity.getBukkitEntity()))
        );

        for (Entity e : nearby) {
            if (e instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) e;

                // Apply type-specific damage/effect
                switch (currentType) {
                    case FIRE:
                        target.setFireTicks(100);
                        target.damage(5.0);
                        break;
                    case WATER:
                        target.addPotionEffect(new PotionEffect(
                                PotionEffectType.SLOWNESS, 60, 2
                        ));
                        break;
                    case ELECTRIC:
                        target.damage(8.0);
                        loc.getWorld().strikeLightningEffect(target.getLocation());
                        break;
                    case ICE:
                        target.addPotionEffect(new PotionEffect(
                                PotionEffectType.SLOWNESS, 100, 5
                        ));
                        target.setFreezeTicks(200);
                        break;
                    case GRASS:
                        // Healing for AI Torque
                        entity.heal(10);
                        break;
                    case DARK:
                        target.addPotionEffect(new PotionEffect(
                                PotionEffectType.WITHER, 60, 1
                        ));
                        break;
                    case PSYCHIC:
                        target.addPotionEffect(new PotionEffect(
                                PotionEffectType.NAUSEA, 100, 1
                        ));
                        break;
                    case DRAGON:
                        target.damage(10.0);
                        break;
                }
            }
        }
    }

    /**
     * Record damage taken for current type
     */
    public void recordDamage(double damage) {
        if (!active) return;

        typeDamageTaken += (int) damage;

        // Check if type defeated (need to take ~1000 damage per type)
        if (typeDamageTaken >= 1000) {
            defeatedTypes.add(currentType);
            entity.sendChatMessage("§6[AI Torque] §c" + currentType.name() + " form defeated!");
            changeToNextType();
        }
    }

    /**
     * Check if should change type
     */
    public boolean shouldChangeType() {
        return typeDamageTaken >= 1000;
    }

    /**
     * All types defeated - fall from sky event
     */
    private void allTypesDefeated() {
        entity.sendChatMessage("§6[AI Torque] §4§lAll forms defeated! Falling...");

        Location loc = entity.getLocation();

        // Teleport high in sky
        entity.getBukkitEntity().teleport(loc.clone().add(0, 50, 0));

        // Fall with effect
        loc.getWorld().spawnParticle(
                Particle.EXPLOSION_EMITTER,
                loc.clone().add(0, 50, 0),
                50,
                5, 5, 5,
                0
        );

        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 5.0f, 0.1f);

        // 50% chance to wake up after 10 seconds
        fallRecoveryTimer = 200; // 10 seconds
    }

    /**
     * Update recovery timer
     */
    public void updateRecoveryTimer() {
        if (fallRecoveryTimer > 0) {
            fallRecoveryTimer--;

            if (fallRecoveryTimer == 0) {
                // 50% chance to wake up
                if (Math.random() < 0.5) {
                    wakeUp();
                } else {
                    entity.sendChatMessage("§6[AI Torque] §7Exhausted...");
                }
            }
        }
    }

    /**
     * Wake up and reset types
     */
    private void wakeUp() {
        entity.sendChatMessage("§6[AI Torque] §c§lI WON'T GIVE UP!");

        // Reset type system
        defeatedTypes.clear();
        typeDamageTaken = 0;
        changeToNextType();

        // Heal partially
        entity.heal(entity.getMaxHealth() * 0.2);
    }

    /**
     * Get current type
     */
    public ElementType getCurrentType() {
        return currentType;
    }

    /**
     * Is active
     */
    public boolean isActive() {
        return active;
    }
}
