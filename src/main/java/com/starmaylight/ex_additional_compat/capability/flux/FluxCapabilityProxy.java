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
 * Two access modes:
 * 1. Direct: getTileEntity() is an IFluxLink (real Crossroads block adjacent)
 * 2. Trait buffer: FluxCapabilityTrait internal storage (auto-imported from adjacent IFluxLink)
 *
 * The proxy tries direct access first, then falls back to trait buffer.
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

        // Try direct access to real Crossroads IFluxLink block
        if (be instanceof IFluxLink fluxLink) {
            return handleWithFluxLink(io, fluxLink, left, simulate);
        }

        // Fallback: use trait internal buffer
        FluxCapabilityTrait trait = FluxCapabilityTrait.getTraitFor(be);
        if (trait != null) {
            return handleWithTrait(io, trait, left, simulate);
        }

        return left;
    }

    private List<Integer> handleWithFluxLink(IO io, IFluxLink fluxLink, List<Integer> left, boolean simulate) {
        List<Integer> remaining = new ArrayList<>();
        for (Integer required : left) {
            if (required == null || required <= 0) continue;

            if (io == IO.IN) {
                int available = fluxLink.getFlux();
                int extractable = Math.min(required, available);
                if (!simulate && extractable > 0) {
                    fluxLink.addFlux(-extractable);
                }
                int leftover = required - extractable;
                if (leftover > 0) remaining.add(leftover);
            } else if (io == IO.OUT) {
                int space = fluxLink.getMaxFlux() - fluxLink.getFlux();
                int fillable = Math.min(required, space);
                if (!simulate && fillable > 0) {
                    fluxLink.addFlux(fillable);
                }
                int leftover = required - fillable;
                if (leftover > 0) remaining.add(leftover);
            }
        }
        return remaining.isEmpty() ? null : remaining;
    }

    private List<Integer> handleWithTrait(IO io, FluxCapabilityTrait trait, List<Integer> left, boolean simulate) {
        List<Integer> remaining = new ArrayList<>();
        for (Integer required : left) {
            if (required == null || required <= 0) continue;

            if (io == IO.IN) {
                int available = trait.getFlux();
                int extractable = Math.min(required, available);
                if (!simulate && extractable > 0) {
                    trait.addFlux(-extractable);
                }
                int leftover = required - extractable;
                if (leftover > 0) remaining.add(leftover);
            } else if (io == IO.OUT) {
                int space = trait.getMaxFlux() - trait.getFlux();
                int fillable = Math.min(required, space);
                if (!simulate && fillable > 0) {
                    trait.addFlux(fillable);
                }
                int leftover = required - fillable;
                if (leftover > 0) remaining.add(leftover);
            }
        }
        return remaining.isEmpty() ? null : remaining;
    }

    @Override
    protected boolean hasInnerChanged() {
        BlockEntity be = getTileEntity();
        if (be == null) return false;

        int currentFlux = -1;

        if (be instanceof IFluxLink fluxLink) {
            currentFlux = fluxLink.getFlux();
        } else {
            FluxCapabilityTrait trait = FluxCapabilityTrait.getTraitFor(be);
            if (trait != null) {
                currentFlux = trait.getFlux();
            }
        }

        if (currentFlux != lastFlux) {
            lastFlux = currentFlux;
            return true;
        }
        return false;
    }
}
