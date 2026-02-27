package com.starmaylight.ex_additional_compat.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin config plugin that conditionally applies mixins based on
 * whether the target mod is actually loaded.
 *
 * Note: We cannot use ModList.get().isLoaded() here because
 * Mixin plugins are loaded before the Forge mod loading system.
 * Instead, we use the Mixin service's classloader which can see
 * all mod jars on the transforming classpath.
 */
public class ExCompatMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("ExCompat-MixinPlugin");

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("[ExCompat MixinPlugin] onLoad: package={}", mixinPackage);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean result;

        // Blood Magic mixins: only apply if Blood Magic is present
        if (mixinClassName.contains(".bloodmagic.")) {
            result = isClassPresent("wayoftime.bloodmagic.util.helper.RitualHelper");
            LOGGER.info("[ExCompat MixinPlugin] shouldApplyMixin: {} -> target={} -> bloodmagic present={}",
                    mixinClassName, targetClassName, result);
            return result;
        }

        // Extra Utilities Reborn mixins: only apply if ExUtil is present
        if (mixinClassName.contains(".exutil.")) {
            result = isClassPresent(
                    "inzhefop.extrautilitiesrebirth.procedures.EnchanterUpdateTickProcedure");
            LOGGER.info("[ExCompat MixinPlugin] shouldApplyMixin: {} -> target={} -> exutil present={}",
                    mixinClassName, targetClassName, result);
            return result;
        }

        // Enchanted mixins: only apply if Enchanted is present
        if (mixinClassName.contains(".enchanted.")) {
            result = isClassPresent(
                    "com.favouriteless.enchanted.common.init.registry.RiteTypes");
            LOGGER.info("[ExCompat MixinPlugin] shouldApplyMixin: {} -> target={} -> enchanted present={}",
                    mixinClassName, targetClassName, result);
            return result;
        }

        // Multiblocked mixins: only apply if Multiblocked is present
        if (mixinClassName.contains(".multiblocked.")) {
            result = isClassPresent(
                    "com.lowdragmc.multiblocked.api.capability.MultiblockCapability");
            LOGGER.info("[ExCompat MixinPlugin] shouldApplyMixin: {} -> target={} -> multiblocked present={}",
                    mixinClassName, targetClassName, result);
            return result;
        }

        // All other mixins: always apply
        LOGGER.info("[ExCompat MixinPlugin] shouldApplyMixin: {} -> target={} -> always apply",
                mixinClassName, targetClassName);
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        LOGGER.info("[ExCompat MixinPlugin] acceptTargets: myTargets={}", myTargets);
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) {
        LOGGER.info("[ExCompat MixinPlugin] preApply: mixin={} -> target={}", mixinClassName, targetClassName);
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) {
        LOGGER.info("[ExCompat MixinPlugin] postApply: mixin={} -> target={}", mixinClassName, targetClassName);
    }

    /**
     * Check if a class is present using ONLY resource-based lookups.
     *
     * IMPORTANT: We must NOT use Class.forName() here, because loading a class
     * via Class.forName in the Mixin plugin's classloader can cause the class
     * to be loaded BEFORE the TransformingClassLoader processes it, which
     * prevents Mixin transformations from being applied.
     *
     * Resource-based lookup only checks if the .class file exists on the
     * classpath without actually loading or linking the class.
     */
    private static boolean isClassPresent(String className) {
        String resourcePath = className.replace('.', '/') + ".class";

        // Strategy 1: Plugin's own classloader resource lookup
        if (ExCompatMixinPlugin.class.getClassLoader().getResource(resourcePath) != null) {
            LOGGER.info("[ExCompat MixinPlugin]   isClassPresent({}) -> found via plugin resource lookup", className);
            return true;
        }

        // Strategy 2: Thread context classloader resource lookup
        ClassLoader ctxCl = Thread.currentThread().getContextClassLoader();
        if (ctxCl != null && ctxCl.getResource(resourcePath) != null) {
            LOGGER.info("[ExCompat MixinPlugin]   isClassPresent({}) -> found via context resource lookup", className);
            return true;
        }

        LOGGER.warn("[ExCompat MixinPlugin]   isClassPresent({}) -> NOT FOUND by any strategy!", className);
        return false;
    }
}
