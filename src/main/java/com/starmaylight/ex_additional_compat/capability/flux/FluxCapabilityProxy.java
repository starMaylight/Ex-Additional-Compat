package com.starmaylight.ex_additional_compat.capability.flux;

import com.lowdragmc.multiblocked.api.capability.IO;
import com.lowdragmc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.lowdragmc.multiblocked.api.recipe.Recipe;
import com.Da_Technomancer.crossroads.API.technomancy.IFluxLink;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxy for handling Flux (Temporal Entropy) I/O between Multiblocked and Crossroads.
 *
 * IN: Extracts flux from connected IFluxLink blocks
 * OUT: Adds flux to connected IFluxLink blocks
 */
public class FluxCapabilityProxy extends CapabilityProxy<Integer> {

    private int lastFlux = -1;

    public FluxCapabilityProxy(BlockEntity blockEntity) {
        super(FluxMultiblockCapability.CAP, blockEntity);
    }

    @Override
    protected List<Integer> handleRecipeInner(@Nonnull IO io, @Nonnull Recipe recipe,
                                               @Nonnull List<Integer> left, String slotName,
                                               boolean simulate) {
        BlockEntity be = getTileEntity();
        if (be == null) return left;
        if (!(be instanceof IFluxLink fluxLink)) return left;

        List<Integer> remaining = new ArrayList<>();

        for (Integer required : left) {
            if (required == null || required <= 0) continue;

            if (io == IO.IN) {
                // Extract flux from the block
                // IFluxLink.addFlux() supports negative values for extraction
                int available = fluxLink.getFlux();
                int extractable = Math.min(required, available);
                if (!simulate && extractable > 0) {
                    fluxLink.addFlux(-extractable);
                }
                int leftover = required - extractable;
                if (leftover > 0) {
                    remaining.add(leftover);
                }
            } else if (io == IO.OUT) {
                // Add flux to the block
                int maxFlux = fluxLink.getMaxFlux();
                int currentFlux = fluxLink.getFlux();
                int space = maxFlux - currentFlux;
                int fillable = Math.min(required, space);
                if (!simulate && fillable > 0) {
                    fluxLink.addFlux(fillable);
                }
                int leftover = required - fillable;
                if (leftover > 0) {
                    remaining.add(leftover);
                }
            }
        }

        return remaining.isEmpty() ? null : remaining;
    }

    @Override
    protected boolean hasInnerChanged() {
        BlockEntity be = getTileEntity();
        if (be == null) return false;
        if (!(be instanceof IFluxLink fluxLink)) return false;

        int currentFlux = fluxLink.getFlux();
        if (currentFlux != lastFlux) {
            lastFlux = currentFlux;
            return true;
        }
        return false;
    }
}
