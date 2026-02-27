package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Cooling Coil fuel recipes.
 *
 * JSON format:
 * {
 *   "fuel": Ingredient,
 *   "cooling": int
 * }
 *
 * Usage in KubeJS:
 *   event.recipes.crossroads.cooling(fuelItem, coolingValue)
 *   event.recipes.crossroads.cooling(fuelItem).cooling(1600)
 */
public class CoolingRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        // args[0] = fuel ingredient item
        // args[1] = cooling value (optional, can also set via .cooling())
        inputItems.add(parseIngredientItem(args.get(0)));
        if (args.size() > 1) {
            Object coolingArg = args.get(1);
            if (coolingArg instanceof Number) {
                json.addProperty("cooling", ((Number) coolingArg).intValue());
            } else {
                // Try parsing string as number
                try {
                    json.addProperty("cooling", Integer.parseInt(coolingArg.toString()));
                } catch (NumberFormatException e) {
                    // If not a number, treat second arg as another ingredient (wrong usage)
                    // but don't crash - just ignore it
                }
            }
        }
    }

    @Override
    public void deserialize() {
        if (json.has("fuel")) {
            inputItems.add(parseIngredientItem(json.get("fuel")));
        }
        // "cooling" stays in json as-is
    }

    @Override
    public void serialize() {
        if (serializeInputs && !inputItems.isEmpty()) {
            json.add("fuel", inputItems.get(0).toJson());
        }
        // "cooling" is already in json from create() or originalJson
    }

    public CoolingRecipeJS cooling(int value) {
        json.addProperty("cooling", value);
        save();
        return this;
    }
}
