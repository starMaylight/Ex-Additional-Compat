package com.starmaylight.ex_additional_compat.jei.embryo;

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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;

/**
 * JEI category displaying Crossroads Embryo Lab Morph recipes.
 * Shows: [Input Entity] + [Catalyst Item] → [Output Entity]
 *
 * Layout (170x60):
 *   [InputMob SpawnEgg]  +  [Catalyst]  →  [OutputMob SpawnEgg]
 *      "Entity Name"                          "Entity Name"
 */
public class EmbryoMorphCategory implements IRecipeCategory<EmbryoMorphRecipe> {

    public static final RecipeType<EmbryoMorphRecipe> RECIPE_TYPE =
            RecipeType.create("ex_additional_compat", "embryo_lab_morph", EmbryoMorphRecipe.class);

    private static final ResourceLocation UID =
            new ResourceLocation("ex_additional_compat", "embryo_lab_morph");

    private static final int WIDTH = 170;
    private static final int HEIGHT = 60;
    private static final int TEXT_COLOR = 0x404040;
    private static final int ARROW_COLOR = 0x808080;

    private final IDrawable background;
    private final IDrawable icon;

    public EmbryoMorphCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);

        // Try to use Crossroads embryo_lab block as icon; fallback to brewing stand
        ItemStack iconStack = getEmbryoLabIcon();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconStack);
    }

    private static ItemStack getEmbryoLabIcon() {
        try {
            var item = ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("crossroads", "embryo_lab"));
            if (item != null && item != Items.AIR) {
                return new ItemStack(item);
            }
        } catch (Exception ignored) {}
        return new ItemStack(Items.BREWING_STAND);
    }

    @Override
    public RecipeType<EmbryoMorphRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends EmbryoMorphRecipe> getRecipeClass() {
        return EmbryoMorphRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("jei.ex_additional_compat.embryo_lab_morph");
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
    public void setRecipe(IRecipeLayoutBuilder builder, EmbryoMorphRecipe recipe,
                          IFocusGroup focuses) {
        // Input mob (left, spawn egg)
        ItemStack inputEgg = getSpawnEgg(recipe.getInputMob());
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 12)
                .addItemStack(inputEgg);

        // Catalyst ingredient (center)
        builder.addSlot(RecipeIngredientRole.CATALYST, 62, 12)
                .addItemStacks(Arrays.asList(recipe.getCatalyst().getItems()));

        // Output mob (right, spawn egg)
        ItemStack outputEgg = getSpawnEgg(recipe.getOutputMob());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 144, 12)
                .addItemStack(outputEgg);
    }

    @Override
    public void draw(EmbryoMorphRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     PoseStack poseStack, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        // Title line
        Component title = new TextComponent("Embryo Lab Morph");
        int titleWidth = mc.font.width(title);
        mc.font.draw(poseStack, title, (WIDTH - titleWidth) / 2.0f, 1, TEXT_COLOR);

        // Entity names below slots
        String inputName = getEntityName(recipe.getInputMob());
        Component inputComp = new TextComponent(inputName);
        int inputW = mc.font.width(inputComp);
        mc.font.draw(poseStack, inputComp,
                8 + (18 - inputW) / 2.0f, 32, TEXT_COLOR);

        String outputName = getEntityName(recipe.getOutputMob());
        Component outputComp = new TextComponent(outputName);
        int outputW = mc.font.width(outputComp);
        mc.font.draw(poseStack, outputComp,
                144 + (18 - outputW) / 2.0f, 32, TEXT_COLOR);

        // "+" between input and catalyst
        Component plus = new TextComponent("+");
        int plusWidth = mc.font.width(plus);
        mc.font.draw(poseStack, plus, 38 + (24 - plusWidth) / 2.0f, 16, ARROW_COLOR);

        // "→" between catalyst and output
        Component arrow = new TextComponent("\u2192");
        int arrowWidth = mc.font.width(arrow);
        mc.font.draw(poseStack, arrow, 100 + (24 - arrowWidth) / 2.0f, 16, ARROW_COLOR);
    }

    /**
     * Get a spawn egg for an entity by its ResourceLocation.
     */
    private static ItemStack getSpawnEgg(ResourceLocation entityId) {
        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entityId);
        if (entityType != null) {
            SpawnEggItem egg = SpawnEggItem.byId(entityType);
            if (egg != null) {
                return new ItemStack(egg);
            }
        }
        return new ItemStack(Items.BARRIER);
    }

    /**
     * Get a short display name for an entity from its ResourceLocation.
     */
    private static String getEntityName(ResourceLocation entityId) {
        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entityId);
        if (entityType != null) {
            return entityType.getDescription().getString();
        }
        // Fallback: format the path nicely
        String path = entityId.getPath();
        return path.substring(0, 1).toUpperCase() + path.substring(1).replace('_', ' ');
    }
}
