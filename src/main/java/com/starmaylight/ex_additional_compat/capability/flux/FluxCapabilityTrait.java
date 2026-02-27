package com.starmaylight.ex_additional_compat.capability.flux;

import com.lowdragmc.multiblocked.api.capability.trait.SingleCapabilityTrait;
import net.minecraft.nbt.CompoundTag;

/**
 * Capability trait for Crossroads Flux in Multiblocked machines.
 * Stores internal flux buffer for recipe processing.
 */
public class FluxCapabilityTrait extends SingleCapabilityTrait {

    private int flux = 0;
    private int maxFlux = 64;

    public FluxCapabilityTrait() {
        super(FluxMultiblockCapability.CAP);
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        flux = compound.getInt("flux_amount");
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.writeToNBT(compound);
        compound.putInt("flux_amount", flux);
    }

    public int getFlux() { return flux; }
    public int getMaxFlux() { return maxFlux; }
}
