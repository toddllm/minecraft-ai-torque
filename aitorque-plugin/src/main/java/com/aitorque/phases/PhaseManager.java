package com.aitorque.phases;

import com.aitorque.AITorquePlugin;
import com.aitorque.entity.AITorqueEntity;
import com.aitorque.phases.implementations.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages phase progression for AI Torque
 * Handles transitions between phases based on health percentage
 */
public class PhaseManager {

    private final AITorquePlugin plugin;
    private final AITorqueEntity entity;
    private final Map<Integer, Phase> phases;
    private Phase currentPhase;
    private int currentPhaseNumber;

    public PhaseManager(AITorquePlugin plugin, AITorqueEntity entity) {
        this.plugin = plugin;
        this.entity = entity;
        this.phases = new HashMap<>();
        this.currentPhaseNumber = 1;

        initializePhases();
        this.currentPhase = phases.get(1);
    }

    /**
     * Initialize all phases
     */
    private void initializePhases() {
        // Register all 20 phases
        phases.put(1, new Phase01());
        phases.put(2, new Phase02());
        phases.put(3, new Phase03());
        phases.put(4, new Phase04());
        phases.put(5, new Phase05());
        phases.put(6, new Phase06());
        phases.put(7, new Phase07());
        phases.put(8, new Phase08());
        phases.put(9, new Phase09());
        phases.put(10, new Phase10());
        phases.put(11, new Phase11());
        phases.put(12, new Phase12());
        phases.put(13, new Phase13());
        phases.put(14, new Phase14());
        phases.put(15, new Phase15());
        phases.put(16, new Phase16());
        phases.put(17, new Phase17());
        phases.put(18, new Phase18());
        phases.put(19, new Phase19());
        phases.put(20, new Phase20());
    }

    /**
     * Update phase based on current health
     */
    public void updatePhase() {
        double healthPercent = entity.getHealth() / entity.getMaxHealth();

        // Find appropriate phase for current health
        Phase newPhase = findPhaseForHealth(healthPercent);

        if (newPhase != null && newPhase != currentPhase) {
            transitionToPhase(newPhase);
        }
    }

    /**
     * Find the phase that should be active at this health percentage
     */
    private Phase findPhaseForHealth(double healthPercent) {
        Phase targetPhase = null;
        int maxPhase = plugin.getConfigManager().getMaxPhase();

        for (int i = 1; i <= maxPhase; i++) {
            Phase phase = phases.get(i);
            if (phase != null && healthPercent <= phase.getActivationHealth()) {
                targetPhase = phase;
            }
        }

        return targetPhase != null ? targetPhase : phases.get(1);
    }

    /**
     * Transition to a new phase
     */
    private void transitionToPhase(Phase newPhase) {
        if (currentPhase != null) {
            currentPhase.onDeactivate(entity);
        }

        Phase oldPhase = currentPhase;
        currentPhase = newPhase;
        currentPhaseNumber = newPhase.getPhaseNumber();

        currentPhase.onActivate(entity);

        // Broadcast phase change if enabled
        if (plugin.getConfigManager().shouldBroadcastPhaseChanges()) {
            broadcastPhaseChange(oldPhase, newPhase);
        }

        // Log if debug enabled
        if (plugin.getConfigManager().shouldLogPhaseChanges()) {
            plugin.getLogger().info("AI Torque transitioned to Phase " + currentPhaseNumber);
        }
    }

    /**
     * Force set to specific phase (admin command)
     */
    public void setPhase(int phaseNumber) {
        Phase phase = phases.get(phaseNumber);
        if (phase != null) {
            transitionToPhase(phase);
        }
    }

    /**
     * Tick the current phase
     */
    public void tick() {
        if (currentPhase != null) {
            currentPhase.onTick(entity);
        }
    }

    /**
     * Broadcast phase change to all players
     */
    private void broadcastPhaseChange(Phase oldPhase, Phase newPhase) {
        String message = String.format("§6[AI Torque] §c⚡ Phase %d Activated ⚡§r §7- %s",
                newPhase.getPhaseNumber(),
                newPhase.getDescription());

        plugin.getServer().broadcastMessage(message);
    }

    /**
     * Get current phase
     */
    public Phase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Get current phase number
     */
    public int getCurrentPhaseNumber() {
        return currentPhaseNumber;
    }

    /**
     * Get size multiplier for current phase
     */
    public double getSizeMultiplier() {
        return currentPhase != null ? currentPhase.getSizeMultiplier() : 1.0;
    }
}
