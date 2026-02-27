package com.starmaylight.ex_additional_compat.jei.arthana;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

/**
 * Data class representing an Enchanted Arthana entity drop.
 * Used by JEI to display which entities drop what items when killed with the Arthana.
 */
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
