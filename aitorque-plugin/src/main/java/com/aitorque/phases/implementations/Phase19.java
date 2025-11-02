package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 19: Pre-Transformation
 * - All abilities at maximum
 * - Statue spawning
 * - Preparing for Zikes
 * - Size: 500x
 */
public class Phase19 extends BasePhase {

    public Phase19() {
        super(19, 0.10, 500.0, "Pre-Transformation - The Precipice");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.sendChatMessage("§6[AI Torque] §c§lTHIS ISN'T EVEN MY FINAL FORM!");
        entity.startStatueSpawning();
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Maximum aggression
        entity.maximumAggression();

        // Spawn statues
        if (entity.getTickCounter() % 300 == 0) {
            entity.spawnStatue();
        }
    }
}
