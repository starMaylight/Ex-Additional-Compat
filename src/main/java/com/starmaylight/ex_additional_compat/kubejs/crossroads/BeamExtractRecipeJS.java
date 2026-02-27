package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Beam Extraction recipes.
 * Ingredient -> BeamUnit (energy, potential, stability, void) with duration.
 *
 * JSON format:
 * {
 *   "input": Ingredient,
 *   "energy": int (optional, default 0),
 *   "potential": int (optional, default 0),
 *   "stability": int (optional, default 0),
 *   "void": int (optional, default 0),
 *   "duration": int (optional, default 1)
 * }
 *
 * Usage in KubeJS:
 *   // Positional args: (input, energy, potential, stability, void)
 *   event.recipes.crossroads.beam_extract('minecraft:glowstone_dust', 16, 0, 0, 0)
 *   // Builder pattern:
 *   event.recipes.crossroads.beam_extract('minecraft:redstone')
 *       .energy(0).potential(4).stability(0).beamVoid(0).duration(1)
 *   // Mixed: positional + builder
 *   event.recipes.crossroads.beam_extract('minecraft:glowstone_dust', 16, 0, 0, 0).duration(5)
 */
public class BeamExtractRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        inputItems.add(parseIngredientItem(args.get(0)));

        // Parse optional positional args: energy, potential, stability, void
        if (args.size() > 1) {
            json.addProperty("energy", ((Number) args.get(1)).intValue());
        }
        if (args.size() > 2) {
            json.addProperty("potential", ((Number) args.get(2)).intValue());
        }
        if (args.size() > 3) {
            json.addProperty("stability", ((Number) args.get(3)).intValue());
        }
        if (args.size() > 4) {
            json.addProperty("void", ((Number) args.get(4)).intValue());
        }

        if (!json.has("duration")) {
            json.addProperty("duration", 1);
        }
    }

    @Override
    public void deserialize() {
        inputItems.add(parseIngredientItem(json.get("input")));
    }

    @Override
    public void serialize() {
        if (serializeInputs && !inputItems.isEmpty()) {
            json.add("input", inputItems.get(0).toJson());
        }
        // energy, potential, stability, void, duration stay in json
    }

    public BeamExtractRecipeJS energy(int value) {
        json.addProperty("energy", value);
        save();
        return this;
    }

    public BeamExtractRecipeJS potential(int value) {
        json.addProperty("potential", value);
        save();
        return this;
    }

    public BeamExtractRecipeJS stability(int value) {
        json.addProperty("stability", value);
        save();
        return this;
    }

    public BeamExtractRecipeJS beamVoid(int value) {
        json.addProperty("void", value);
        save();
        return this;
    }

    public BeamExtractRecipeJS duration(int value) {
        json.addProperty("duration", value);
        save();
        return this;
    }
}
