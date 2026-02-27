package com.starmaylight.ex_additional_compat.jei.exutil;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * JEI category for Extra Utilities Reborn Enchanter recipes.
 *
 * Layout (160x65):
 * [Main Input]  + [Catalyst]  --->  [Output]
 *              ticks / FE info
 */
public class EnchanterCategory implements IRecipeCategory<EnchanterRecipe> {

    public static final RecipeType<EnchanterRecipe> RECIPE_TYPE =
            RecipeType.create("ex_additional_compat", "exutil_enchanter", EnchanterRecipe.class);

    private static final ResourceLocation UID =
            new ResourceLocation("ex_additional_compat", "exutil_enchanter");

    private final IDrawable background;
    private final IDrawable icon;

    public EnchanterCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 65);

        // Try to use the ExUtil Enchanter block as icon; fallback to enchanting table
        ItemStack enchanterIcon = getEnchanterIcon();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, enchanterIcon);
    }

    private static ItemStack getEnchanterIcon() {
        try {
            var item = ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("extrautilitiesrebirth", "enchanter"));
            if (item != null && item != Items.AIR) {
                return new ItemStack(item);
            }
        } catch (Exception ignored) {}
        return new ItemStack(Items.ENCHANTING_TABLE);
    }

    @Override
    public RecipeType<EnchanterRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends EnchanterRecipe> getRecipeClass() {
        return EnchanterRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("jei.ex_additional_compat.exutil_enchanter");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EnchanterRecipe recipe,
                          IFocusGroup focuses) {
        // Main input (top-left)
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 5)
                .addItemStack(recipe.getInputMain());

        // Catalyst input (below main input)
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 30)
                .addItemStack(recipe.getInputCatalyst());

        // Output (right side)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 130, 17)
                .addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(EnchanterRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     PoseStack poseStack, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        // Draw arrow
        Component arrow = new TextComponent("→");
        mc.font.draw(poseStack, arrow, 70, 21, 0x808080);

        // Draw time info
        String timeText = String.format("%.1fs (%d ticks)", recipe.getSeconds(), recipe.getTicks());
        mc.font.draw(poseStack, timeText, 40, 52, 0x808080);

        // Draw FE info
        String feText = formatFE(recipe.getTotalFE()) + " FE";
        mc.font.draw(poseStack, feText, 40, 42, 0xCC4444);
    }

    private static String formatFE(int fe) {
        if (fe >= 1000) {
            return String.format("%,.0fk", fe / 1000.0);
        }
        return String.valueOf(fe);
    }
}
