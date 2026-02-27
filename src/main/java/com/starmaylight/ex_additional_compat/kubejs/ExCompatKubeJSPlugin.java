package com.starmaylight.ex_additional_compat.kubejs;

import com.starmaylight.ex_additional_compat.ModLoadedHelper;
import com.starmaylight.ex_additional_compat.kubejs.crossroads.*;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public class ExCompatKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void addRecipes(RegisterRecipeHandlersEvent event) {
        if (ModLoadedHelper.isCrossroadsLoaded()) {
            // Group A: Simple recipes (Ingredient -> ItemStack)
            event.register("crossroads:mill", MillRecipeJS::new);
            event.register("crossroads:stamp_mill", StampMillRecipeJS::new);
            event.register("crossroads:ore_cleanser", OreCleanserRecipeJS::new);
            event.register("crossroads:cooling", CoolingRecipeJS::new);
            event.register("crossroads:bobo", BoboRecipeJS::new);

            // Group B: Fluid recipes
            event.register("crossroads:crucible", CrucibleRecipeJS::new);
            event.register("crossroads:cr_blast_furnace", BlastFurnaceRecipeJS::new);
            event.register("crossroads:fluid_cooling", FluidCoolingRecipeJS::new);
            event.register("crossroads:centrifuge", CentrifugeRecipeJS::new);
            event.register("crossroads:copshowium", CopshowiumRecipeJS::new);
            event.register("crossroads:formulation_vat", FormulationVatRecipeJS::new);

            // Group C: Special recipes
            event.register("crossroads:beam_extract", BeamExtractRecipeJS::new);
            event.register("crossroads:beam_transmute", BeamTransmuteRecipeJS::new);
            event.register("crossroads:beam_lens", BeamLensRecipeJS::new);
            event.register("crossroads:alchemy", AlchemyRecipeJS::new);
            event.register("crossroads:detailed_crafter", DetailedCrafterRecipeJS::new);
            event.register("crossroads:reagents", ReagentRecipeJS::new);
            event.register("crossroads:embryo_lab_morph", EmbryoLabMorphRecipeJS::new);
        }
    }

    @Override
    public void addBindings(BindingsEvent event) {
        // Feature 6: Crossroads utility class bindings for KubeJS scripts
        if (ModLoadedHelper.isCrossroadsLoaded()) {
            try {
                // Expose Crossroads beam/alchemy classes for script access
                event.add("BeamUnit", Class.forName("com.Da_Technomancer.crossroads.API.beams.BeamUnit"));
                event.add("EnumBeamAlignments", Class.forName("com.Da_Technomancer.crossroads.API.beams.EnumBeamAlignments"));
            } catch (ClassNotFoundException e) {
                // Crossroads classes not available at runtime; silently skip
            }
        }

        // Feature 11: ExUtil recipe registry bindings for KubeJS scripts
        // Allows adding/removing Enchanter and Resonator recipes from scripts:
        //   ExUtilRecipes.addEnchanterRecipe(recipe)
        //   ExUtilRecipes.removeEnchanterRecipe('extrautilitiesreborn:item_id')
        if (ModLoadedHelper.isExtraUtilLoaded()) {
            event.add("ExUtilRecipes",
                    com.starmaylight.ex_additional_compat.recipe.ExUtilRecipeRegistry.class);
        }

        // Blood Magic ritual crystal override bindings for KubeJS scripts
        // Allows setting custom minimum crystal tier per ritual:
        //   RitualCrystalOverrides.setMinCrystalLevel('bloodmagic:ritual_green_grove', 2)
        if (ModLoadedHelper.isBloodMagicLoaded()) {
            event.add("RitualCrystalOverrides",
                    com.starmaylight.ex_additional_compat.bloodmagic.RitualCrystalOverrides.class);
        }
    }
}
