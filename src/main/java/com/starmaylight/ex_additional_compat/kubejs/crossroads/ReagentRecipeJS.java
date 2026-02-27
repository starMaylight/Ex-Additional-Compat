package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Reagent definition recipes.
 * Defines alchemical reagents with their properties.
 *
 * JSON format:
 * {
 *   "id": String (reagent name, lowercased, spaces -> underscores),
 *   "melting": float/String (temperature or "never", default -275),
 *   "boiling": float/String (temperature or "never", default -274),
 *   "item": String (item tag, default "crossroads:empty"),
 *   "fluid": { FluidIngredient } (optional),
 *   "vessel": String (ContainRequirements, default "none"),
 *   "effect": String (IAlchEffect name, default "none"),
 *   "flame": String (flame radius type, default "none"),
 *   "color": int/object (color value or per-phase colors)
 * }
 *
 * Usage in KubeJS:
 *   event.custom({
 *     type: 'crossroads:reagents',
 *     id: 'custom_reagent',
 *     item: 'forge:dusts/redstone',
 *     melting: 100.0,
 *     boiling: 500.0,
 *     color: 16711680
 *   })
 */
public class ReagentRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        // Reagent definitions are complex; use event.custom() for creation.
    }

    @Override
    public void deserialize() {
        // Crossroads-specific types; keep in json
    }

    @Override
    public void serialize() {
        // All data stays in json as-is
    }
}
