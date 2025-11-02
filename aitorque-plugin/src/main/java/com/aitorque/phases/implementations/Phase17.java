package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 17: Reality Bending
 * - Terrain destruction
 * - Block manipulation
 * - Forcefield activation
 * - Size: 150x
 */
public class Phase17 extends BasePhase {

    public Phase17() {
        super(17, 0.20, 150.0, "Reality Bending - World Breaker");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.sendChatMessage("§6[AI Torque] §5§l§oREALITY IS MINE TO COMMAND!");
        entity.activateForcefield();
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Break terrain
        if (entity.getTickCounter() % 40 == 0) {
            entity.destroyNearbyTerrain(10);
        }

        // Forcefield blocks some damage
        entity.updateForcefield();
    }
}
