package com.aitorque.transformations;

/**
 * Transformation forms for AI Torque
 * Represents different evolution states of the boss
 */
public enum TransformationForm {
    /**
     * Normal AI Torque form
     */
    NORMAL,

    /**
     * Zikes form - Thermine Slash, Giant Blackholes, UFO Grab
     * Triggered after significant damage or Phase 10
     */
    ZIKES,

    /**
     * The End Of The Universe (TEOTU) - Shark form, extreme power
     * Triggered after Zikes takes enough damage
     */
    TEOTU,

    /**
     * Medinuio Aura - Final form with all powers
     * Triggered after TEOTU is defeated
     */
    MEDINUIO_AURA
}
