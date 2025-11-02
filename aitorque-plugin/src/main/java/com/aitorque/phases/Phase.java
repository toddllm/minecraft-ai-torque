package com.aitorque.phases;

import com.aitorque.entity.AITorqueEntity;

/**
 * Interface for AI Torque phases
 * Each phase represents a different power level and ability set
 */
public interface Phase {

    /**
     * Get the phase number (1-20+)
     */
    int getPhaseNumber();

    /**
     * Get the health percentage where this phase activates
     * @return Health percentage (0.0 - 1.0)
     */
    double getActivationHealth();

    /**
     * Get the size multiplier for this phase
     */
    double getSizeMultiplier();

    /**
     * Called when this phase becomes active
     * @param entity The AI Torque entity
     */
    void onActivate(AITorqueEntity entity);

    /**
     * Called every tick while this phase is active
     * @param entity The AI Torque entity
     */
    void onTick(AITorqueEntity entity);

    /**
     * Called when this phase deactivates (moving to next phase)
     * @param entity The AI Torque entity
     */
    void onDeactivate(AITorqueEntity entity);

    /**
     * Get description of this phase's abilities
     */
    String getDescription();

    /**
     * Check if this phase allows certain abilities
     */
    boolean canGrabTrees();
    boolean canGrabLargeObjects();
    boolean canGrabVillages();
    boolean hasTractorBeam();
    boolean canBreakBlocksWithMaskTeeth();
    boolean canCreateStorm();
    boolean canGrabSouls();
}
