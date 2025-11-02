package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 13: Infection Spread
 * - Villager infection active
 * - Parasites spread exponentially
 * - Size: 40x
 */
public class Phase13 extends BasePhase {

    public Phase13() {
        super(13, 0.40, 40.0, "Infection Spread - Plague Bringer");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.sendChatMessage("§6[AI Torque] §4§lCONTAGION UNLEASHED!");
        entity.activateInfectionSystem();
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Infect nearby villagers
        if (entity.getTickCounter() % 60 == 0) {
            entity.infectNearbyVillagers();
        }

        // Parasites spread the infection
        entity.updateParasiteSpread();
    }
}
