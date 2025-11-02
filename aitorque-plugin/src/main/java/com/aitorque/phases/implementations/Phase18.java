package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 18: Dragon Consumption
 * - Can consume Ender Dragon
 * - Drains health from any boss
 * - Size: 200x
 */
public class Phase18 extends BasePhase {

    public Phase18() {
        super(18, 0.15, 200.0, "Dragon Consumption - Boss Killer");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.sendChatMessage("§6[AI Torque] §d§lEVEN DRAGONS BOW TO ME!");
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Attempt to consume nearby bosses
        if (entity.getTickCounter() % 100 == 0) {
            entity.drainBossHealth();
        }
    }
}
