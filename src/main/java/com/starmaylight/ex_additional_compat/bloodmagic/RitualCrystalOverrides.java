package com.starmaylight.ex_additional_compat.bloodmagic;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of custom crystal tier overrides for Blood Magic rituals.
 * Used by MixinRitualHelper to enforce custom activation crystal requirements.
 *
 * Crystal tiers in Blood Magic:
 *   1 = Weak Activation Crystal
 *   2 = Awakened Activation Crystal
 *
 * KubeJS scripts can set custom minimum crystal levels per ritual.
 * A value of -1 means no override (use default Blood Magic logic).
 */
public class RitualCrystalOverrides {

    private static final ConcurrentHashMap<String, Integer> OVERRIDES = new ConcurrentHashMap<>();

    /**
     * Set a custom minimum crystal level for a ritual.
     * @param ritualId The ritual's registry name (e.g., "bloodmagic:ritual_green_grove")
     * @param minCrystalLevel Minimum crystal tier required (1=weak, 2=awakened)
     */
    public static void setMinCrystalLevel(String ritualId, int minCrystalLevel) {
        OVERRIDES.put(ritualId, minCrystalLevel);
    }

    /**
     * Get the custom minimum crystal level for a ritual.
     * @param ritualId The ritual's registry name
     * @return The minimum crystal level, or -1 if no override exists
     */
    public static int getMinCrystalLevel(String ritualId) {
        return OVERRIDES.getOrDefault(ritualId, -1);
    }

    /**
     * Remove a custom override for a ritual.
     * @param ritualId The ritual's registry name
     */
    public static void removeOverride(String ritualId) {
        OVERRIDES.remove(ritualId);
    }

    /**
     * Clear all custom overrides.
     */
    public static void clearAll() {
        OVERRIDES.clear();
    }
}
