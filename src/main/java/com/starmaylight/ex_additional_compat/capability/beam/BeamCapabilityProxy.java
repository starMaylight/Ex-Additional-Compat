package com.starmaylight.ex_additional_compat.capability.beam;

import com.lowdragmc.multiblocked.api.capability.IO;
import com.lowdragmc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.lowdragmc.multiblocked.api.recipe.Recipe;
import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.beams.BeamUnit;
import com.Da_Technomancer.crossroads.API.beams.IBeamHandler;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Proxy for handling Beam energy INPUT from Crossroads beam system.
 * OUTPUT is not supported in this initial version.
 *
 * IN: Sends a BeamUnit to the connected IBeamHandler via setBeam()
 */
public class BeamCapabilityProxy extends CapabilityProxy<int[]> {

    public BeamCapabilityProxy(BlockEntity blockEntity) {
        super(BeamMultiblockCapability.CAP, blockEntity);
    }

    @Override
    protected List<int[]> handleRecipeInner(@Nonnull IO io, @Nonnull Recipe recipe,
                                             @Nonnull List<int[]> left, String slotName,
                                             boolean simulate) {
        // Only support INPUT (delivering beam to a receiver)
        if (io != IO.IN) return left;

        BlockEntity be = getTileEntity();
        if (be == null) return left;

        IBeamHandler handler = be.getCapability(Capabilities.BEAM_CAPABILITY).resolve().orElse(null);
        if (handler == null) return left;

        for (int[] beamValues : left) {
            if (beamValues == null || beamValues.length < 4) continue;

            if (!simulate) {
                BeamUnit beam = new BeamUnit(beamValues[0], beamValues[1], beamValues[2], beamValues[3]);
                handler.setBeam(beam);
            }
        }

        // Beam delivery always succeeds once we have a handler
        return null;
    }

    @Override
    protected boolean hasInnerChanged() {
        // Beam state is transient (set each tick), so always recheck
        return true;
    }
}
