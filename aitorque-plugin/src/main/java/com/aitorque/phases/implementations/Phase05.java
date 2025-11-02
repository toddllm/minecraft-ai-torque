package com.aitorque.phases.implementations;

import com.aitorque.entity.AITorqueEntity;

/**
 * Phase 5: Village Consumption
 * - Mask teeth can break any blocks
 * - Can consume entire villages
 * - Massive power boost when consuming villages
 * - Size: 5x
 */
public class Phase05 extends BasePhase {

    public Phase05() {
        super(5, 0.80, 5.0, "Village Consumption - Destroyer of Worlds");
    }

    @Override
    public void onActivate(AITorqueEntity entity) {
        super.onActivate(entity);
        entity.enableMaskTeethBlockBreaking();
        entity.sendChatMessage("§6[AI Torque] §4§lENTIRE CIVILIZATIONS SHALL FUEL MY ASCENSION!");
    }

    @Override
    public void onTick(AITorqueEntity entity) {
        // Attempt to consume nearby villages
        if (entity.getTickCounter() % 200 == 0) { // Every 10 seconds
            entity.attemptVillageConsumption(64);
        }

        // Break blocks with mask teeth
        entity.breakBlocksWithMaskTeeth();
    }
}
