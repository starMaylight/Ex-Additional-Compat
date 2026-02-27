package com.starmaylight.ex_additional_compat.capability.flux;

import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.lowdragmc.multiblocked.api.gui.recipe.ContentWidget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class FluxContentWidget extends ContentWidget<Integer> {

    private static final int COLOR = 0xFF4A4A4A;

    @Override
    protected void onContentUpdate() {
        if (content != null) {
            setHoverTooltips(String.format("%d Flux", content));
        }
    }

    @Override
    protected void drawHookBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Position pos = getPosition();
        Size size = getSize();
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 1);
        RenderSystem.disableDepthTest();
        Font font = Minecraft.getInstance().font;
        font.drawShadow(poseStack, "Fx", (pos.x + 1) * 2f, (pos.y + 1) * 2f, COLOR);
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
        group.addWidget(new LabelWidget(5, 50, "Flux Amount"));
        group.addWidget(new TextFieldWidget(65, 47, 60, 15,
                () -> content == null ? "0" : String.valueOf(content.intValue()),
                s -> {
                    try { content = Integer.parseInt(s); } catch (NumberFormatException ignored) {}
                    onContentUpdate();
                }).setNumbersOnly(0, Integer.MAX_VALUE));
    }

    @Override
    public Object getJEIIngredient(Integer content) {
        return null;
    }
}
