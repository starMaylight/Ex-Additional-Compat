package com.starmaylight.ex_additional_compat.capability.lp;

import com.lowdragmc.multiblocked.api.capability.trait.SingleCapabilityTrait;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capability trait for Blood Magic LP in Multiblocked machines.
 * Contains a BloodOrb item slot that links the machine to a player's Soul Network.
 *
 * Maintains a static registry mapping BlockEntity positions to trait instances,
 * so that LPCapabilityProxy can access the orb slot without a direct reference.
 */
public class LPCapabilityTrait extends SingleCapabilityTrait {

    /**
     * Static registry: maps controller BlockPos to its LPCapabilityTrait.
     * Used by LPCapabilityProxy to access the BloodOrb.
     */
    private static final Map<BlockPos, LPCapabilityTrait> TRAIT_REGISTRY = new ConcurrentHashMap<>();

    private final ItemStackHandler orbSlot = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem().getClass().getSimpleName().contains("BloodOrb");
        }
    };

    public LPCapabilityTrait() {
        super(LPMultiblockCapability.CAP);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (component != null && component.getBlockPos() != null) {
            TRAIT_REGISTRY.put(component.getBlockPos(), this);
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        unregister();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        unregister();
    }

    private void unregister() {
        if (component != null && component.getBlockPos() != null) {
            TRAIT_REGISTRY.remove(component.getBlockPos());
        }
    }

    /**
     * Look up the LPCapabilityTrait associated with a BlockEntity.
     * The proxy's getTileEntity() returns the ADJACENT block, not the controller.
     * We search for any registered trait whose controller is adjacent to the given BE.
     */
    public static LPCapabilityTrait getTraitFor(BlockEntity be) {
        if (be == null) return null;
        BlockPos pos = be.getBlockPos();

        // Direct match (if the BE itself is a controller)
        LPCapabilityTrait direct = TRAIT_REGISTRY.get(pos);
        if (direct != null) return direct;

        // Search for a controller adjacent to this block (manhattan distance <= 1)
        for (Map.Entry<BlockPos, LPCapabilityTrait> entry : TRAIT_REGISTRY.entrySet()) {
            BlockPos controllerPos = entry.getKey();
            int dist = Math.abs(controllerPos.getX() - pos.getX())
                     + Math.abs(controllerPos.getY() - pos.getY())
                     + Math.abs(controllerPos.getZ() - pos.getZ());
            if (dist <= 1) {
                return entry.getValue();
            }
        }

        return null;
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        if (compound.contains("orb_slot")) {
            orbSlot.deserializeNBT(compound.getCompound("orb_slot"));
        }
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.writeToNBT(compound);
        compound.put("orb_slot", orbSlot.serializeNBT());
    }

    public ItemStackHandler getOrbSlot() { return orbSlot; }
    public ItemStack getOrb() { return orbSlot.getStackInSlot(0); }
}
