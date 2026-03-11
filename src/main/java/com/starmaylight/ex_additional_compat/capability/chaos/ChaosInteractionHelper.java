package com.starmaylight.ex_additional_compat.capability.chaos;

import com.lowdragmc.multiblocked.api.tile.ComponentTileEntity;
import net.foxmcloud.draconicadditions.items.IChaosContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Helper class for handling Chaos Container item interactions with MBD machines.
 *
 * DraconicAdditions' ChaosContainer.useOn() only works on TileChaosHolderBase.
 * Since MBD machines are ComponentTileEntity (not TileChaosHolderBase),
 * we intercept the interaction in MixinComponentUse and handle it here.
 *
 * Shift + right-click: Transfer chaos FROM item TO machine
 * Right-click: Transfer chaos FROM machine TO item
 */
public final class ChaosInteractionHelper {

    private ChaosInteractionHelper() {}

    /**
     * Handle a ChaosContainer item interaction with an MBD machine component.
     *
     * @return InteractionResult if handled, null if the component has no chaos trait
     */
    public static InteractionResult handleChaosContainerUse(
            ComponentTileEntity<?> component, Player player, InteractionHand hand) {

        if (component.getLevel() == null || component.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS; // Client side: just swing hand
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !(stack.getItem() instanceof IChaosContainer chaosContainer)) {
            return null;
        }

        // Find the ChaosCapabilityTrait on this component
        ChaosCapabilityTrait trait = ChaosCapabilityTrait.getTraitFor(component);
        if (trait == null) return null;

        if (player.isShiftKeyDown()) {
            // Shift + right-click: dump chaos FROM item TO machine
            int itemChaos = chaosContainer.getChaos(stack);
            int space = trait.getMaxChaos() - trait.getChaos();
            int transfer = Math.min(itemChaos, space);
            if (transfer > 0) {
                chaosContainer.removeChaos(stack, transfer);
                trait.addChaos(transfer);
            }
        } else {
            // Right-click: extract chaos FROM machine TO item
            int machineChaos = trait.getChaos();
            int itemSpace = chaosContainer.getMaxChaos(stack) - chaosContainer.getChaos(stack);
            int transfer = Math.min(machineChaos, itemSpace);
            if (transfer > 0) {
                trait.subtractChaos(transfer);
                chaosContainer.addChaos(stack, transfer);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
