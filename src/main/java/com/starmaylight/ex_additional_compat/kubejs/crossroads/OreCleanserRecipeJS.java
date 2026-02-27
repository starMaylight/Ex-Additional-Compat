package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import com.google.gson.JsonArray;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Ore Cleanser recipes.
 * Single ingredient (as array) -> single output.
 *
 * Actual Crossroads JSON format:
 * {
 *   "ingredient": [ { "type": "forge:nbt", "item": "...", "nbt": {...} } ],
 *   "output": { "item": "...", "count": N, "nbt": {...} }
 * }
 *
 * Usage in KubeJS:
 *   event.recipes.crossroads.ore_cleanser(output, input)
 */
public class OreCleanserRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        outputItems.add(parseResultItem(args.get(0)));
        inputItems.add(parseIngredientItem(args.get(1)));
    }

    @Override
    public void deserialize() {
        if (json.has("ingredient")) {
            // Crossroads uses ingredient as an array; parse first element
            var ingredientEl = json.get("ingredient");
            if (ingredientEl.isJsonArray() && ingredientEl.getAsJsonArray().size() > 0) {
                inputItems.add(parseIngredientItem(ingredientEl.getAsJsonArray().get(0)));
            } else {
                inputItems.add(parseIngredientItem(ingredientEl));
            }
        }
        if (json.has("output")) {
            outputItems.add(parseResultItem(json.get("output")));
        }
    }

    @Override
    public void serialize() {
        if (serializeInputs && !inputItems.isEmpty()) {
            // Crossroads expects ingredient as an array
            JsonArray arr = new JsonArray();
            arr.add(inputItems.get(0).toJson());
            json.add("ingredient", arr);
        }
        if (serializeOutputs && !outputItems.isEmpty()) {
            json.add("output", outputItems.get(0).toResultJson());
        }
    }
}
