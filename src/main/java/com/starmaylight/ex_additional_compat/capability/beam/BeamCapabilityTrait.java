package com.starmaylight.ex_additional_compat.capability.beam;

import com.lowdragmc.multiblocked.api.capability.trait.SingleCapabilityTrait;

/**
 * Capability trait for Crossroads Beam in Multiblocked machines.
 * INPUT ONLY - does not emit beams.
 */
public class BeamCapabilityTrait extends SingleCapabilityTrait {

    public BeamCapabilityTrait() {
        super(BeamMultiblockCapability.CAP);
    }
}
