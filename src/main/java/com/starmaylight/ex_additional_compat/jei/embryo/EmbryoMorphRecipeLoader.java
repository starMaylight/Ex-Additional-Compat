package com.starmaylight.ex_additional_compat.jei.embryo;

import com.Da_Technomancer.crossroads.crafting.CRRecipes;
import com.Da_Technomancer.crossroads.crafting.recipes.EmbryoLabMorphRec;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads Embryo Lab Morph recipes from the RecipeManager for JEI display.
 * Uses Crossroads' registered RecipeType to find all morph recipes,
 * including those added by KubeJS scripts and datapacks.
 */
public final class EmbryoMorphRecipeLoader {

    private static final Logger LOGGER = LogUtils.getLogger();

    private EmbryoMorphRecipeLoader() {}

    /**
     * Load all Embryo Lab Morph recipes from the current world's RecipeManager.
     * Called during JEI recipe registration (after world load).
     */
    public static List<EmbryoMorphRecipe> loadRecipes() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            LOGGER.warn("EmbryoMorphRecipeLoader: Client level not available, cannot load recipes");
            return Collections.emptyList();
        }

        List<EmbryoMorphRecipe> recipes = new ArrayList<>();

        try {
            var recipeManager = level.getRecipeManager();
            List<EmbryoLabMorphRec> morphRecipes =
                    recipeManager.getAllRecipesFor(CRRecipes.EMBRYO_LAB_MORPH_TYPE);

            for (EmbryoLabMorphRec rec : morphRecipes) {
                try {
                    // Skip disabled recipes (IOptionalRecipe)
                    if (!rec.isEnabled()) continue;

                    EmbryoMorphRecipe recipe = new EmbryoMorphRecipe(
                            rec.getId(),
                            rec.getInputMob(),
                            rec.getOutputMob(),
                            rec.getIngr()
                    );
                    recipes.add(recipe);
                    LOGGER.debug("EmbryoMorphRecipeLoader: Loaded {} ({} -> {})",
                            rec.getId(), rec.getInputMob(), rec.getOutputMob());
                } catch (Exception e) {
                    LOGGER.error("EmbryoMorphRecipeLoader: Failed to process recipe {}",
                            rec.getId(), e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("EmbryoMorphRecipeLoader: Failed to load recipes from RecipeManager", e);
        }

        LOGGER.info("EmbryoMorphRecipeLoader: Loaded {} embryo lab morph recipes for JEI",
                recipes.size());
        return Collections.unmodifiableList(recipes);
    }
}
