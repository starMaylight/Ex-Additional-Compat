package com.starmaylight.ex_additional_compat;

import com.mojang.logging.LogUtils;
import com.starmaylight.ex_additional_compat.capability.ExCompatCapabilityRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Ex_additional_compat.MOD_ID)
public class Ex_additional_compat {

    public static final String MOD_ID = "ex_additional_compat";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Ex_additional_compat() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Ex Additional Compat initializing...");

        // Early-register capabilities in the fallback registry so that
        // MbdCapabilities.get() (via Mixin) can resolve them during Multiblocked's
        // recipe deserialization, which runs in an earlier enqueueWork() than ours.
        // This does NOT add .any blocks or modify the official CAPABILITY_REGISTRY.
        if (ModLoadedHelper.isMultiblockedLoaded()) {
            earlyRegisterCapabilities();
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        if (ModLoadedHelper.isMultiblockedLoaded()) {
            event.enqueueWork(this::registerMultiblockedCapabilities);
        }

        LOGGER.info("Ex Additional Compat setup complete.");
        logLoadedMods();
    }

    /**
     * Register all Multiblocked capabilities.
     * Each registration is guarded by a check for the source mod.
     * This method is called inside enqueueWork() to ensure thread safety.
     */
    private void registerMultiblockedCapabilities() {
        LOGGER.info("Multiblocked detected - registering capabilities...");

        try {
            if (ModLoadedHelper.isCrossroadsLoaded()) {
                registerCrossroadsCapabilities();
            }

            if (ModLoadedHelper.isDraconicAdditionsLoaded()) {
                registerChaosCapability();
            }

            if (ModLoadedHelper.isEnchantedLoaded()) {
                registerAltarPowerCapability();
            }

            if (ModLoadedHelper.isBloodMagicLoaded()) {
                registerBloodMagicCapabilities();
            }
        } catch (Exception e) {
            LOGGER.error("Error registering Multiblocked capabilities", e);
        }
    }

    private void registerCrossroadsCapabilities() {
        LOGGER.info("  Crossroads detected - registering capabilities");
        try {
            com.lowdragmc.multiblocked.api.registry.MbdCapabilities.registerCapability(
                com.starmaylight.ex_additional_compat.capability.heat.HeatMultiblockCapability.CAP);
            LOGGER.info("    - Heat capability registered");

            com.lowdragmc.multiblocked.api.registry.MbdCapabilities.registerCapability(
                com.starmaylight.ex_additional_compat.capability.rotary.RotaryMultiblockCapability.CAP);
            LOGGER.info("    - Rotary capability registered (simplified)");

            com.lowdragmc.multiblocked.api.registry.MbdCapabilities.registerCapability(
                com.starmaylight.ex_additional_compat.capability.beam.BeamMultiblockCapability.CAP);
            LOGGER.info("    - Beam capability registered (IN only)");

            com.lowdragmc.multiblocked.api.registry.MbdCapabilities.registerCapability(
                com.starmaylight.ex_additional_compat.capability.flux.FluxMultiblockCapability.CAP);
            LOGGER.info("    - Flux capability registered");
        } catch (Exception e) {
            LOGGER.error("    Failed to register Crossroads capabilities", e);
        }
    }

    private void registerChaosCapability() {
        LOGGER.info("  Draconic Additions detected - registering Chaos capability");
        try {
            com.lowdragmc.multiblocked.api.registry.MbdCapabilities.registerCapability(
                com.starmaylight.ex_additional_compat.capability.chaos.ChaosMultiblockCapability.CAP);
            LOGGER.info("    - Chaos capability registered");
        } catch (Exception e) {
            LOGGER.error("    Failed to register Chaos capability", e);
        }
    }

    private void registerAltarPowerCapability() {
        LOGGER.info("  Enchanted detected - registering Altar Power capability (IN only)");
        try {
            com.lowdragmc.multiblocked.api.registry.MbdCapabilities.registerCapability(
                com.starmaylight.ex_additional_compat.capability.altar.AltarPowerMultiblockCapability.CAP);
            LOGGER.info("    - Altar Power capability registered");
        } catch (Exception e) {
            LOGGER.error("    Failed to register Altar Power capability", e);
        }
    }

    private void registerBloodMagicCapabilities() {
        LOGGER.info("  Blood Magic detected - registering capabilities");
        try {
            com.lowdragmc.multiblocked.api.registry.MbdCapabilities.registerCapability(
                com.starmaylight.ex_additional_compat.capability.lp.LPMultiblockCapability.CAP);
            LOGGER.info("    - LP capability registered");

            com.lowdragmc.multiblocked.api.registry.MbdCapabilities.registerCapability(
                com.starmaylight.ex_additional_compat.capability.demonwill.DemonWillMultiblockCapability.CAP);
            LOGGER.info("    - Demon Will capability registered");
        } catch (Exception e) {
            LOGGER.error("    Failed to register Blood Magic capabilities", e);
        }
    }

    /**
     * Populate the early fallback capability registry.
     * Called from the constructor, BEFORE any FMLCommonSetupEvent.
     *
     * This ensures MbdCapabilities.get() (via Mixin) can resolve our capabilities
     * during Multiblocked's RecipeMap JSON deserialization (which happens in
     * Multiblocked's enqueueWork, before ours).
     *
     * We do NOT call MbdCapabilities.registerCapability() here because:
     * - registerAnyCapabilityBlocks() runs during block registration and would
     *   create .any blocks for any capability in CAPABILITY_REGISTRY, changing
     *   block registry IDs and breaking existing world saves.
     * - The full registration still happens via enqueueWork() later.
     */
    private void earlyRegisterCapabilities() {
        LOGGER.info("Early-registering capabilities in fallback registry...");
        try {
            if (ModLoadedHelper.isCrossroadsLoaded()) {
                ExCompatCapabilityRegistry.register(
                    com.starmaylight.ex_additional_compat.capability.heat.HeatMultiblockCapability.CAP);
                ExCompatCapabilityRegistry.register(
                    com.starmaylight.ex_additional_compat.capability.rotary.RotaryMultiblockCapability.CAP);
                ExCompatCapabilityRegistry.register(
                    com.starmaylight.ex_additional_compat.capability.beam.BeamMultiblockCapability.CAP);
                ExCompatCapabilityRegistry.register(
                    com.starmaylight.ex_additional_compat.capability.flux.FluxMultiblockCapability.CAP);
            }
            if (ModLoadedHelper.isDraconicAdditionsLoaded()) {
                ExCompatCapabilityRegistry.register(
                    com.starmaylight.ex_additional_compat.capability.chaos.ChaosMultiblockCapability.CAP);
            }
            if (ModLoadedHelper.isEnchantedLoaded()) {
                ExCompatCapabilityRegistry.register(
                    com.starmaylight.ex_additional_compat.capability.altar.AltarPowerMultiblockCapability.CAP);
            }
            if (ModLoadedHelper.isBloodMagicLoaded()) {
                ExCompatCapabilityRegistry.register(
                    com.starmaylight.ex_additional_compat.capability.lp.LPMultiblockCapability.CAP);
                ExCompatCapabilityRegistry.register(
                    com.starmaylight.ex_additional_compat.capability.demonwill.DemonWillMultiblockCapability.CAP);
            }
        } catch (Exception e) {
            LOGGER.error("Error during early capability registration", e);
        }
    }

    private void logLoadedMods() {
        LOGGER.info("=== Ex Additional Compat - Detected Mods ===");
        LOGGER.info("  Multiblocked: {}", ModLoadedHelper.isMultiblockedLoaded());
        LOGGER.info("  KubeJS: {}", ModLoadedHelper.isKubeJSLoaded());
        LOGGER.info("  JEI: {}", ModLoadedHelper.isJEILoaded());
        LOGGER.info("  Draconic Additions: {}", ModLoadedHelper.isDraconicAdditionsLoaded());
        LOGGER.info("  Crossroads: {}", ModLoadedHelper.isCrossroadsLoaded());
        LOGGER.info("  Enchanted: {}", ModLoadedHelper.isEnchantedLoaded());
        LOGGER.info("  Blood Magic: {}", ModLoadedHelper.isBloodMagicLoaded());
        LOGGER.info("  Extra Utilities Reborn: {}", ModLoadedHelper.isExtraUtilLoaded());
        LOGGER.info("=============================================");
    }
}
