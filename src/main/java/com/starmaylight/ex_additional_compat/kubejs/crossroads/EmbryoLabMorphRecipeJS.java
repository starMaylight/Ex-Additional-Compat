package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Embryo Lab Morph recipes.
 * Transforms one entity type into another with a catalyst ingredient.
 *
 * JSON format (Crossroads native):
 * {
 *   "input_mob": String (ResourceLocation of input entity),
 *   "output_mob": String (ResourceLocation of output entity),
 *   "input": Ingredient (required; single ingredient or array = alternative matches)
 * }
 *
 * Note: "input" is a required single Minecraft Ingredient. If a JsonArray, vanilla
 * Ingredient.fromJson() treats elements as alternative accepted items (OR logic).
 * If "input" key is missing, Crossroads falls back to parsing the ENTIRE json
 * as an ingredient (allowDirect=true), causing "Unknown ingredient type" errors.
 *
 * Usage in KubeJS:
 *   // Single catalyst
 *   event.recipes.crossroads.embryo_lab_morph('minecraft:zombie', 'minecraft:skeleton', 'minecraft:bone')
 *   // args[2] (catalyst) is required by Crossroads
 */
public class EmbryoLabMorphRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        // args[0] = input_mob, args[1] = output_mob, args[2] = catalyst item (optional)
        json.addProperty("input_mob", args.get(0).toString());
        json.addProperty("output_mob", args.get(1).toString());
        if (args.size() > 2) {
            inputItems.add(parseIngredientItem(args.get(2)));
        }
    }

    @Override
    public void deserialize() {
        if (json.has("input")) {
            inputItems.add(parseIngredientItem(json.get("input")));
        }
        // input_mob and output_mob stay in json
    }

    @Override
    public void serialize() {
        if (serializeInputs) {
            if (!inputItems.isEmpty()) {
                json.add("input", inputItems.get(0).toJson());
            } else {
                // Must output "input" key to prevent Crossroads from parsing
                // the entire json as an Ingredient (allowDirect=true fallback).
                // Use minecraft:barrier as a fallback placeholder.
                JsonObject fallback = new JsonObject();
                fallback.addProperty("item", "minecraft:barrier");
                json.add("input", fallback);
            }
        }
        // input_mob and output_mob stay in json
    }
}
