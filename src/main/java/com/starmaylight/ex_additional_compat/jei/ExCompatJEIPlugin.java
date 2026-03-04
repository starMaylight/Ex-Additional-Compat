package com.starmaylight.ex_additional_compat.jei;

import com.mojang.logging.LogUtils;
import com.starmaylight.ex_additional_compat.ModLoadedHelper;
import com.starmaylight.ex_additional_compat.jei.arthana.ArthanaDropCategory;
import com.starmaylight.ex_additional_compat.jei.arthana.ArthanaDropRecipeLoader;
import com.starmaylight.ex_additional_compat.jei.exutil.EnchanterCategory;
import com.starmaylight.ex_additional_compat.jei.exutil.ResonatorCategory;
import com.starmaylight.ex_additional_compat.jei.embryo.EmbryoMorphCategory;
import com.starmaylight.ex_additional_compat.jei.embryo.EmbryoMorphRecipeLoader;
import com.starmaylight.ex_additional_compat.jei.rite.RiteCategory;
import com.starmaylight.ex_additional_compat.jei.rite.RiteRecipeLoader;
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
 * - Enchanted Rite category (Enchanted mod: custom ritual requirements via KubeJS)
 * - Embryo Lab Morph category (Crossroads MC: embryo lab entity morphing recipes)
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
            registration.addRecipeCategories(new RiteCategory(guiHelper));
            LOGGER.info("ExCompatJEI: Registered Arthana Drop and Enchanted Rite categories");
        }

        if (ModLoadedHelper.isCrossroadsLoaded()) {
            registration.addRecipeCategories(new EmbryoMorphCategory(guiHelper));
            LOGGER.info("ExCompatJEI: Registered Embryo Lab Morph category");
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

            try {
                var riteRecipes = RiteRecipeLoader.loadRecipes();
                registration.addRecipes(RiteCategory.RECIPE_TYPE, riteRecipes);
                LOGGER.info("ExCompatJEI: Registered {} custom rite recipes", riteRecipes.size());
            } catch (Exception e) {
                LOGGER.error("ExCompatJEI: Failed to load custom rite recipes", e);
            }
        }

        if (ModLoadedHelper.isCrossroadsLoaded()) {
            try {
                var embryoRecipes = EmbryoMorphRecipeLoader.loadRecipes();
                registration.addRecipes(EmbryoMorphCategory.RECIPE_TYPE, embryoRecipes);
                LOGGER.info("ExCompatJEI: Registered {} embryo lab morph recipes",
                        embryoRecipes.size());
            } catch (Exception e) {
                LOGGER.error("ExCompatJEI: Failed to load embryo lab morph recipes", e);
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

            // Register chalk_gold as catalyst for Enchanted Rite category
            ItemStack chalk = getItemStack("enchanted", "chalk_gold");
            if (chalk.isEmpty()) {
                chalk = getItemStack("enchanted", "chalk_white");
            }
            if (!chalk.isEmpty()) {
                registration.addRecipeCatalyst(chalk, RiteCategory.RECIPE_TYPE);
            }
        }

        if (ModLoadedHelper.isCrossroadsLoaded()) {
            // Register Embryo Lab block as catalyst for Embryo Morph category
            ItemStack embryoLab = getItemStack("crossroads", "embryo_lab");
            if (!embryoLab.isEmpty()) {
                registration.addRecipeCatalyst(embryoLab, EmbryoMorphCategory.RECIPE_TYPE);
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
