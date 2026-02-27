package com.starmaylight.ex_additional_compat.kubejs.enchanted;

import com.favouriteless.enchanted.api.rites.AbstractRite;
import com.favouriteless.enchanted.common.rites.CirclePart;
import com.favouriteless.enchanted.common.rites.RiteType;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A generic AbstractRite subclass that delegates its behavior to JavaScript callbacks.
 * Used by RiteBuilderJS to create customizable rituals from KubeJS scripts.
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
public class ScriptableRite extends AbstractRite {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Consumer<AbstractRite> executeHandler;
    private final Consumer<AbstractRite> tickHandler;

    public ScriptableRite(RiteType<?> riteType, ServerLevel level, BlockPos pos, UUID casterUUID,
                          int power, int powerTick,
                          Map<CirclePart, Block> circles,
                          Map<Item, Integer> items,
                          Map<EntityType<?>, Integer> entities,
                          Consumer<AbstractRite> executeHandler,
                          Consumer<AbstractRite> tickHandler) {
        super(riteType, level, pos, casterUUID, power, powerTick);
        if (circles != null) this.CIRCLES_REQUIRED.putAll(circles);
        if (items != null) this.ITEMS_REQUIRED.putAll(items);
        if (entities != null) this.ENTITIES_REQUIRED.putAll(entities);
        this.executeHandler = executeHandler;
        this.tickHandler = tickHandler;
    }

    @Override
    public void execute() {
        if (executeHandler != null) {
            try {
                executeHandler.accept(this);
            } catch (Exception e) {
                // Log but don't crash the game if script has errors
                LOGGER.error("Error executing scripted rite", e);
            }
        }
        // One-shot rite (no onTick handler): auto-terminate after execute()
        // Ticking rites must call rite.stopExecuting() from their onTick handler when done
        if (tickHandler == null) {
            stopExecuting();
        }
    }

    @Override
    protected void onTick() {
        if (tickHandler != null) {
            try {
                tickHandler.accept(this);
            } catch (Exception e) {
                LOGGER.error("Error in scripted rite tick", e);
                stopExecuting(); // Stop the rite if script errors occur during tick
            }
        } else {
            // Safety: no tick handler but somehow still ticking — stop immediately
            stopExecuting();
        }
    }
}
