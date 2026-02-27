package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;

/**
 * KubeJS recipe handler for Crossroads Beam Transmutation recipes.
 * Block -> Block transmutation with beam alignment requirements.
 *
 * JSON format:
 * {
 *   "alignment": String (EnumBeamAlignments name),
 *   "void": boolean (optional, default false),
 *   "power": int (optional, default 1),
 *   "input": BlockIngredient,
 *   "output": String (block registry name)
 * }
 *
 * Usage in KubeJS:
 *   event.custom({
 *     type: 'crossroads:beam_transmute',
 *     alignment: 'EQUILIBRIUM',
 *     power: 4,
 *     input: 'minecraft:stone',
 *     output: 'minecraft:gold_block'
 *   })
 */
public class BeamTransmuteRecipeJS extends RecipeJS {

    @Override
    public void create(ListJS args) {
        // Complex block-to-block recipe; use event.custom() for creation.
        // All data stays in json.
    }

    @Override
    public void deserialize() {
        // Block ingredients are not standard KubeJS items
        // Keep all data in json
    }

    @Override
    public void serialize() {
        // All data stays in json as-is
    }
}
