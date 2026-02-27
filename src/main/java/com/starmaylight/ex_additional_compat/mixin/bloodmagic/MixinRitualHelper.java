package com.starmaylight.ex_additional_compat.mixin.bloodmagic;

import com.mojang.logging.LogUtils;
import com.starmaylight.ex_additional_compat.bloodmagic.RitualCrystalOverrides;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wayoftime.bloodmagic.ritual.Ritual;
import wayoftime.bloodmagic.util.helper.RitualHelper;

/**
 * Mixin to override the crystal tier check in Blood Magic rituals.
 *
 * By default, Blood Magic restricts certain rituals to higher-tier
 * activation crystals (WEAK=1, AWAKENED=2). This mixin allows
 * KubeJS scripts to set custom minimum crystal tier requirements
 * per ritual via RitualCrystalOverrides.
 *
 * Target: wayoftime.bloodmagic.util.helper.RitualHelper
 * Method: canCrystalActivate(Ritual, int) -> boolean
 *
 * The original method checks:
 *   1. ritual.getCrystalLevel() <= crystalLevel
 *   2. RitualManager.enabled(ritual)
 * This mixin replaces check #1 with a configurable override system.
 */
@Mixin(value = RitualHelper.class, remap = false)
public abstract class MixinRitualHelper {

    private static final Logger EXCOMPAT_LOGGER = LogUtils.getLogger();

    /**
     * Inject at HEAD of canCrystalActivate to check custom overrides.
     * If a ritual has an override, use the custom minimum crystal level.
     * Otherwise, fall through to the original Blood Magic logic.
     */
    @Inject(method = "canCrystalActivate", at = @At("HEAD"), cancellable = true)
    private static void excompat$overrideCrystalTier(
            Ritual ritual, int crystalLevel,
            CallbackInfoReturnable<Boolean> cir) {
        String ritualId = ritual.getName();
        int overrideLevel = RitualCrystalOverrides.getMinCrystalLevel(ritualId);
        EXCOMPAT_LOGGER.info("[ExCompat] canCrystalActivate called: ritual='{}', crystalLevel={}, overrideLevel={}",
                ritualId, crystalLevel, overrideLevel);
        if (overrideLevel >= 0) {
            boolean allowed = crystalLevel >= overrideLevel;
            EXCOMPAT_LOGGER.info("[ExCompat] Override active: crystalLevel({}) >= overrideLevel({}) = {}",
                    crystalLevel, overrideLevel, allowed);
            cir.setReturnValue(allowed);
        }
        // No override: fall through to original Blood Magic logic
    }
}
