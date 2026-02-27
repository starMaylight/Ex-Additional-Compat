package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Formulation Vat recipes.
 * FluidIngredient + Ingredient -> FluidStack
 *
 * Actual Crossroads JSON format:
 * {
 *   "input_fluid": { "fluid": "water" } or { "tag": "crossroads:liquid_fat" },
 *   "fluid_amount": 100,           // NOTE: separate from input_fluid object!
 *   "input_item": { "tag": "forge:dusts/salt" },
 *   "output": { "fluid": "crossroads:nutrient_solution", "amount": 100 }
 * }
 *
 * Usage in KubeJS:
 *   event.custom({
 *     type: 'crossroads:formulation_vat',
 *     input_fluid: { fluid: 'minecraft:water' },
 *     fluid_amount: 200,
 *     input_item: { item: 'minecraft:bone_meal' },
 *     output: { fluid: 'crossroads:fertilizer_solution', amount: 200 }
 *   })
 */
public class FormulationVatRecipeJS extends RecipeJS {

    private FluidStackJS outputFluid;

    @Override
    public void create(ListJS args) {
        // args[0] = output fluid, args[1] = input item, args[2] = input fluid
        outputFluid = FluidStackJS.of(args.get(0));
        inputItems.add(parseIngredientItem(args.get(1)));
        if (args.size() > 2) {
            json.add("input_fluid", FluidInputHelper.toJson(args.get(2)));
        }
    }

    @Override
    public void deserialize() {
        if (json.has("input_item")) {
            inputItems.add(parseIngredientItem(json.get("input_item")));
        }
        if (json.has("output")) {
            outputFluid = FluidStackJS.fromJson(json.getAsJsonObject("output"));
        }
        // input_fluid stays in json
    }

    @Override
    public void serialize() {
        if (serializeInputs && !inputItems.isEmpty()) {
            json.add("input_item", inputItems.get(0).toJson());
        }
        if (serializeOutputs && outputFluid != null) {
            json.add("output", outputFluid.toJson());
        }
        // input_fluid stays in json as-is
    }
}
