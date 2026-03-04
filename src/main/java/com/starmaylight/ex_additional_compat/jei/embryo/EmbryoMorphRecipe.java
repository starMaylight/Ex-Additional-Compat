package com.starmaylight.ex_additional_compat.jei.embryo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Data class representing an Embryo Lab Morph recipe for JEI display.
 * Transforms one entity type into another using a catalyst ingredient.
 */
public class EmbryoMorphRecipe {

    private final ResourceLocation id;
    private final ResourceLocation inputMob;
    private final ResourceLocation outputMob;
    private final Ingredient catalyst;

    public EmbryoMorphRecipe(ResourceLocation id, ResourceLocation inputMob,
                             ResourceLocation outputMob, Ingredient catalyst) {
        this.id = id;
        this.inputMob = inputMob;
        this.outputMob = outputMob;
        this.catalyst = catalyst;
    }

    public ResourceLocation getId() { return id; }
    public ResourceLocation getInputMob() { return inputMob; }
    public ResourceLocation getOutputMob() { return outputMob; }
    public Ingredient getCatalyst() { return catalyst; }
}
