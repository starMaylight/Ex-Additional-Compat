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
 * right-click interactions with mod-specific tools and items.
 *
 * Without this mixin, right-clicking a Multiblocked block with these items
 * opens the Multiblocked GUI instead of processing the tool interaction,
 * because ComponentTileEntity.use() returns SUCCESS before the tool can process.
 *
 * Handled interactions:
 * - Essentials LinkingTool: Delegates to FluxInteractionHelper for ILinkTE linking
 * - Crossroads BeamCage: Returns PASS to allow item processing
 * - DraconicAdditions ChaosContainer: Delegates to ChaosInteractionHelper for chaos transfer
 */
@Mixin(value = ComponentTileEntity.class, remap = false)
public abstract class MixinComponentUse {

    /**
     * Known tool class names that should simply bypass Multiblocked's UI (PASS).
     */
    private static final String[] PASSTHROUGH_TOOL_CLASSES = {
        "com.Da_Technomancer.crossroads.items.technomancy.BeamCage",
    };

    // Tool class names that need special delegation handling
    private static final String LINKING_TOOL_CLASS = "com.Da_Technomancer.essentials.items.LinkingTool";
    private static final String CHAOS_CONTAINER_CLASS = "net.foxmcloud.draconicadditions.items.tools.ChaosContainer";

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void excompat$allowCrossroadsTools(
            Player player, InteractionHand hand, BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir) {

        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) return;

        String heldClassName = held.getItem().getClass().getName();

        // 1. Simple passthrough tools (let item handle the interaction)
        for (String toolClass : PASSTHROUGH_TOOL_CLASSES) {
            if (heldClassName.equals(toolClass)) {
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }
        }

        // 2. Linking Tool → delegate to FluxInteractionHelper for ILinkTE linking
        if (heldClassName.equals(LINKING_TOOL_CLASS)) {
            try {
                ComponentTileEntity<?> self = (ComponentTileEntity<?>) (Object) this;
                InteractionResult result = com.starmaylight.ex_additional_compat
                        .capability.flux.FluxInteractionHelper
                        .handleLinkingToolUse(self, player, hand, hit);
                if (result != null) {
                    cir.setReturnValue(result);
                    return;
                }
            } catch (NoClassDefFoundError ignored) {
                // Essentials/Crossroads not present - fallback to PASS
            }
            // If no flux trait found, still bypass GUI
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        // 3. Chaos Container → delegate to ChaosInteractionHelper for chaos transfer
        if (heldClassName.equals(CHAOS_CONTAINER_CLASS)) {
            try {
                ComponentTileEntity<?> self = (ComponentTileEntity<?>) (Object) this;
                InteractionResult result = com.starmaylight.ex_additional_compat
                        .capability.chaos.ChaosInteractionHelper
                        .handleChaosContainerUse(self, player, hand);
                if (result != null) {
                    cir.setReturnValue(result);
                    return;
                }
            } catch (NoClassDefFoundError ignored) {
                // DraconicAdditions not present - fallback to PASS
            }
            // If no chaos trait found, let normal GUI open
            return;
        }
    }
}
