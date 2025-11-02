package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 10: Rebirth
 * - Major transformation opportunity
 * - Can reset size and rebirth stronger
 * - Potential transformation to Zikes form
 * - Size: Resets to 1x then grows
 */
public class Phase10 extends BasePhase {

    public Phase10() {
        super(10, 0.55, 1.0, "Rebirth - Transformation Gateway");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.initiateRebirth();
        entity.sendChatMessage("§6[AI Torque] §d§lI AM REBORN!");

        // 30% chance to transform to Zikes
        if (Math.random() < 0.3 && entity.getPlugin().getConfigManager().areTransformationsAllowed()) {
            entity.transformToZikes();
        }
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Rapid growth after rebirth
        entity.accelerateGrowth();
    }
}
