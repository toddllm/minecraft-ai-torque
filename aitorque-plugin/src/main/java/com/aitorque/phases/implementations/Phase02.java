package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 2: Debris Orbit
 * - Debris begins orbiting
 * - Accessories appear (blackhole wings, halos)
 * - Size: 1.5x
 */
public class Phase02 extends BasePhase {

    public Phase02() {
        super(2, 0.95, 1.5, "Debris Orbit - Accessories Manifest");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.spawnAccessories();
        entity.startDebrisOrbit();
        entity.sendChatMessage("§6[AI Torque] §cMy power grows...");
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Update orbiting debris
        entity.updateDebrisOrbit();
    }
}
