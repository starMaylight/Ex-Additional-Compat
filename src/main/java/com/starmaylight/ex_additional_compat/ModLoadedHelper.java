package com.starmaylight.ex_additional_compat;

import net.minecraftforge.fml.ModList;

/**
 * Utility class to check if optional dependency mods are loaded.
 * All checks are cached on first access to avoid repeated lookups.
 */
public final class ModLoadedHelper {

    private ModLoadedHelper() {}

    // Integration targets
    public static boolean isMultiblockedLoaded() {
        return ModList.get().isLoaded("multiblocked");
    }

    public static boolean isKubeJSLoaded() {
        return ModList.get().isLoaded("kubejs");
    }

    public static boolean isJEILoaded() {
        return ModList.get().isLoaded("jei");
    }

    // Source mods
    public static boolean isDraconicAdditionsLoaded() {
        return ModList.get().isLoaded("draconicadditions");
    }

    public static boolean isCrossroadsLoaded() {
        return ModList.get().isLoaded("crossroads");
    }

    public static boolean isEnchantedLoaded() {
        return ModList.get().isLoaded("enchanted");
    }

    public static boolean isBloodMagicLoaded() {
        return ModList.get().isLoaded("bloodmagic");
    }

    public static boolean isExtraUtilLoaded() {
        return ModList.get().isLoaded("extrautilitiesrebirth");
    }
}
