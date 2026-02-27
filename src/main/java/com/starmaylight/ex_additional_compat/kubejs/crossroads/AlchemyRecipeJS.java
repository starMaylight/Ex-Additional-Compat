package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Alchemy recipes.
 * Complex recipe with ReagentStack inputs/outputs, temperature ranges, catalyst, etc.
 *
 * JSON format:
 * {
 *   "category": String ("normal"/"destructive"/"elemental", default "normal"),
 *   "reagents": [ { "type": String, "qty": int } ],
 *   "products": [ { "type": String, "qty": int } ],
 *   "min_temp": float (default -300),
 *   "max_temp": float (default 32767),
 *   "heat": float (default 0),
 *   "catalyst": String (default "NONE"),
 *   "charged": boolean (default false),
 *   "data": float/String (depends on category)
 * }
 *
 * Usage in KubeJS:
 *   event.custom({
 *     type: 'crossroads:alchemy',
 *     category: 'normal',
 *     reagents: [{ type: 'sulfuric_acid', qty: 1 }],
 *     products: [{ type: 'salt', qty: 2 }],
 *     min_temp: 100.0,
 *     max_temp: 500.0
 *   })
 */
public class AlchemyRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        // Alchemy recipes are very complex with ReagentStack arrays;
        // use event.custom() for creation.
    }

    @Override
    public void deserialize() {
        // ReagentStack is a Crossroads-specific type
        // Keep all data in json for proper round-tripping
    }

    @Override
    public void serialize() {
        // All data stays in json as-is
    }
}
