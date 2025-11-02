package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 11: Power Accumulation
 * - Rapid power gain
 * - Enhanced all abilities
 * - Size: 30x
 */
public class Phase11 extends BasePhase {

    public Phase11() {
        super(11, 0.50, 30.0, "Power Accumulation - Ascending Beyond");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.sendChatMessage("§6[AI Torque] §d§lMY POWER GROWS EXPONENTIALLY!");
        entity.startRapidPowerGain();
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // All previous abilities active, enhanced
        if (entity.getTickCounter() % 40 == 0) {
            entity.attemptGrabNearbyObjects(35);
            entity.grabNearbySouls(30);
        }

        entity.acceleratePowerGain();
    }
}
