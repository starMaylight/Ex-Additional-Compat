package com.starmaylight.ex_additional_compat.capability.beam;

import com.lowdragmc.multiblocked.api.capability.IO;
import com.lowdragmc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.lowdragmc.multiblocked.api.recipe.Recipe;
import com.Da_Technomancer.crossroads.API.beams.BeamUnit;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxy for handling Beam energy I/O between Multiblocked machines and Crossroads beam system.
 *
 * IO.IN: Checks if the BeamCapabilityTrait has received enough beam from external emitters.
 *        External beams hit the multiblock component → Trait stores via IBeamHandler.setBeam()
 *        → Proxy reads stored beam and checks if it meets recipe requirements.
 *
 * Architecture:
 * - Beam is a per-tick transient stream, NOT accumulated storage
 * - Each tick, if a beam is hitting the component, setBeam() is called on the Trait
 * - The Proxy checks if the current beam meets recipe requirements
 * - If the beam is strong enough, the recipe progresses that tick
 */
public class BeamCapabilityProxy extends CapabilityProxy<int[]> {

    private BeamUnit lastCheckedBeam = BeamUnit.EMPTY;

    public BeamCapabilityProxy(BlockEntity blockEntity) {
        super(BeamMultiblockCapability.CAP, blockEntity);
    }

    @Override
    protected List<int[]> handleRecipeInner(@Nonnull IO io, @Nonnull Recipe recipe,
                                             @Nonnull List<int[]> left, String slotName,
                                             boolean simulate) {
        // Only support INPUT (receiving beam from external source)
        if (io != IO.IN) return left;

        BlockEntity be = getTileEntity();
        if (be == null) return left;

        // Find the BeamCapabilityTrait via registry
        BeamCapabilityTrait trait = BeamCapabilityTrait.getTraitFor(be);
        if (trait == null) return left;

        BeamUnit receivedBeam = trait.getLastReceivedBeam();
        if (receivedBeam == null || receivedBeam.isEmpty()) return left;

        List<int[]> remaining = new ArrayList<>();

        for (int[] beamValues : left) {
            if (beamValues == null || beamValues.length < 4) continue;

            int reqEnergy = beamValues[0];
            int reqPotential = beamValues[1];
            int reqStability = beamValues[2];
            int reqVoid = beamValues[3];

            // Check if the received beam meets all component requirements
            int totalRequired = reqEnergy + reqPotential + reqStability + reqVoid;
            int totalReceived = receivedBeam.getEnergy() + receivedBeam.getPotential()
                              + receivedBeam.getStability() + receivedBeam.getVoid();

            if (totalReceived >= totalRequired) {
                // Beam is strong enough - recipe can progress this tick
                // Don't consume the beam (it's a continuous stream)
            } else {
                // Not enough beam power
                remaining.add(beamValues);
            }
        }

        return remaining.isEmpty() ? null : remaining;
    }

    @Override
    protected boolean hasInnerChanged() {
        BlockEntity be = getTileEntity();
        if (be == null) return false;

        BeamCapabilityTrait trait = BeamCapabilityTrait.getTraitFor(be);
        if (trait == null) return false;

        BeamUnit current = trait.getLastReceivedBeam();
        if (current == null) current = BeamUnit.EMPTY;

        boolean changed = !current.equals(lastCheckedBeam);
        lastCheckedBeam = current;
        return changed;
    }
}
