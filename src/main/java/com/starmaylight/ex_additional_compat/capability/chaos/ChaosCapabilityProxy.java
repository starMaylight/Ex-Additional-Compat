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
 * Directly accesses TileChaosHolderBase.chaos ManagedInt field:
 * - chaos.get() - current chaos amount
 * - chaos.add(int) - add chaos
 * - chaos.subtract(int) - remove chaos
 *
 * IN: Extract chaos from adjacent TileChaosHolderBase
 * OUT: Insert chaos into adjacent TileChaosHolderBase
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
        if (!(be instanceof TileChaosHolderBase chaosHolder)) return left;

        List<Integer> remaining = new ArrayList<>();

        for (Integer required : left) {
            if (required == null || required <= 0) continue;

            if (io == IO.IN) {
                // Extract chaos from the DraconicAdditions block
                int available = chaosHolder.chaos.get();
                int extractable = Math.min(required, available);
                if (!simulate && extractable > 0) {
                    chaosHolder.chaos.subtract(extractable);
                }
                int leftover = required - extractable;
                if (leftover > 0) {
                    remaining.add(leftover);
                }
            } else if (io == IO.OUT) {
                // Insert chaos into the DraconicAdditions block
                int currentChaos = chaosHolder.chaos.get();
                int maxChaos = chaosHolder.getMaxChaos();
                int space = maxChaos - currentChaos;
                int insertable = Math.min(required, space);
                if (!simulate && insertable > 0) {
                    chaosHolder.chaos.add(insertable);
                }
                int leftover = required - insertable;
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
        if (!(be instanceof TileChaosHolderBase chaosHolder)) return false;

        int currentChaos = chaosHolder.chaos.get();
        if (currentChaos != lastChaos) {
            lastChaos = currentChaos;
            return true;
        }
        return false;
    }
}
