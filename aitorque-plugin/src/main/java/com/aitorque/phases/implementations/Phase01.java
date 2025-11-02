package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 1: Initial Awakening
 * - Basic grabbing (trees, mobs, blocks)
 * - Teleportation
 * - Flying
 * - Size: 1x
 */
public class Phase01 extends BasePhase {

    public Phase01() {
        super(1, 1.0, 1.0, "Initial Awakening - Basic Powers");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        // Grant basic abilities
        entity.sendChatMessage("§6[AI Torque] §7A presence stirs...");
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Phase 1 behavior: Grab nearby objects
        if (entity.getTickCounter() % 100 == 0) { // Every 5 seconds
            entity.attemptGrabNearbyObjects(10); // 10 block radius
        }
    }
}
