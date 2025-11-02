package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 4: Enhanced Power
 * - Enhanced grabbing speed
 * - Movement speed boost
 * - Size: 2.5x
 */
public class Phase04 extends BasePhase {

    public Phase04() {
        super(4, 0.85, 2.5, "Enhanced Power - Speed Ascension");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.applySpeedBoost(1.5);
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Enhanced grabbing with higher frequency
        if (entity.getTickCounter() % 40 == 0) { // Every 2 seconds
            entity.attemptGrabNearbyObjects(20);
        }
    }
}
