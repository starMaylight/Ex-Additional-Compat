package com.starmaylight.ex_additional_compat.jei.arthana;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

/**
 * Data class representing an Enchanted Arthana entity drop.
 * Used by JEI to display which entities drop what items when killed with the Arthana.
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
public class ArthanaDropRecipe {

    private final EntityType<?> entityType;
    private final ItemStack result;

    public ArthanaDropRecipe(EntityType<?> entityType, ItemStack result) {
        this.entityType = entityType;
        this.result = result;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public ItemStack getResult() {
        return result;
    }
}
