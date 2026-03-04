package com.starmaylight.ex_additional_compat.mixin.multiblocked;

import com.lowdragmc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to prevent Multiblocked's controller/component UI from intercepting
 * right-click interactions with Crossroads/Essentials' Linking Tool.
 *
 * Without this mixin, right-clicking a Multiblocked block with a Linking Tool
 * opens the Multiblocked GUI instead of initiating the linking process,
 * because ComponentTileEntity.use() returns SUCCESS before the tool can process.
 *
 * This mixin injects at HEAD and returns PASS for known linking/interaction
 * tools from Crossroads and Essentials, allowing them to function normally.
 */
@Mixin(value = ComponentTileEntity.class, remap = false)
public abstract class MixinComponentUse {

    /**
     * Known tool class names that should bypass Multiblocked's UI interaction.
     * Checked by class name string to avoid hard dependency on optional mods.
     */
    private static final String[] PASSTHROUGH_TOOL_CLASSES = {
        // Essentials: Linking Tool for copper wire flux networks
        "com.Da_Technomancer.essentials.items.LinkingTool",
        // Crossroads: various interaction tools that need block access
        "com.Da_Technomancer.crossroads.items.technomancy.BeamCage",
    };

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void excompat$allowCrossroadsTools(
            Player player, InteractionHand hand, BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir) {

        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) return;

        String heldClassName = held.getItem().getClass().getName();
        for (String toolClass : PASSTHROUGH_TOOL_CLASSES) {
            if (heldClassName.equals(toolClass)) {
                // Return PASS so the tool's own use() logic can process the interaction
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }
        }
    }
}
