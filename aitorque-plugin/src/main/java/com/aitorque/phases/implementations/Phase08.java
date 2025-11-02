package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 8: Shockwave Devastation
 * - Massive shockwaves
 * - Crashes create enormous damage
 * - Size: 15x
 */
public class Phase08 extends BasePhase {

    public Phase08() {
        super(8, 0.65, 15.0, "Shockwave Devastation - Earth Shaker");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.sendChatMessage("§6[AI Torque] §4§lTHE GROUND TREMBLES!");
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Create massive shockwaves
        if (entity.getTickCounter() % 80 == 0) {
            entity.createMassiveShockwave(32);
        }
    }
}
