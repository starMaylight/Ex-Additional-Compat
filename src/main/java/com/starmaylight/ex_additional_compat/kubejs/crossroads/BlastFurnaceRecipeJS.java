package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Blast Furnace recipes.
 * Ingredient -> FluidStack + slag amount
 *
 * JSON format:
 * {
 *   "ingredient": Ingredient,
 *   "output": { "fluid": "...", "amount": N },
 *   "slag": int (optional, default 0)
 * }
 *
 * Usage in KubeJS:
 *   event.recipes.crossroads.cr_blast_furnace(Fluid.of('crossroads:molten_iron', 144), 'minecraft:iron_ore')
 *   event.recipes.crossroads.cr_blast_furnace(Fluid.of('crossroads:molten_iron', 144), 'minecraft:iron_ore').slag(1)
 */
public class BlastFurnaceRecipeJS extends RecipeJS {

    private FluidStackJS outputFluid;

    @Override
    public void create(ListJS args) {
        outputFluid = FluidStackJS.of(args.get(0));
        inputItems.add(parseIngredientItem(args.get(1)));
        if (!json.has("slag")) {
            json.addProperty("slag", 0);
        }
    }

    @Override
    public void deserialize() {
        if (json.has("ingredient")) {
            inputItems.add(parseIngredientItem(json.get("ingredient")));
        }
        if (json.has("output")) {
            outputFluid = FluidStackJS.fromJson(json.getAsJsonObject("output"));
        }
    }

    @Override
    public void serialize() {
        if (serializeInputs && !inputItems.isEmpty()) {
            json.add("ingredient", inputItems.get(0).toJson());
        }
        if (serializeOutputs && outputFluid != null) {
            json.add("output", outputFluid.toJson());
        }
    }

    public BlastFurnaceRecipeJS slag(int amount) {
        json.addProperty("slag", amount);
        save();
        return this;
    }
}
