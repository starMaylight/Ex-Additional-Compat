package com.starmaylight.ex_additional_compat.capability.beam;

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

public class BeamContentWidget extends ContentWidget<int[]> {

    private static final int COLOR = 0xFFFFFF00;

    @Override
    protected void onContentUpdate() {
        if (content != null && content.length >= 4) {
            int power = content[0] + content[1] + content[2] + content[3];
            setHoverTooltips(String.format("Beam [E:%d P:%d S:%d V:%d] = %d",
                    content[0], content[1], content[2], content[3], power));
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
        font.drawShadow(poseStack, "Bm", (pos.x + 1) * 2f, (pos.y + 1) * 2f, COLOR);
        if (content != null && content.length >= 4) {
            long total = content[0] + content[1] + content[2] + content[3];
            String val = TextFormattingUtil.formatLongToCompactString(total, 4);
            float textX = (pos.x + size.width) * 2f - font.width(val) - 2;
            float textY = (pos.y + size.height) * 2f - font.lineHeight - 1;
            font.drawShadow(poseStack, val, textX, textY, 0xFFFFFF);
        }
        poseStack.popPose();
    }

    @Override
    public void openConfigurator(WidgetGroup group) {
        super.openConfigurator(group);
        if (content == null) content = new int[]{0, 0, 0, 0};
        String[] labels = {"Energy", "Potential", "Stability", "Void"};
        for (int i = 0; i < 4; i++) {
            int idx = i;
            int yPos = 47 + i * 20;
            group.addWidget(new LabelWidget(5, yPos + 3, labels[i]));
            group.addWidget(new TextFieldWidget(65, yPos, 60, 15,
                    () -> String.valueOf(content[idx]),
                    s -> {
                        try { content[idx] = Integer.parseInt(s); } catch (NumberFormatException ignored) {}
                        onContentUpdate();
                    }).setNumbersOnly(0, Integer.MAX_VALUE));
        }
    }

    @Override
    public Object getJEIIngredient(int[] content) {
        return null;
    }
}
