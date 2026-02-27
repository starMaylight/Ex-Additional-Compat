package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Detailed Crafter recipes.
 * Extends ShapedRecipe with an additional EnumPath requirement.
 *
 * JSON format:
 * {
 *   "path": String (EnumPath name),
 *   "pattern": [...],
 *   "key": {...},
 *   "result": ItemStack
 * }
 *
 * Usage in KubeJS:
 *   event.custom({
 *     type: 'crossroads:detailed_crafter',
 *     path: 'TECHNOMANCY',
 *     pattern: ['ABA', 'BCB', 'ABA'],
 *     key: { A: { item: 'minecraft:iron_ingot' }, B: { item: 'minecraft:gold_ingot' }, C: { item: 'minecraft:diamond' } },
 *     result: { item: 'crossroads:some_item' }
 *   })
 */
public class DetailedCrafterRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        // Delegates to vanilla shaped recipe format + path field
        // Use event.custom() for creation.
    }

    @Override
    public void deserialize() {
        // Shaped recipe parsing is handled by vanilla;
        // path is a custom field in json
    }

    @Override
    public void serialize() {
        // All data stays in json as-is
    }
}
