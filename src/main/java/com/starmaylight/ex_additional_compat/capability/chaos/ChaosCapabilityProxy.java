package com.starmaylight.ex_additional_compat.capability.chaos;

import com.lowdragmc.multiblocked.api.capability.IO;
import com.lowdragmc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.lowdragmc.multiblocked.api.recipe.Recipe;
import net.foxmcloud.draconicadditions.blocks.tileentity.TileChaosHolderBase;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxy for handling Chaos energy I/O between Multiblocked machines and DraconicAdditions blocks.
 *
 * Two access modes:
 * 1. Direct: getTileEntity() is a TileChaosHolderBase (real DA block adjacent)
 * 2. Trait buffer: ChaosCapabilityTrait internal storage (auto-imported from adjacent chaos holders)
 *
 * The proxy tries direct access first, then falls back to trait buffer.
 */
public class ChaosCapabilityProxy extends CapabilityProxy<Integer> {

    private int lastChaos = -1;

    public ChaosCapabilityProxy(BlockEntity blockEntity) {
        super(ChaosMultiblockCapability.CAP, blockEntity);
    }

    @Override
    protected List<Integer> handleRecipeInner(@Nonnull IO io, @Nonnull Recipe recipe,
                                               @Nonnull List<Integer> left, String slotName,
                                               boolean simulate) {
        BlockEntity be = getTileEntity();
        if (be == null) return left;

        // Try direct access to real DraconicAdditions chaos holder
        if (be instanceof TileChaosHolderBase chaosHolder) {
            return handleWithChaosHolder(io, chaosHolder, left, simulate);
        }

        // Fallback: use trait internal buffer
        ChaosCapabilityTrait trait = ChaosCapabilityTrait.getTraitFor(be);
        if (trait != null) {
            return handleWithTrait(io, trait, left, simulate);
        }

        return left;
    }

    private List<Integer> handleWithChaosHolder(IO io, TileChaosHolderBase chaosHolder,
                                                 List<Integer> left, boolean simulate) {
        List<Integer> remaining = new ArrayList<>();
        for (Integer required : left) {
            if (required == null || required <= 0) continue;

            if (io == IO.IN) {
                int available = chaosHolder.chaos.get();
                int extractable = Math.min(required, available);
                if (!simulate && extractable > 0) {
                    chaosHolder.chaos.subtract(extractable);
                }
                int leftover = required - extractable;
                if (leftover > 0) remaining.add(leftover);
            } else if (io == IO.OUT) {
                int space = chaosHolder.getMaxChaos() - chaosHolder.chaos.get();
                int insertable = Math.min(required, space);
                if (!simulate && insertable > 0) {
                    chaosHolder.chaos.add(insertable);
                }
                int leftover = required - insertable;
                if (leftover > 0) remaining.add(leftover);
            }
        }
        return remaining.isEmpty() ? null : remaining;
    }

    private List<Integer> handleWithTrait(IO io, ChaosCapabilityTrait trait,
                                           List<Integer> left, boolean simulate) {
        List<Integer> remaining = new ArrayList<>();
        for (Integer required : left) {
            if (required == null || required <= 0) continue;

            if (io == IO.IN) {
                int available = trait.getChaos();
                int extractable = Math.min(required, available);
                if (!simulate && extractable > 0) {
                    trait.subtractChaos(extractable);
                }
                int leftover = required - extractable;
                if (leftover > 0) remaining.add(leftover);
            } else if (io == IO.OUT) {
                int space = trait.getMaxChaos() - trait.getChaos();
                int insertable = Math.min(required, space);
                if (!simulate && insertable > 0) {
                    trait.addChaos(insertable);
                }
                int leftover = required - insertable;
                if (leftover > 0) remaining.add(leftover);
            }
        }
        return remaining.isEmpty() ? null : remaining;
    }

    @Override
    protected boolean hasInnerChanged() {
        BlockEntity be = getTileEntity();
        if (be == null) return false;

        int currentChaos = -1;

        if (be instanceof TileChaosHolderBase chaosHolder) {
            currentChaos = chaosHolder.chaos.get();
        } else {
            ChaosCapabilityTrait trait = ChaosCapabilityTrait.getTraitFor(be);
            if (trait != null) {
                currentChaos = trait.getChaos();
            }
        }

        if (currentChaos != lastChaos) {
            lastChaos = currentChaos;
            return true;
        }
        return false;
    }
}
