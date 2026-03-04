package com.starmaylight.ex_additional_compat.jei.rite;

import com.mojang.blaze3d.vertex.PoseStack;
import com.favouriteless.enchanted.common.rites.CirclePart;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

/**
 * JEI category displaying custom Enchanted rite (ritual) requirements.
 *
 * Layout (170 x dynamic height):
 *   Title: Rite name
 *   Power: initial / per tick
 *   Circles: [Block icons] with size labels (Small/Medium/Large)
 *   Items:   [Item icons] with counts
 *   Entities: [Spawn egg icons] with counts
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
public class RiteCategory implements IRecipeCategory<RiteRecipe> {

    public static final RecipeType<RiteRecipe> RECIPE_TYPE =
            RecipeType.create("ex_additional_compat", "enchanted_rite", RiteRecipe.class);

    private static final ResourceLocation UID =
            new ResourceLocation("ex_additional_compat", "enchanted_rite");

    // Layout constants
    private static final int WIDTH = 170;
    private static final int HEIGHT = 120;
    private static final int SLOT_SIZE = 18;
    private static final int TEXT_COLOR = 0x404040;
    private static final int LABEL_COLOR = 0x808080;
    private static final int POWER_COLOR = 0x8B4513; // Saddle brown for power display

    private final IDrawable background;
    private final IDrawable icon;

    public RiteCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);

        // Use chalk (Enchanted's ritual catalyst) as icon; fallback to gold block
        ItemStack iconStack = getChalkIcon();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconStack);
    }

    private static ItemStack getChalkIcon() {
        // Try Enchanted's chalk_gold item as category icon
        try {
            var item = ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("enchanted", "chalk_gold"));
            if (item != null && item != Items.AIR) {
                return new ItemStack(item);
            }
        } catch (Exception ignored) {}
        // Fallback: try regular chalk
        try {
            var item = ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("enchanted", "chalk_white"));
            if (item != null && item != Items.AIR) {
                return new ItemStack(item);
            }
        } catch (Exception ignored) {}
        return new ItemStack(Items.GOLD_BLOCK);
    }

    @Override
    public RecipeType<RiteRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends RiteRecipe> getRecipeClass() {
        return RiteRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("jei.ex_additional_compat.enchanted_rite");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RiteRecipe recipe,
                          IFocusGroup focuses) {
        int slotX = 5;
        int slotY = 40; // Start below power text area

        // Circle requirement blocks
        for (Map.Entry<CirclePart, Block> entry : recipe.getCircles().entrySet()) {
            Block block = entry.getValue();
            ItemStack blockStack = new ItemStack(block.asItem());
            if (!blockStack.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.CATALYST, slotX, slotY)
                        .addItemStack(blockStack);
                slotX += SLOT_SIZE + 2;
            }
        }

        // Required items
        if (!recipe.getCircles().isEmpty() && !recipe.getItems().isEmpty()) {
            slotX += 6; // Gap between circles and items
        }
        for (Map.Entry<Item, Integer> entry : recipe.getItems().entrySet()) {
            ItemStack itemStack = new ItemStack(entry.getKey(), entry.getValue());
            builder.addSlot(RecipeIngredientRole.INPUT, slotX, slotY)
                    .addItemStack(itemStack);
            slotX += SLOT_SIZE + 2;
        }

        // Required entities (shown as spawn eggs on next row)
        if (!recipe.getEntities().isEmpty()) {
            slotX = 5;
            slotY += SLOT_SIZE + 14; // Next row with label space
            for (Map.Entry<EntityType<?>, Integer> entry : recipe.getEntities().entrySet()) {
                SpawnEggItem egg = SpawnEggItem.byId(entry.getKey());
                ItemStack eggStack = egg != null
                        ? new ItemStack(egg, entry.getValue())
                        : new ItemStack(Items.BARRIER, entry.getValue());
                builder.addSlot(RecipeIngredientRole.INPUT, slotX, slotY)
                        .addItemStack(eggStack);
                slotX += SLOT_SIZE + 2;
            }
        }
    }

    @Override
    public void draw(RiteRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     PoseStack poseStack, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        // Line 1: Rite name (formatted from ResourceLocation path)
        String riteName = formatRiteName(recipe.getId());
        Component nameComponent = new TextComponent(riteName);
        int nameWidth = mc.font.width(nameComponent);
        mc.font.draw(poseStack, nameComponent, (WIDTH - nameWidth) / 2.0f, 2, TEXT_COLOR);

        // Line 2: Power requirements
        String powerText;
        if (recipe.getPowerTick() > 0) {
            powerText = String.format("Altar Power: %d + %d/tick", recipe.getPower(), recipe.getPowerTick());
        } else if (recipe.getPower() > 0) {
            powerText = String.format("Altar Power: %d", recipe.getPower());
        } else {
            powerText = "Altar Power: None";
        }
        Component powerComponent = new TextComponent(powerText);
        int powerWidth = mc.font.width(powerComponent);
        mc.font.draw(poseStack, powerComponent, (WIDTH - powerWidth) / 2.0f, 14, POWER_COLOR);

        // Line 3: Section labels above slots
        int labelY = 30;
        int labelX = 5;

        // Circle labels
        if (!recipe.getCircles().isEmpty()) {
            Component circleLabel = new TextComponent("Circles:");
            mc.font.draw(poseStack, circleLabel, labelX, labelY, LABEL_COLOR);
        }

        // Draw circle size labels under each circle slot
        int circleSlotX = 5;
        for (Map.Entry<CirclePart, Block> entry : recipe.getCircles().entrySet()) {
            CirclePart part = entry.getKey();
            String sizeLabel = getCircleLabel(part);
            Component label = new TextComponent(sizeLabel);
            int textW = mc.font.width(label);
            mc.font.draw(poseStack, label,
                    circleSlotX + (SLOT_SIZE - textW) / 2.0f, 40 + SLOT_SIZE + 1,
                    LABEL_COLOR);
            circleSlotX += SLOT_SIZE + 2;
        }

        // Items label
        if (!recipe.getItems().isEmpty()) {
            int itemsLabelX = circleSlotX;
            if (!recipe.getCircles().isEmpty()) {
                itemsLabelX += 6;
            }
            Component itemsLabel = new TextComponent("Items:");
            mc.font.draw(poseStack, itemsLabel, itemsLabelX, labelY, LABEL_COLOR);
        }

        // Entities label (on next row)
        if (!recipe.getEntities().isEmpty()) {
            int entityLabelY = 40 + SLOT_SIZE + 14 - 10;
            Component entityLabel = new TextComponent("Entities:");
            mc.font.draw(poseStack, entityLabel, 5, entityLabelY, LABEL_COLOR);
        }
    }

    /**
     * Format a ResourceLocation path into a human-readable rite name.
     * e.g. "my_custom_rite" -> "My Custom Rite"
     */
    private static String formatRiteName(ResourceLocation id) {
        String path = id.getPath();
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    /**
     * Get a short label for a circle part size.
     */
    private static String getCircleLabel(CirclePart part) {
        if (part == CirclePart.SMALL) return "S";
        if (part == CirclePart.MEDIUM) return "M";
        if (part == CirclePart.LARGE) return "L";
        return "?";
    }
}
