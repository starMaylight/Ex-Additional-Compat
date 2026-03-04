package com.starmaylight.ex_additional_compat.jei.rite;

import com.favouriteless.enchanted.common.rites.CirclePart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Map;

/**
 * Data class representing a custom Enchanted rite for JEI display.
 * Contains all requirements needed to set up and activate the ritual.
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
public class RiteRecipe {

    private final ResourceLocation id;
    private final int power;
    private final int powerTick;
    private final Map<CirclePart, Block> circles;
    private final Map<Item, Integer> items;
    private final Map<EntityType<?>, Integer> entities;

    public RiteRecipe(ResourceLocation id, int power, int powerTick,
                      Map<CirclePart, Block> circles,
                      Map<Item, Integer> items,
                      Map<EntityType<?>, Integer> entities) {
        this.id = id;
        this.power = power;
        this.powerTick = powerTick;
        this.circles = circles;
        this.items = items;
        this.entities = entities;
    }

    public ResourceLocation getId() { return id; }
    public int getPower() { return power; }
    public int getPowerTick() { return powerTick; }
    public Map<CirclePart, Block> getCircles() { return circles; }
    public Map<Item, Integer> getItems() { return items; }
    public Map<EntityType<?>, Integer> getEntities() { return entities; }
}
