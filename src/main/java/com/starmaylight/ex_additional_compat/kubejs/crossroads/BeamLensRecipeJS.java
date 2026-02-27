package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Beam Lens recipes.
 * Ingredient -> BeamMod (beam modification parameters).
 *
 * JSON format:
 * {
 *   "input": Ingredient,
 *   "energy": float (optional, default 1.0),
 *   "potential": float (optional, default 1.0),
 *   "stability": float (optional, default 1.0),
 *   "void": float (optional, default 1.0),
 *   "void_convert": float (optional, default 0.0),
 *   "transmute_result": ItemStack (optional),
 *   "transmute_alignment": String (optional),
 *   "transmute_void": boolean (optional, default false)
 * }
 *
 * Usage in KubeJS:
 *   // Positional args: (input, energy, potential, stability, void)
 *   event.recipes.crossroads.beam_lens('minecraft:prismarine_shard', 4, 4, 0, 0)
 *   // Builder pattern:
 *   event.recipes.crossroads.beam_lens('minecraft:glass')
 *       .energy(1.0).potential(2.0).stability(0.5)
 *   // Mixed: positional + builder
 *   event.recipes.crossroads.beam_lens('minecraft:prismarine_shard', 4, 4, 0, 0).voidConvert(0.5)
 */
public class BeamLensRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        inputItems.add(parseIngredientItem(args.get(0)));

        // Parse optional positional args: energy, potential, stability, void
        if (args.size() > 1) {
            json.addProperty("energy", ((Number) args.get(1)).floatValue());
        }
        if (args.size() > 2) {
            json.addProperty("potential", ((Number) args.get(2)).floatValue());
        }
        if (args.size() > 3) {
            json.addProperty("stability", ((Number) args.get(3)).floatValue());
        }
        if (args.size() > 4) {
            json.addProperty("void", ((Number) args.get(4)).floatValue());
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
        // beam mod parameters stay in json
    }

    public BeamLensRecipeJS energy(float value) {
        json.addProperty("energy", value);
        save();
        return this;
    }

    public BeamLensRecipeJS potential(float value) {
        json.addProperty("potential", value);
        save();
        return this;
    }

    public BeamLensRecipeJS stability(float value) {
        json.addProperty("stability", value);
        save();
        return this;
    }

    public BeamLensRecipeJS beamVoid(float value) {
        json.addProperty("void", value);
        save();
        return this;
    }

    public BeamLensRecipeJS voidConvert(float value) {
        json.addProperty("void_convert", value);
        save();
        return this;
    }
}
