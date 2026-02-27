package com.starmaylight.ex_additional_compat.jei.exutil;

import net.minecraft.world.item.ItemStack;

/**
 * Data class representing an Extra Utilities Reborn Resonator recipe.
 * The Resonator takes one input item and produces one output item,
 * consuming FE over a number of ticks.
 */
public class ResonatorRecipe {

    private final ItemStack input;
    private final ItemStack output;
    private final int ticks;
    private final int totalFE;

    public ResonatorRecipe(ItemStack input, ItemStack output, int ticks, int totalFE) {
        this.input = input;
        this.output = output;
        this.ticks = ticks;
        this.totalFE = totalFE;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public int getTicks() {
        return ticks;
    }

    public int getTotalFE() {
        return totalFE;
    }

    /**
     * Get the FE consumed per tick.
     */
    public int getFEPerTick() {
        return ticks > 0 ? totalFE / ticks : 0;
    }

    /**
     * Get the processing time in seconds.
     */
    public float getSeconds() {
        return ticks / 20.0f;
    }
}
