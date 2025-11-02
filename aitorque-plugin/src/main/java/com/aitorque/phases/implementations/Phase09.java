package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 9: Soul Reaper
 * - Can grab souls from mobs
 * - Souls convert to power or void
 * - Size: 20x
 */
public class Phase09 extends BasePhase {

    public Phase09() {
        super(9, 0.60, 20.0, "Soul Reaper - Harvester of Life");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.enableSoulGrabbing();
        entity.sendChatMessage("§6[AI Torque] §5§lYOUR SOULS ARE MINE!");
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Grab souls from nearby mobs
        if (entity.getTickCounter() % 60 == 0) {
            entity.grabNearbySouls(25);
        }
    }
}
