package com.starmaylight.ex_additional_compat.kubejs.enchanted;

import com.favouriteless.enchanted.common.rites.RiteType;
import com.mojang.logging.LogUtils;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Handles registration of custom Enchanted rites defined through KubeJS scripts.
 *
 * Flow:
 * 1. Forge fires RegistryEvent.Register for RiteType (Enchanted's custom registry)
 * 2. We fire the KubeJS startup event to collect rite definitions from scripts
 * 3. We register the collected rites directly into the IForgeRegistry
 *
 * This happens at RegistryEvent time, which is AFTER KubeJS startup scripts have loaded,
 * solving the timing issue with DeferredRegister (which requires entries at mod constructor time).
 *
 * Note: RiteTypes.getRiteAt() only checks Enchanted's own DeferredRegister entries.
 * A mixin (MixinRiteTypes) extends getRiteAt() to also check our entries.
 */
@Mod.EventBusSubscriber(modid = "ex_additional_compat", bus = Mod.EventBusSubscriber.Bus.MOD)
public class EnchantedRiteRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Registered RiteType instances for iteration by the mixin */
    private static final List<RiteType<?>> REGISTERED_RITES = new ArrayList<>();

    /**
     * Called when Forge fires the RegistryEvent.Register for RiteType.
     * At this point KubeJS startup scripts have already been loaded.
     */
    @SubscribeEvent
    public static void onRegisterRiteTypes(RegistryEvent.Register<RiteType<?>> event) {
        LOGGER.info("[ExCompat Rite] RegistryEvent.Register<RiteType> fired");

        IForgeRegistry<RiteType<?>> registry = event.getRegistry();

        // Fire KubeJS event to collect rite definitions
        try {
            new RiteRegistryEventJS().post(ScriptType.STARTUP, "ex_additional_compat.rite_registry");
            LOGGER.info("[ExCompat Rite] Fired rite_registry KubeJS event");
        } catch (Exception e) {
            LOGGER.error("[ExCompat Rite] Failed to fire rite_registry event", e);
            return;
        }

        // Register all collected rites
        Map<ResourceLocation, RiteBuilderJS> pendingRites = RiteRegistryEventJS.getPendingRites();
        if (pendingRites.isEmpty()) {
            LOGGER.info("[ExCompat Rite] No custom rites registered by scripts");
            return;
        }

        for (Map.Entry<ResourceLocation, RiteBuilderJS> entry : pendingRites.entrySet()) {
            ResourceLocation id = entry.getKey();
            RiteBuilderJS builder = entry.getValue();

            try {
                RiteType<ScriptableRite> riteType = builder.buildRiteType();
                riteType.setRegistryName(id);
                registry.register(riteType);
                REGISTERED_RITES.add(riteType);

                LOGGER.info("[ExCompat Rite] Registered custom rite: {} (power={}, powerTick={})",
                        id, builder.getPower(), builder.getPowerTick());
            } catch (Exception e) {
                LOGGER.error("[ExCompat Rite] Failed to register rite: {}", id, e);
            }
        }

        LOGGER.info("[ExCompat Rite] Registered {} custom rites total", REGISTERED_RITES.size());
        RiteRegistryEventJS.clearPending();
    }

    /**
     * Get all registered rite instances. Used by the MixinRiteTypes to include
     * our rites in the getRiteAt() scan.
     */
    public static List<RiteType<?>> getRegisteredRites() {
        return Collections.unmodifiableList(REGISTERED_RITES);
    }
}
