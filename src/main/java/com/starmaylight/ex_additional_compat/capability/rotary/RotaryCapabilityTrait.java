package com.starmaylight.ex_additional_compat.capability.rotary;

import com.lowdragmc.multiblocked.api.capability.trait.SingleCapabilityTrait;

/**
 * Simplified capability trait for Crossroads Rotary energy in Multiblocked machines.
 * Does not participate in the propagation system.
 */
public class RotaryCapabilityTrait extends SingleCapabilityTrait {

    public RotaryCapabilityTrait() {
        super(RotaryMultiblockCapability.CAP);
    }
}
