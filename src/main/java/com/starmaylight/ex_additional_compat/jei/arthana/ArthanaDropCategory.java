package com.starmaylight.ex_additional_compat.jei.arthana;

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
 * JEI category displaying Enchanted mod's Arthana entity drops.
 * Shows: EntityType (as spawn egg) -> ItemStack drop (when killed with Arthana).
 *
 * Layout (150x50):
 * [SpawnEgg/Entity] ---> [Arthana icon] ---> [Drop Item]
 */
/*+
 *
 *   Copyright (c) 2023. Favouriteless
 *   Enchanted, a minecraft mod.
 *   GNU GPLv3 License
 *
 *       This file is part of Enchanted.
 *
 *       Enchanted is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       Enchanted is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU General Public License for more details.
 *
 *       You should have received a copy of the GNU General Public License
 *       along with Enchanted.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 **/
public class ArthanaDropCategory implements IRecipeCategory<ArthanaDropRecipe> {

    public static final RecipeType<ArthanaDropRecipe> RECIPE_TYPE =
            RecipeType.create("ex_additional_compat", "arthana_drop", ArthanaDropRecipe.class);

    private static final ResourceLocation UID =
            new ResourceLocation("ex_additional_compat", "arthana_drop");

    private final IDrawable background;
    private final IDrawable icon;

    public ArthanaDropCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 50);

        // Try to use the Enchanted Arthana item as icon; fallback to golden sword
        ItemStack arthanaIcon = getArthanaIcon();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, arthanaIcon);
    }

    private static ItemStack getArthanaIcon() {
        try {
            var item = ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("enchanted", "arthana"));
            if (item != null && item != Items.AIR) {
                return new ItemStack(item);
            }
        } catch (Exception ignored) {}
        return new ItemStack(Items.GOLDEN_SWORD);
    }

    @Override
    public RecipeType<ArthanaDropRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends ArthanaDropRecipe> getRecipeClass() {
        return ArthanaDropRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("jei.ex_additional_compat.arthana_drop");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ArthanaDropRecipe recipe,
                          IFocusGroup focuses) {
        // Input: Spawn egg for the entity (left side)
        ItemStack spawnEgg = getSpawnEgg(recipe.getEntityType());
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 17)
                .addItemStack(spawnEgg);

        // Output: Drop item (right side)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 124, 17)
                .addItemStack(recipe.getResult());
    }

    @Override
    public void draw(ArthanaDropRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     PoseStack poseStack, double mouseX, double mouseY) {
        // Draw entity name in the center
        Minecraft mc = Minecraft.getInstance();
        Component entityName = recipe.getEntityType().getDescription();
        int textWidth = mc.font.width(entityName);
        mc.font.draw(poseStack, entityName, (150 - textWidth) / 2.0f, 3, 0x404040);

        // Draw arrow in the middle
        Component arrow = new TextComponent("→");
        int arrowWidth = mc.font.width(arrow);
        mc.font.draw(poseStack, arrow, (150 - arrowWidth) / 2.0f, 21, 0x808080);
    }

    /**
     * Get the spawn egg for the given entity type.
     * Returns a barrier item if no spawn egg exists.
     */
    private static ItemStack getSpawnEgg(net.minecraft.world.entity.EntityType<?> entityType) {
        var spawnEgg = net.minecraft.world.item.SpawnEggItem.byId(entityType);
        if (spawnEgg != null) {
            return new ItemStack(spawnEgg);
        }
        // No spawn egg available; use barrier as placeholder
        return new ItemStack(Items.BARRIER);
    }
}
