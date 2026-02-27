package com.starmaylight.ex_additional_compat.jei.exutil;

import net.minecraft.world.item.ItemStack;

/**
 * Data class representing an Extra Utilities Reborn Enchanter recipe.
 * The Enchanter takes two input items and produces one output item,
 * consuming FE over a number of ticks.
 */
public class EnchanterRecipe {

    private final ItemStack inputMain;
    private final ItemStack inputCatalyst;
    private final ItemStack output;
    private final int ticks;
    private final int totalFE;

    public EnchanterRecipe(ItemStack inputMain, ItemStack inputCatalyst, ItemStack output,
                           int ticks, int totalFE) {
        this.inputMain = inputMain;
        this.inputCatalyst = inputCatalyst;
        this.output = output;
        this.ticks = ticks;
        this.totalFE = totalFE;
    }

    public ItemStack getInputMain() {
        return inputMain;
    }

    public ItemStack getInputCatalyst() {
        return inputCatalyst;
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
