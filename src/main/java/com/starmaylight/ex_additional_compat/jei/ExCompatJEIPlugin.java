package com.starmaylight.ex_additional_compat.jei;

import com.mojang.logging.LogUtils;
import com.starmaylight.ex_additional_compat.ModLoadedHelper;
import com.starmaylight.ex_additional_compat.jei.arthana.ArthanaDropCategory;
import com.starmaylight.ex_additional_compat.jei.arthana.ArthanaDropRecipeLoader;
import com.starmaylight.ex_additional_compat.jei.exutil.EnchanterCategory;
import com.starmaylight.ex_additional_compat.jei.exutil.ResonatorCategory;
import com.starmaylight.ex_additional_compat.recipe.ExUtilRecipeRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

/**
 * JEI plugin for Ex Additional Compat.
 *
 * Registers:
 * - Arthana Drop category (Enchanted mod: entity drops when killed with Arthana)
 * - Enchanter category (Extra Utilities Reborn: enchanting machine recipes)
 * - Resonator category (Extra Utilities Reborn: resonating machine recipes)
 *
 * Each category is only registered if its source mod is loaded.
 */
@JeiPlugin
public class ExCompatJEIPlugin implements IModPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation PLUGIN_UID =
            new ResourceLocation("ex_additional_compat", "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();

        if (ModLoadedHelper.isEnchantedLoaded()) {
            registration.addRecipeCategories(new ArthanaDropCategory(guiHelper));
            LOGGER.info("ExCompatJEI: Registered Arthana Drop category");
        }

        if (ModLoadedHelper.isExtraUtilLoaded()) {
            registration.addRecipeCategories(new EnchanterCategory(guiHelper));
            registration.addRecipeCategories(new ResonatorCategory(guiHelper));
            LOGGER.info("ExCompatJEI: Registered Enchanter and Resonator categories");
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (ModLoadedHelper.isEnchantedLoaded()) {
            try {
                var recipes = ArthanaDropRecipeLoader.loadRecipes();
                registration.addRecipes(ArthanaDropCategory.RECIPE_TYPE, recipes);
                LOGGER.info("ExCompatJEI: Registered {} Arthana drop recipes", recipes.size());
            } catch (Exception e) {
                LOGGER.error("ExCompatJEI: Failed to load Arthana drop recipes", e);
            }
        }

        if (ModLoadedHelper.isExtraUtilLoaded()) {
            try {
                ExUtilRecipeRegistry.init();
                registration.addRecipes(EnchanterCategory.RECIPE_TYPE,
                        ExUtilRecipeRegistry.getEnchanterRecipes());
                registration.addRecipes(ResonatorCategory.RECIPE_TYPE,
                        ExUtilRecipeRegistry.getResonatorRecipes());
                LOGGER.info("ExCompatJEI: Registered {} Enchanter and {} Resonator recipes",
                        ExUtilRecipeRegistry.getEnchanterRecipes().size(),
                        ExUtilRecipeRegistry.getResonatorRecipes().size());
            } catch (Exception e) {
                LOGGER.error("ExCompatJEI: Failed to load ExUtil recipes", e);
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (ModLoadedHelper.isEnchantedLoaded()) {
            // Register Arthana as catalyst for Arthana Drop category
            ItemStack arthana = getItemStack("enchanted", "arthana");
            if (!arthana.isEmpty()) {
                registration.addRecipeCatalyst(arthana, ArthanaDropCategory.RECIPE_TYPE);
            }
        }

        if (ModLoadedHelper.isExtraUtilLoaded()) {
            // Register Enchanter block as catalyst
            ItemStack enchanter = getItemStack("extrautilitiesrebirth", "enchanter");
            if (!enchanter.isEmpty()) {
                registration.addRecipeCatalyst(enchanter, EnchanterCategory.RECIPE_TYPE);
            }

            // Register Resonator block as catalyst
            ItemStack resonator = getItemStack("extrautilitiesrebirth", "resonator");
            if (!resonator.isEmpty()) {
                registration.addRecipeCatalyst(resonator, ResonatorCategory.RECIPE_TYPE);
            }
        }
    }

    /**
     * Helper to get an ItemStack from a mod's registry name.
     * Returns ItemStack.EMPTY if the item is not found.
     */
    private static ItemStack getItemStack(String namespace, String path) {
        try {
            var rl = new ResourceLocation(namespace, path);
            var item = ForgeRegistries.ITEMS.getValue(rl);
            if (item != null && item != Items.AIR) {
                return new ItemStack(item);
            }
            // Try as block item
            var block = ForgeRegistries.BLOCKS.getValue(rl);
            if (block != null && block != net.minecraft.world.level.block.Blocks.AIR) {
                return new ItemStack(block.asItem());
            }
        } catch (Exception ignored) {}
        return ItemStack.EMPTY;
    }
}
