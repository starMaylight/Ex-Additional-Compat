package com.starmaylight.ex_additional_compat.capability.demonwill;

import com.lowdragmc.multiblocked.api.capability.IO;
import com.lowdragmc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.lowdragmc.multiblocked.api.recipe.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.demonaura.WorldDemonWillHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxy for handling Demon Will I/O between Multiblocked machines and the chunk-based will system.
 *
 * IN (absorb): Drains will from the air around the block using WorldDemonWillHandler.drainWill()
 * OUT (emit): Fills will into the air using WorldDemonWillHandler.fillWill()
 *
 * Currently uses DEFAULT will type. Future versions may support will type selection.
 */
public class DemonWillCapabilityProxy extends CapabilityProxy<Double> {

    private double lastWillAmount = -1;

    public DemonWillCapabilityProxy(BlockEntity blockEntity) {
        super(DemonWillMultiblockCapability.CAP, blockEntity);
    }

    @Override
    protected List<Double> handleRecipeInner(@Nonnull IO io, @Nonnull Recipe recipe,
                                              @Nonnull List<Double> left, String slotName,
                                              boolean simulate) {
        BlockEntity be = getTileEntity();
        if (be == null || be.getLevel() == null) return left;

        List<Double> remaining = new ArrayList<>();

        for (Double required : left) {
            if (required == null || required <= 0) continue;

            if (io == IO.IN) {
                // Drain demon will from the air (chunk)
                double drained = 0;
                if (!simulate) {
                    drained = WorldDemonWillHandler.drainWill(be.getLevel(), be.getBlockPos(), EnumDemonWillType.DEFAULT, required, true);
                } else {
                    // For simulation, check current will in chunk
                    drained = WorldDemonWillHandler.getCurrentWill(be.getLevel(), be.getBlockPos(), EnumDemonWillType.DEFAULT);
                    drained = Math.min(drained, required);
                }
                double leftover = required - drained;
                if (leftover > 0.01) {
                    remaining.add(leftover);
                }
            } else if (io == IO.OUT) {
                // Fill demon will into the air (chunk)
                if (!simulate) {
                    WorldDemonWillHandler.fillWill(be.getLevel(), be.getBlockPos(), EnumDemonWillType.DEFAULT, required, true);
                }
                // Will output always succeeds (fills into chunk)
            }
        }

        return remaining.isEmpty() ? null : remaining;
    }

    @Override
    protected boolean hasInnerChanged() {
        BlockEntity be = getTileEntity();
        if (be == null || be.getLevel() == null) return false;

        double currentWill = WorldDemonWillHandler.getCurrentWill(be.getLevel(), be.getBlockPos(), EnumDemonWillType.DEFAULT);
        if (Math.abs(currentWill - lastWillAmount) > 0.01) {
            lastWillAmount = currentWill;
            return true;
        }
        return false;
    }
}
