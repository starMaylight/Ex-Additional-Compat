package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Copshowium Creation recipes.
 * FluidIngredient input with multiplier and entropy flag.
 *
 * JSON format:
 * {
 *   "input": FluidIngredient,
 *   "mult": float (optional, default 1.0),
 *   "entropy": boolean (optional, default false)
 * }
 *
 * Usage in KubeJS:
 *   event.custom({
 *     type: 'crossroads:copshowium',
 *     input: { fluid: 'crossroads:molten_copper', amount: 144 },
 *     mult: 1.5,
 *     entropy: false
 *   })
 */
public class CopshowiumRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        // Complex recipe; use event.custom() for creation.
    }

    @Override
    public void deserialize() {
        // Data stays in json for Crossroads custom FluidIngredient
    }

    @Override
    public void serialize() {
        // All data stays in json as-is
    }
}
