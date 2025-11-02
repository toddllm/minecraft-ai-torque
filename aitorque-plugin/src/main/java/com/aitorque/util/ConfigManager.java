package com.aitorque.util;

import com.aitorque.AITorquePlugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Configuration manager for AI Torque
 * Provides easy access to configuration values
 */
public class ConfigManager {

    private final AITorquePlugin plugin;
    private final FileConfiguration config;

    public ConfigManager(AITorquePlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    // Spawn Settings
    public boolean isNaturalSpawnEnabled() {
        return config.getBoolean("spawn.natural", false);
    }

    public boolean isCommandOnlySpawn() {
        return config.getBoolean("spawn.command-only", true);
    }

    public int getMaxInstances() {
        return config.getInt("spawn.max-instances", 1);
    }

    // Difficulty Settings
    public double getHealthMultiplier() {
        return config.getDouble("difficulty.health-multiplier", 1.0);
    }

    public double getDamageMultiplier() {
        return config.getDouble("difficulty.damage-multiplier", 1.0);
    }

    public double getPhaseSpeed() {
        return config.getDouble("difficulty.phase-speed", 1.0);
    }

    // Phase Configuration
    public boolean areAllPhasesEnabled() {
        return config.getBoolean("phases.enable-all", true);
    }

    public int getMaxPhase() {
        return config.getInt("phases.max-phase", 20);
    }

    public boolean areTransformationsAllowed() {
        return config.getBoolean("phases.allow-transformations", true);
    }

    // Abilities
    public boolean isInfectionEnabled() {
        return config.getBoolean("abilities.infection.enabled", true);
    }

    public int getInfectionSpreadRange() {
        return config.getInt("abilities.infection.spread-range", 32);
    }

    public int getMaxParasites() {
        return config.getInt("abilities.infection.max-parasites", 100);
    }

    public boolean isCloningEnabled() {
        return config.getBoolean("abilities.cloning.enabled", true);
    }

    public int getMaxClones() {
        return config.getInt("abilities.cloning.max-clones", 10);
    }

    public boolean isHealthDrainEnabled() {
        return config.getBoolean("abilities.health-drain.enabled", true);
    }

    public int getDrainRange() {
        return config.getInt("abilities.health-drain.drain-range", 16);
    }

    public double getDrainRate() {
        return config.getDouble("abilities.health-drain.drain-rate", 0.5);
    }

    public boolean areHealingTowersEnabled() {
        return config.getBoolean("abilities.healing-towers.enabled", true);
    }

    public int getMaxHealingTowers() {
        return config.getInt("abilities.healing-towers.max-towers", 5);
    }

    public boolean isGrabbingEnabled() {
        return config.getBoolean("abilities.grabbing.enabled", true);
    }

    public int getMaxOrbitingObjects() {
        return config.getInt("abilities.grabbing.max-orbiting-objects", 50);
    }

    public boolean areMasksEnabled() {
        return config.getBoolean("abilities.masks.enabled", true);
    }

    public int getStartingMasks() {
        return config.getInt("abilities.masks.starting-masks", 4);
    }

    public int getMaxMasks() {
        return config.getInt("abilities.masks.max-masks", 16);
    }

    public boolean isWeatherControlEnabled() {
        return config.getBoolean("abilities.weather.enabled", true);
    }

    public boolean isTypeSystemEnabled() {
        return config.getBoolean("abilities.type-system.enabled", true);
    }

    // Behavior
    public boolean isNeutralByDefault() {
        return config.getBoolean("behavior.neutral-by-default", true);
    }

    public boolean doesAggroOnAttack() {
        return config.getBoolean("behavior.aggro-on-attack", true);
    }

    public double getAggroDistance() {
        return config.getDouble("behavior.aggro-distance", 8.0);
    }

    // Mechanics
    public boolean areStatueDropsEnabled() {
        return config.getBoolean("mechanics.statue-drops.enabled", true);
    }

    public boolean isWoodTriggerEnabled() {
        return config.getBoolean("mechanics.wood-trigger.enabled", true);
    }

    public long getWoodPowerMultiplier() {
        return config.getLong("mechanics.wood-trigger.power-multiplier", 50000000000L);
    }

    public boolean isTerrainDestructionEnabled() {
        return config.getBoolean("mechanics.terrain-destruction.enabled", true);
    }

    public boolean isImmortalityEnabled() {
        return config.getBoolean("mechanics.immortality.enabled", true);
    }

    // Performance
    public int getTickRate() {
        return config.getInt("performance.tick-rate", 1);
    }

    public int getMaxParticles() {
        return config.getInt("performance.max-particles", 1000);
    }

    // Effects
    public boolean areParticlesEnabled() {
        return config.getBoolean("effects.particles.enabled", true);
    }

    public double getParticleIntensity() {
        return config.getDouble("effects.particles.intensity", 1.0);
    }

    public boolean areSoundsEnabled() {
        return config.getBoolean("effects.sounds.enabled", true);
    }

    public boolean isSizeScalingEnabled() {
        return config.getBoolean("effects.size-scaling.enabled", true);
    }

    // Communication
    public boolean isChatEnabled() {
        return config.getBoolean("communication.chat-enabled", true);
    }

    public boolean shouldBroadcastPhaseChanges() {
        return config.getBoolean("communication.broadcast-phase-changes", true);
    }

    public String getMessage(String key) {
        return config.getString("communication.messages." + key, "");
    }

    // Debug
    public boolean isDebugEnabled() {
        return config.getBoolean("debug.enabled", false);
    }

    public boolean shouldLogPhaseChanges() {
        return config.getBoolean("debug.log-phase-changes", false);
    }

    public boolean shouldLogAbilityUsage() {
        return config.getBoolean("debug.log-ability-usage", false);
    }
}
