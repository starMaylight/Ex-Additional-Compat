package com.starmaylight.ex_additional_compat.jei.rite;

import com.mojang.logging.LogUtils;
import com.starmaylight.ex_additional_compat.kubejs.enchanted.EnchantedRiteRegistry;
import com.starmaylight.ex_additional_compat.kubejs.enchanted.RiteBuilderJS;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Loads custom rite data from EnchantedRiteRegistry for JEI display.
 * Called during JEI recipe registration.
 */
public final class RiteRecipeLoader {

    private static final Logger LOGGER = LogUtils.getLogger();

    private RiteRecipeLoader() {}

    /**
     * Load all registered custom rites as RiteRecipe instances for JEI.
     */
    public static List<RiteRecipe> loadRecipes() {
        Map<ResourceLocation, RiteBuilderJS> builders = EnchantedRiteRegistry.getRegisteredBuilders();
        if (builders.isEmpty()) {
            LOGGER.info("RiteRecipeLoader: No custom rites registered");
            return Collections.emptyList();
        }

        List<RiteRecipe> recipes = new ArrayList<>();
        for (Map.Entry<ResourceLocation, RiteBuilderJS> entry : builders.entrySet()) {
            ResourceLocation id = entry.getKey();
            RiteBuilderJS builder = entry.getValue();

            try {
                RiteRecipe recipe = new RiteRecipe(
                        id,
                        builder.getPower(),
                        builder.getPowerTick(),
                        builder.getCircles(),
                        builder.getItems(),
                        builder.getEntities()
                );
                recipes.add(recipe);
                LOGGER.debug("RiteRecipeLoader: Loaded rite {} for JEI", id);
            } catch (Exception e) {
                LOGGER.error("RiteRecipeLoader: Failed to create JEI recipe for rite {}", id, e);
            }
        }

        LOGGER.info("RiteRecipeLoader: Loaded {} custom rite recipes for JEI", recipes.size());
        return Collections.unmodifiableList(recipes);
    }
}
