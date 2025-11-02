package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 20: Final Stand / Transformation
 * - Grabs all nearby mobs for full heal
 * - Forces transformation to Zikes
 * - Last normal form phase
 * - Size: 1000x
 */
public class Phase20 extends BasePhase {

    public Phase20() {
        super(20, 0.05, 1000.0, "Final Stand - Transformation Imminent");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.sendChatMessage("§6[AI Torque] §4§l§n§oYOU FORCED MY HAND!");

        // Grab everything for massive heal
        entity.grabAllNearbyMobs(64);
        entity.fullHealFromGrab();

        // Force transformation
        if (entity.getPlugin().getConfigManager().areTransformationsAllowed()) {
            entity.initiateZikesTransformation();
        }
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // All abilities firing
        entity.unleashAllAbilities();
    }
}
