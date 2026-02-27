package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Fluid Cooling recipes.
 * Fluid (by id or tag) + quantity -> ItemStack, with temperature parameters.
 *
 * Actual Crossroads JSON format:
 * {
 *   "type": "crossroads:fluid_cooling",
 *   "fluid": "crossroads:molten_iron",    // OR "tag": "forge:molten_copper"
 *   "fluid_amount": 90,
 *   "item": "minecraft:iron_ingot",        // plain string, not nested object
 *   "max_temp": 1500,
 *   "temp_change": 100
 * }
 *
 * Usage in KubeJS:
 *   event.recipes.crossroads.fluid_cooling('minecraft:obsidian', 'minecraft:lava', 1000, 300.0)
 *   // args: outputItem, fluidId, fluidAmount, maxTemp
 */
public class FluidCoolingRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        outputItems.add(parseResultItem(args.get(0)));
        // args[1] = fluid id string
        json.addProperty("fluid", args.get(1).toString());
        // args[2] = fluid amount
        json.addProperty("fluid_amount", ((Number) args.get(2)).intValue());
        // args[3] = max_temp
        json.addProperty("max_temp", ((Number) args.get(3)).floatValue());
    }

    @Override
    public void deserialize() {
        // Crossroads uses "item" key as a plain string for the output item
        if (json.has("item")) {
            outputItems.add(parseResultItem(json.get("item")));
        }
        // fluid, fluid_amount, max_temp, temp_change stay in json
    }

    @Override
    public void serialize() {
        if (serializeOutputs && !outputItems.isEmpty()) {
            json.addProperty("item", outputItems.get(0).getId());
        }
        // fluid, fluid_amount, max_temp, temp_change stay in json as-is
    }

    public FluidCoolingRecipeJS maxTemp(float temp) {
        json.addProperty("max_temp", temp);
        save();
        return this;
    }

    public FluidCoolingRecipeJS tempChange(float change) {
        json.addProperty("temp_change", change);
        save();
        return this;
    }

    public FluidCoolingRecipeJS fluidAmount(int amount) {
        json.addProperty("fluid_amount", amount);
        save();
        return this;
    }
}
