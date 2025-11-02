package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 14: Healing Towers
 * - Spawns healing towers
 * - Continuous regeneration
 * - Size: 45x
 */
public class Phase14 extends BasePhase {

    public Phase14() {
        super(14, 0.35, 45.0, "Healing Towers - Eternal Sustenance");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.sendChatMessage("§6[AI Torque] §bI CANNOT BE STOPPED!");
        entity.spawnHealingTowers();
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Heal from towers
        entity.healFromTowers();

        // Respawn destroyed towers
        if (entity.getTickCounter() % 600 == 0) {
            entity.respawnDestroyedTowers();
        }
    }
}
