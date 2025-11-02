package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 6: Wind Force
 * - Spinning attacks
 * - Wind generation that lifts players
 * - Size: 7x
 */
public class Phase06 extends BasePhase {

    public Phase06() {
        super(6, 0.75, 7.0, "Wind Force - Hurricane Manifestation");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.sendChatMessage("§6[AI Torque] §bFeel the winds of destruction!");
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Generate wind that pushes players
        if (entity.getTickCounter() % 30 == 0) {
            entity.createWindBlast(20);
        }
    }
}
