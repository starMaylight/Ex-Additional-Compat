package com.starmaylight.ex_additional_compat.kubejs.enchanted;

import com.favouriteless.enchanted.common.rites.RiteType;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * KubeJS event for registering custom Enchanted rituals.
 * Fired during startup script phase to collect ritual definitions,
 * which are then registered with Forge's DeferredRegister.
 *
 * Usage in KubeJS startup scripts:
 * <pre>
 * onEvent('ex_additional_compat.rite_registry', event => {
 *     event.create('my_custom_rite')
 *         .power(1000, 10)
 *         .smallCircle(Block.getBlock('minecraft:gold_block'))
 *         .requireItem('minecraft:diamond', 4)
 *         .onExecute(rite => {
 *             let pos = rite.getPos();
 *             let level = rite.getLevel();
 *             // ... custom ritual logic
 *         })
 *         .register();
 * })
 * </pre>
 */
/*+
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
public class RiteRegistryEventJS extends EventJS {

    private static final Map<ResourceLocation, RiteBuilderJS> PENDING_RITES = new LinkedHashMap<>();

    /**
     * Create a new rite builder with the given name.
     * The name will be prefixed with "ex_additional_compat:" as the namespace.
     *
     * @param name The rite name (e.g., "my_custom_rite")
     * @return A RiteBuilderJS for chaining
     */
    public RiteBuilderJS create(String name) {
        RiteBuilderJS builder = new RiteBuilderJS(name);
        // Store with mod namespace
        ResourceLocation id = new ResourceLocation("ex_additional_compat", name);
        PENDING_RITES.put(id, builder);
        return builder;
    }

    /**
     * Get all pending rite registrations collected from scripts.
     */
    public static Map<ResourceLocation, RiteBuilderJS> getPendingRites() {
        return PENDING_RITES;
    }

    /**
     * Clear pending registrations (called after rites are registered).
     */
    public static void clearPending() {
        PENDING_RITES.clear();
    }
}
