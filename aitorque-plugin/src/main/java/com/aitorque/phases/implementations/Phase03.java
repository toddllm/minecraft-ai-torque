package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 3: Tractor Beam Eyes
 * - Can grab ENTIRE trees
 * - Can pick up very large structures
 * - Tractor beam activated
 * - Size: 2x
 */
public class Phase03 extends BasePhase {

    public Phase03() {
        super(3, 0.90, 2.0, "Tractor Beam Eyes - Mass Extraction");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.enableTractorBeam();
        entity.sendChatMessage("§6[AI Torque] §4Reality bends to my will...");
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Use tractor beam to grab large objects
        if (entity.getTickCounter() % 60 == 0) { // Every 3 seconds
            entity.useTractorBeam(32); // 32 block radius
        }
    }
}
