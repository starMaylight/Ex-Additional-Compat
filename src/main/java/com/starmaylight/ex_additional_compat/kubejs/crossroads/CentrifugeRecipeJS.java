package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Centrifuge recipes.
 * FluidStack input -> FluidStack output + weighted ItemStack outputs.
 *
 * JSON format:
 * {
 *   "input": { "fluid": "...", "amount": N },
 *   "output_fluid": { "fluid": "...", "amount": N },
 *   "output": [
 *     { "output": ItemStack, "weight": int },
 *     ...
 *   ]
 * }
 *
 * Usage in KubeJS:
 *   event.custom({
 *     type: 'crossroads:centrifuge',
 *     input: { fluid: 'crossroads:dirty_water', amount: 500 },
 *     output_fluid: { fluid: 'minecraft:water', amount: 500 },
 *     output: [
 *       { output: { item: 'minecraft:clay_ball' }, weight: 3 },
 *       { output: { item: 'minecraft:sand' }, weight: 1 }
 *     ]
 *   })
 */
public class CentrifugeRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        // Centrifuge recipes are complex; use event.custom() for creation.
        // This handler mainly enables KubeJS to recognize and modify these recipes.
    }

    @Override
    public void deserialize() {
        // Parse fluid inputs/outputs from json for KubeJS recipe filtering
        // Keep everything in json since Crossroads uses custom FluidIngredient
    }

    @Override
    public void serialize() {
        // All data stays in json as-is for complex Crossroads serialization
    }
}
