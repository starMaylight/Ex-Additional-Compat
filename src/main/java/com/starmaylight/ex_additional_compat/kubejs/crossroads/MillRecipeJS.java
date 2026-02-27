package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Millstone recipes.
 *
 * JSON format:
 * {
 *   "input": Ingredient,
 *   "output": ItemStack or ItemStack[] (up to 3)
 * }
 *
 * Usage in KubeJS:
 *   event.recipes.crossroads.mill(output, input)
 *   event.recipes.crossroads.mill([output1, output2], input)
 */
public class MillRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        // args[0] = output (single or array), args[1] = input
        ListJS outputList = ListJS.orSelf(args.get(0));
        for (Object o : outputList) {
            outputItems.add(parseResultItem(o));
        }
        inputItems.add(parseIngredientItem(args.get(1)));
    }

    @Override
    public void deserialize() {
        inputItems.add(parseIngredientItem(json.get("input")));
        JsonElement outputEl = json.get("output");
        if (outputEl != null && outputEl.isJsonArray()) {
            JsonArray arr = outputEl.getAsJsonArray();
            for (JsonElement e : arr) {
                outputItems.add(parseResultItem(e));
            }
        } else if (outputEl != null) {
            outputItems.add(parseResultItem(outputEl));
        }
    }

    @Override
    public void serialize() {
        if (serializeInputs && !inputItems.isEmpty()) {
            json.add("input", inputItems.get(0).toJson());
        }
        if (serializeOutputs && !outputItems.isEmpty()) {
            if (outputItems.size() == 1) {
                json.add("output", outputItems.get(0).toResultJson());
            } else {
                JsonArray arr = new JsonArray();
                for (ItemStackJS stack : outputItems) {
                    arr.add(stack.toResultJson());
                }
                json.add("output", arr);
            }
        }
    }
}
