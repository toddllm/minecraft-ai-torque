package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 16: Clone Consumption
 * - Can consume all clones at once
 * - Becomes GIGANTIC temporarily
 * - Size: 100x (temporary surge to 1000x)
 */
public class Phase16 extends BasePhase {

    public Phase16() {
        super(16, 0.25, 100.0, "Clone Consumption - Singularity");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.sendChatMessage("§6[AI Torque] §4§l§nBEHOLD TRUE SCALE!");
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Chance to consume all clones
        if (entity.getTickCounter() % 400 == 0 && Math.random() < 0.3) {
            entity.consumeAllClones(); // Massive size and power boost
        }
    }
}
