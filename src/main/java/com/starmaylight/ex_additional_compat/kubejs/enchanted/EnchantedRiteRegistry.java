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
import java.util.LinkedHashMap;
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
 * Rite activation is handled by RiteActivationHandler, which intercepts Gold Chalk
 * right-clicks via PlayerInteractEvent and checks our registered rites.
 * This avoids mixin into Enchanted's internal RiteTypes class.
 */
/**
 *
 *   Copyright (c) 2023. Favouriteless
 *   Enchanted, a minecraft mod.
 *   GNU GPLv3 License
 *
 *       This file is part of Enchanted.
 *
 *       Enchanted is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       Enchanted is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU General Public License for more details.
 *
 *       You should have received a copy of the GNU General Public License
 *       along with Enchanted.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 **/
@Mod.EventBusSubscriber(modid = "ex_additional_compat", bus = Mod.EventBusSubscriber.Bus.MOD)
public class EnchantedRiteRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Registered RiteType instances for iteration by the mixin */
    private static final List<RiteType<?>> REGISTERED_RITES = new ArrayList<>();

    /** Builder data for each registered rite, keyed by ResourceLocation. Used for JEI display. */
    private static final Map<ResourceLocation, RiteBuilderJS> REGISTERED_BUILDERS = new LinkedHashMap<>();

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
                REGISTERED_BUILDERS.put(id, builder);

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
     * Get all registered rite instances. Used by RiteActivationHandler to
     * check for matching custom rites when Gold Chalk is clicked.
     */
    public static List<RiteType<?>> getRegisteredRites() {
        return Collections.unmodifiableList(REGISTERED_RITES);
    }

    /**
     * Get all registered builder data for JEI display.
     * Keyed by the rite's ResourceLocation, value is the original RiteBuilderJS
     * containing power, circle, item, and entity requirements.
     */
    public static Map<ResourceLocation, RiteBuilderJS> getRegisteredBuilders() {
        return Collections.unmodifiableMap(REGISTERED_BUILDERS);
    }
}
