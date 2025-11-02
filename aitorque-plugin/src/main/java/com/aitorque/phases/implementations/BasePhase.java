package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;
import com.aitorque.phases.Phase;

/**
 * Base implementation of Phase interface
 * Provides default implementations for common phase functionality
 */
public abstract class BasePhase implements Phase {

    private final int phaseNumber;
    private final double activationHealth;
    private final double sizeMultiplier;
    private final String description;

    protected BasePhase(int phaseNumber, double activationHealth, double sizeMultiplier, String description) {
        this.phaseNumber = phaseNumber;
        this.activationHealth = activationHealth;
        this.sizeMultiplier = sizeMultiplier;
        this.description = description;
    }

    @Override
    public int getPhaseNumber() {
        return phaseNumber;
    }

    @Override
    public double getActivationHealth() {
        return activationHealth;
    }

    @Override
    public double getSizeMultiplier() {
        return sizeMultiplier;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        // Default activation - can be overridden
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Default tick - can be overridden
    }

    @Override
    public void onDeactivate(AITorqueEntity entity) {
        // Default deactivation - can be overridden
    }

    // Default ability permissions
    @Override
    public boolean canGrabTrees() {
        return phaseNumber >= 1;
    }

    @Override
    public boolean canGrabLargeObjects() {
        return phaseNumber >= 3;
    }

    @Override
    public boolean canGrabVillages() {
        return phaseNumber >= 5;
    }

    @Override
    public boolean hasTractorBeam() {
        return phaseNumber >= 3;
    }

    @Override
    public boolean canBreakBlocksWithMaskTeeth() {
        return phaseNumber >= 5;
    }

    @Override
    public boolean canCreateStorm() {
        return phaseNumber >= 7;
    }

    @Override
    public boolean canGrabSouls() {
        return phaseNumber >= 9;
    }
}
