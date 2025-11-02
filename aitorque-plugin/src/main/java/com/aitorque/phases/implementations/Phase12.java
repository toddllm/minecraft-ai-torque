package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 12: Cloning Initiation
 * - Spawns clones near villages
 * - Clones generate Void energy
 * - Size: 35x
 */
public class Phase12 extends BasePhase {

    public Phase12() {
        super(12, 0.45, 35.0, "Cloning Initiation - I Am Legion");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.sendChatMessage("§6[AI Torque] §5I AM EVERYWHERE!");
        entity.initiateCloning();
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Spawn and maintain clones
        if (entity.getTickCounter() % 200 == 0) {
            entity.spawnCloneNearVillage();
        }

        // Consume void from clones
        if (entity.getTickCounter() % 100 == 0) {
            entity.consumeVoidFromClones();
        }
    }
}
