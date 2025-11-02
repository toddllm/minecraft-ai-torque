package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 7: Storm Master
 * - Spinning brick storm
 * - Dive attacks
 * - Mask growth
 * - Size: 10x
 */
public class Phase07 extends BasePhase {

    public Phase07() {
        super(7, 0.70, 10.0, "Storm Master - Tempest Unleashed");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.enableStormCreation();
        entity.sendChatMessage("§6[AI Torque] §c§lTHE STORM OBEYS!");
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Create debris storm
        entity.createDebrisStorm();

        // Perform dive attacks
        if (entity.getTickCounter() % 100 == 0) {
            entity.performDiveAttack();
        }
    }
}
