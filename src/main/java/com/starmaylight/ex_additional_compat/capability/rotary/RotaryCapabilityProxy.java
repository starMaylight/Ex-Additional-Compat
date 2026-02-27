package com.starmaylight.ex_additional_compat.capability.rotary;

import com.lowdragmc.multiblocked.api.capability.IO;
import com.lowdragmc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.lowdragmc.multiblocked.api.recipe.Recipe;
import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.rotary.IAxleHandler;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified proxy for Crossroads rotary energy I/O.
 * Uses addEnergy() API without joining the gear propagation system.
 *
 * Multiblocked IO semantics:
 *   IN  = machine CONSUMES rotary energy = extract from axle = addEnergy(-amount, false)
 *   OUT = machine PRODUCES rotary energy = inject to axle    = addEnergy(+amount, false)
 *
 * The boolean parameter (absolute=false) respects rotational direction.
 */
public class RotaryCapabilityProxy extends CapabilityProxy<Double> {

    private double lastEnergy = 0;

    public RotaryCapabilityProxy(BlockEntity blockEntity) {
        super(RotaryMultiblockCapability.CAP, blockEntity);
    }

    @Override
    protected List<Double> handleRecipeInner(@Nonnull IO io, @Nonnull Recipe recipe,
                                              @Nonnull List<Double> left, String slotName,
                                              boolean simulate) {
        BlockEntity be = getTileEntity();
        if (be == null) return left;

        IAxleHandler handler = be.getCapability(Capabilities.AXLE_CAPABILITY).resolve().orElse(null);
        if (handler == null) return left;

        List<Double> remaining = new ArrayList<>();

        for (Double required : left) {
            if (required == null || required <= 0) continue;

            if (io == IO.IN) {
                // Machine consumes rotary energy: extract from the axle
                double currentEnergy = Math.abs(handler.getEnergy());
                double extractable = Math.min(required, currentEnergy);
                if (!simulate && extractable > 0) {
                    handler.addEnergy(-extractable, false);
                }
                double leftover = required - extractable;
                if (leftover > 0.01) {
                    remaining.add(leftover);
                }
            } else if (io == IO.OUT) {
                // Machine produces rotary energy: inject into the axle
                if (!simulate) {
                    handler.addEnergy(required, false);
                }
                // addEnergy always succeeds for output
            }
        }

        return remaining.isEmpty() ? null : remaining;
    }

    @Override
    protected boolean hasInnerChanged() {
        BlockEntity be = getTileEntity();
        if (be == null) return false;

        IAxleHandler handler = be.getCapability(Capabilities.AXLE_CAPABILITY).resolve().orElse(null);
        if (handler == null) return false;

        double currentEnergy = handler.getEnergy();
        if (Math.abs(currentEnergy - lastEnergy) > 0.01) {
            lastEnergy = currentEnergy;
            return true;
        }
        return false;
    }
}
