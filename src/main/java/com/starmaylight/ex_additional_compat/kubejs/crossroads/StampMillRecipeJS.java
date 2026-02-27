package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Stamp Mill recipes.
 * Extends SingleIngrRecipe pattern: single ingredient -> single output.
 *
 * JSON format:
 * {
 *   "ingredient": Ingredient,
 *   "output": ItemStack
 * }
 *
 * Usage in KubeJS:
 *   event.recipes.crossroads.stamp_mill(output, input)
 */
public class StampMillRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        outputItems.add(parseResultItem(args.get(0)));
        inputItems.add(parseIngredientItem(args.get(1)));
    }

    @Override
    public void deserialize() {
        inputItems.add(parseIngredientItem(json.get("ingredient")));
        outputItems.add(parseResultItem(json.get("output")));
    }

    @Override
    public void serialize() {
        if (serializeInputs && !inputItems.isEmpty()) {
            json.add("ingredient", inputItems.get(0).toJson());
        }
        if (serializeOutputs && !outputItems.isEmpty()) {
            json.add("output", outputItems.get(0).toResultJson());
        }
    }
}
