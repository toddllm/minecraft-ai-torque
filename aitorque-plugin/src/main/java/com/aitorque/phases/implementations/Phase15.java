package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 15: Type Transformation
 * - Pokemon type system activates
 * - Changes types and colors
 * - Must defeat all types
 * - Size: 50x
 */
public class Phase15 extends BasePhase {

    public Phase15() {
        super(15, 0.30, 50.0, "Type Transformation - Elemental Mastery");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.sendChatMessage("§6[AI Torque] §e§lWITNESS MY FORMS!");
        entity.activateTypeSystem();
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Attack with current type
        entity.useTypeAttack();

        // Change type when current type defeated
        if (entity.shouldChangeType()) {
            entity.changeToNextType();
        }
    }
}
