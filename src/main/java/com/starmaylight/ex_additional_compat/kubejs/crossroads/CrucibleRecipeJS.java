package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Crucible recipes.
 * Ingredient -> FluidStack
 *
 * JSON format:
 * {
 *   "input": Ingredient,
 *   "output": { "fluid": "...", "amount": N }
 * }
 *
 * Usage in KubeJS:
 *   event.recipes.crossroads.crucible(Fluid.of('minecraft:lava', 1000), 'minecraft:cobblestone')
 */
public class CrucibleRecipeJS extends RecipeJS {

    private FluidStackJS outputFluid;

    @Override
    public void create(ListJS args) {
        outputFluid = FluidStackJS.of(args.get(0));
        inputItems.add(parseIngredientItem(args.get(1)));
    }

    @Override
    public void deserialize() {
        if (json.has("input")) {
            inputItems.add(parseIngredientItem(json.get("input")));
        }
        if (json.has("output")) {
            outputFluid = FluidStackJS.fromJson(json.getAsJsonObject("output"));
        }
    }

    @Override
    public void serialize() {
        if (serializeInputs && !inputItems.isEmpty()) {
            json.add("input", inputItems.get(0).toJson());
        }
        if (serializeOutputs && outputFluid != null) {
            json.add("output", outputFluid.toJson());
        }
    }
}
