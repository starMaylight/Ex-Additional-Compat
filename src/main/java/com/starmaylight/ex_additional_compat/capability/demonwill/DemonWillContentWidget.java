package com.starmaylight.ex_additional_compat.capability.demonwill;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SelectorWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.lowdragmc.multiblocked.api.gui.recipe.ContentWidget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import java.util.Arrays;
import java.util.List;

/**
 * Content widget for displaying Demon Will values in Multiblocked recipe editor and JEI.
 * Shows will amount with abbreviation overlay and will type selector.
 *
 * Will type is stored via Content.uiName (serialized by custom toJsonContent/fromJsonContent).
 */
public class DemonWillContentWidget extends ContentWidget<Double> {

    private static final int COLOR = 0xFF8B0000;

    /** Will type abbreviations corresponding to DemonWillMultiblockCapability.WILL_TYPES */
    private static final String[] TYPE_ABBR = {"DW", "Co", "De", "Vn", "St"};
    /** Will type colors for visual distinction */
    private static final int[] TYPE_COLORS = {
            0xFF8B0000, // DEFAULT  - dark red
            0xFF00AA00, // CORROSIVE - green
            0xFFFF6600, // DESTRUCTIVE - orange
            0xFF6A0DAD, // VENGEFUL - purple
            0xFF4488CC, // STEADFAST - blue
    };

    @Override
    protected void onContentUpdate() {
        if (content != null) {
            String typeName = getWillTypeName();
            setHoverTooltips(String.format("%.1f %s Will", content, typeName));
        }
    }

    @Override
    protected void drawHookBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Position pos = getPosition();
        Size size = getSize();
        int typeIdx = getWillTypeIndex();

        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 1);
        RenderSystem.disableDepthTest();
        Font font = Minecraft.getInstance().font;

        // Top-left: will type abbreviation in type color
        String abbr = typeIdx >= 0 && typeIdx < TYPE_ABBR.length ? TYPE_ABBR[typeIdx] : "DW";
        int abbrColor = typeIdx >= 0 && typeIdx < TYPE_COLORS.length ? TYPE_COLORS[typeIdx] : COLOR;
        font.drawShadow(poseStack, abbr, (pos.x + 1) * 2f, (pos.y + 1) * 2f, abbrColor);

        // Bottom-right: numeric value in white
        if (content != null) {
            String val = TextFormattingUtil.formatLongToCompactString(content.longValue(), 4);
            float textX = (pos.x + size.width) * 2f - font.width(val) - 2;
            float textY = (pos.y + size.height) * 2f - font.lineHeight - 1;
            font.drawShadow(poseStack, val, textX, textY, 0xFFFFFF);
        }

        poseStack.popPose();
    }

    @Override
    public void openConfigurator(WidgetGroup group) {
        super.openConfigurator(group);

        // Will amount input
        group.addWidget(new LabelWidget(5, 50, "Will Amount"));
        group.addWidget(new TextFieldWidget(65, 47, 60, 15,
                () -> content == null ? "0" : String.valueOf(content.doubleValue()),
                s -> {
                    try { content = Double.parseDouble(s); } catch (NumberFormatException ignored) {}
                    onContentUpdate();
                }).setNumbersOnly(0f, Float.MAX_VALUE));

        // Will type selector
        group.addWidget(new LabelWidget(5, 70, "Will Type"));
        List<String> types = Arrays.asList(DemonWillMultiblockCapability.WILL_TYPES);
        group.addWidget(new SelectorWidget(65, 67, 60, 15, types, -1)
                .setValue(getWillTypeName())
                .setOnChanged(selected -> {
                    uiName = selected;
                    onContentUpdate();
                })
                .setMaxCount(5)
                .setIsUp(false)
                .setButtonBackground(new ColorRectTexture(0xFF333333), new ColorBorderTexture(1, 0xFF555555))
                .setBackground(new ColorRectTexture(0xFF222222)));
    }

    @Override
    public Object getJEIIngredient(Double content) {
        return null;
    }

    // --- Helper methods ---

    private String getWillTypeName() {
        if (uiName != null && !uiName.isEmpty()) {
            // Validate it's a known type
            for (String type : DemonWillMultiblockCapability.WILL_TYPES) {
                if (type.equals(uiName)) return type;
            }
        }
        return "DEFAULT";
    }

    private int getWillTypeIndex() {
        String name = getWillTypeName();
        for (int i = 0; i < DemonWillMultiblockCapability.WILL_TYPES.length; i++) {
            if (DemonWillMultiblockCapability.WILL_TYPES[i].equals(name)) return i;
        }
        return 0;
    }
}
