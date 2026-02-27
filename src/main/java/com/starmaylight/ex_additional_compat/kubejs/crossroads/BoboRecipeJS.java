package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Bobo's Arcane Workbench recipes.
 * Takes 3 inputs (each as array) and produces 1 output.
 *
 * Actual Crossroads JSON format:
 * {
 *   "input_a": [ { "tag": "forge:rods/wooden" } ],
 *   "input_b": [ { "tag": "crossroads:ingots/copshowium" } ],
 *   "input_c": [ { "type": "forge:nbt", "item": "...", "nbt": {...} } ],
 *   "output": { "item": "...", "count": N }
 * }
 *
 * Usage in KubeJS:
 *   event.recipes.crossroads.bobo(output, inputA, inputB, inputC)
 */
public class BoboRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        outputItems.add(parseResultItem(args.get(0)));
        inputItems.add(parseIngredientItem(args.get(1)));
        inputItems.add(parseIngredientItem(args.get(2)));
        inputItems.add(parseIngredientItem(args.get(3)));
    }

    private void parseInputSlot(String key) {
        if (json.has(key)) {
            JsonElement el = json.get(key);
            if (el.isJsonArray() && el.getAsJsonArray().size() > 0) {
                inputItems.add(parseIngredientItem(el.getAsJsonArray().get(0)));
            } else {
                inputItems.add(parseIngredientItem(el));
            }
        }
    }

    @Override
    public void deserialize() {
        parseInputSlot("input_a");
        parseInputSlot("input_b");
        parseInputSlot("input_c");
        if (json.has("output")) {
            outputItems.add(parseResultItem(json.get("output")));
        }
    }

    @Override
    public void serialize() {
        if (serializeInputs) {
            // Crossroads expects each input as an array
            if (inputItems.size() > 0) {
                JsonArray arr = new JsonArray();
                arr.add(inputItems.get(0).toJson());
                json.add("input_a", arr);
            }
            if (inputItems.size() > 1) {
                JsonArray arr = new JsonArray();
                arr.add(inputItems.get(1).toJson());
                json.add("input_b", arr);
            }
            if (inputItems.size() > 2) {
                JsonArray arr = new JsonArray();
                arr.add(inputItems.get(2).toJson());
                json.add("input_c", arr);
            }
        }
        if (serializeOutputs && !outputItems.isEmpty()) {
            json.add("output", outputItems.get(0).toResultJson());
        }
    }
}
