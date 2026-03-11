package com.starmaylight.ex_additional_compat.capability.chaos;

import com.lowdragmc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge event handler that intercepts ChaosContainer shift+right-click on MBD machines.
 *
 * Problem: ChaosContainer (via IModularEnergyItem from Draconic Evolution) has
 * doesSneakBypassUse() returning true. This means shift+right-click on a block
 * SKIPS Block.use() entirely and calls Item.useOn() instead.
 * Since our MixinComponentUse hooks into Block.use(), it never fires.
 *
 * Solution: PlayerInteractEvent.RightClickBlock fires BEFORE the doesSneakBypassUse
 * check, so we can intercept here and handle the chaos transfer directly.
 */
@Mod.EventBusSubscriber(modid = "ex_additional_compat", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ChaosInteractionEventHandler {

    private static final String CHAOS_CONTAINER_CLASS = "net.foxmcloud.draconicadditions.items.tools.ChaosContainer";

    private ChaosInteractionEventHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        ItemStack stack = player.getItemInHand(event.getHand());
        if (stack.isEmpty()) return;

        // Only handle ChaosContainer items
        if (!stack.getItem().getClass().getName().equals(CHAOS_CONTAINER_CLASS)) return;

        // Only handle shift-click (non-shift is handled by MixinComponentUse via Block.use())
        if (!player.isShiftKeyDown()) return;

        // Only handle clicks on MBD machines
        if (event.getWorld().isClientSide()) return;
        BlockEntity be = event.getWorld().getBlockEntity(event.getPos());
        if (!(be instanceof ComponentTileEntity<?>)) return;

        try {
            InteractionResult result = ChaosInteractionHelper.handleChaosContainerUse(
                    (ComponentTileEntity<?>) be, player, event.getHand());
            if (result != null) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }
        } catch (NoClassDefFoundError ignored) {
            // DraconicAdditions not present
        }
    }
}
