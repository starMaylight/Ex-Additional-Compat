package com.starmaylight.ex_additional_compat.capability.chaos;

import com.lowdragmc.multiblocked.api.capability.trait.SingleCapabilityTrait;
import net.minecraft.nbt.CompoundTag;

/**
 * Capability trait for Draconic Additions Chaos in Multiblocked machines.
 * Provides internal chaos buffer for recipe processing.
 */
public class ChaosCapabilityTrait extends SingleCapabilityTrait {

    private int chaos = 0;
    private int maxChaos = 10000;

    public ChaosCapabilityTrait() {
        super(ChaosMultiblockCapability.CAP);
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        chaos = compound.getInt("chaos_amount");
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.writeToNBT(compound);
        compound.putInt("chaos_amount", chaos);
    }

    public int getChaos() { return chaos; }
    public int getMaxChaos() { return maxChaos; }
}
