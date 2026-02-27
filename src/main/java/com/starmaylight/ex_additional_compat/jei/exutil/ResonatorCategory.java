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
 * JEI category for Extra Utilities Reborn Resonator recipes.
 *
 * Layout (150x55):
 * [Input]  --->  [Output]
 *       ticks / FE info
 */
public class ResonatorCategory implements IRecipeCategory<ResonatorRecipe> {

    public static final RecipeType<ResonatorRecipe> RECIPE_TYPE =
            RecipeType.create("ex_additional_compat", "exutil_resonator", ResonatorRecipe.class);

    private static final ResourceLocation UID =
            new ResourceLocation("ex_additional_compat", "exutil_resonator");

    private final IDrawable background;
    private final IDrawable icon;

    public ResonatorCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 55);

        // Try to use the ExUtil Resonator block as icon; fallback to note block
        ItemStack resonatorIcon = getResonatorIcon();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, resonatorIcon);
    }

    private static ItemStack getResonatorIcon() {
        try {
            var item = ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("extrautilitiesrebirth", "resonator"));
            if (item != null && item != Items.AIR) {
                return new ItemStack(item);
            }
        } catch (Exception ignored) {}
        return new ItemStack(Items.NOTE_BLOCK);
    }

    @Override
    public RecipeType<ResonatorRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends ResonatorRecipe> getRecipeClass() {
        return ResonatorRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("jei.ex_additional_compat.exutil_resonator");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ResonatorRecipe recipe,
                          IFocusGroup focuses) {
        // Input (left)
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 10)
                .addItemStack(recipe.getInput());

        // Output (right)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 10)
                .addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(ResonatorRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     PoseStack poseStack, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        // Draw arrow
        Component arrow = new TextComponent("→");
        mc.font.draw(poseStack, arrow, 65, 14, 0x808080);

        // Draw time info
        String timeText = String.format("%.1fs (%d ticks)", recipe.getSeconds(), recipe.getTicks());
        mc.font.draw(poseStack, timeText, 35, 42, 0x808080);

        // Draw FE info
        String feText = formatFE(recipe.getTotalFE()) + " FE";
        mc.font.draw(poseStack, feText, 35, 32, 0xCC4444);
    }

    private static String formatFE(int fe) {
        if (fe >= 1000) {
            return String.format("%,.0fk", fe / 1000.0);
        }
        return String.valueOf(fe);
    }
}
