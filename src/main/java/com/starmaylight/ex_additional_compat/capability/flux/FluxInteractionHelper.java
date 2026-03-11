package com.starmaylight.ex_additional_compat.capability.flux;

import com.Da_Technomancer.essentials.tileentities.LinkHelper;
import com.lowdragmc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Helper class for handling Essentials Linking Tool interactions with MBD machines.
 *
 * The Linking Tool does NOT override use()/useOn() itself - linking logic is
 * handled by the target block's use() handler calling LinkHelper.wrench().
 * Since MBD machines open their GUI on right-click, we intercept in
 * MixinComponentUse and delegate here to invoke the linking logic.
 *
 * Flow:
 * 1. Player right-clicks MBD machine with Linking Tool
 * 2. MixinComponentUse detects LinkingTool → calls this helper
 * 3. We find FluxCapabilityTrait (which implements ILinkTE)
 * 4. Call LinkHelper.wrench(trait, stack, player) to process linking
 *
 * Note: Linking only works when the MBD machine is clicked SECOND
 * (after clicking an Entropy Node first), because the reverse direction
 * requires ComponentTileEntity to implement ILinkTE (which it doesn't).
 */
public final class FluxInteractionHelper {

    private FluxInteractionHelper() {}

    /**
     * Handle a Linking Tool interaction with an MBD machine component.
     *
     * @return InteractionResult if handled, null if the component has no flux trait
     */
    public static InteractionResult handleLinkingToolUse(
            ComponentTileEntity<?> component, Player player,
            InteractionHand hand, BlockHitResult hit) {

        if (component.getLevel() == null || component.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !LinkHelper.isLinkTool(stack)) {
            return null;
        }

        // Find FluxCapabilityTrait on this component
        FluxCapabilityTrait trait = FluxCapabilityTrait.getTraitFor(component);
        if (trait == null) return null;

        // Delegate to LinkHelper.wrench() which handles:
        // - First click: stores this block's position on the tool's NBT
        // - Second click: links the previously stored block to this one
        ItemStack result = LinkHelper.wrench(trait, stack, player);
        player.setItemInHand(hand, result);

        return InteractionResult.SUCCESS;
    }
}
