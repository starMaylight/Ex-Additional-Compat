package com.starmaylight.ex_additional_compat.capability.demonwill;

import com.google.common.collect.ImmutableList;
import com.lowdragmc.multiblocked.api.capability.IO;
import com.lowdragmc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.lowdragmc.multiblocked.api.recipe.Content;
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
 * Will type is read from Content.uiName (set by DemonWillContentWidget's type selector).
 * Supports all 5 will types: DEFAULT, CORROSIVE, DESTRUCTIVE, VENGEFUL, STEADFAST.
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

        // Get the original Content list from the recipe to read will types
        ImmutableList<Content> contents = null;
        try {
            if (io == IO.IN && recipe.inputs.containsKey(DemonWillMultiblockCapability.CAP)) {
                contents = recipe.inputs.get(DemonWillMultiblockCapability.CAP);
            } else if (io == IO.OUT && recipe.outputs.containsKey(DemonWillMultiblockCapability.CAP)) {
                contents = recipe.outputs.get(DemonWillMultiblockCapability.CAP);
            }
        } catch (Exception ignored) {}

        List<Double> remaining = new ArrayList<>();

        for (int i = 0; i < left.size(); i++) {
            Double required = left.get(i);
            if (required == null || required <= 0) continue;

            // Determine will type from the recipe Content's uiName
            EnumDemonWillType willType = EnumDemonWillType.DEFAULT;
            if (contents != null && i < contents.size()) {
                String typeName = contents.get(i).uiName;
                if (typeName != null && !typeName.isEmpty()) {
                    try {
                        willType = EnumDemonWillType.valueOf(typeName);
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            if (io == IO.IN) {
                // Drain demon will from the air (chunk)
                double drained;
                if (!simulate) {
                    drained = WorldDemonWillHandler.drainWill(be.getLevel(), be.getBlockPos(), willType, required, true);
                } else {
                    // For simulation, check current will in chunk
                    drained = WorldDemonWillHandler.getCurrentWill(be.getLevel(), be.getBlockPos(), willType);
                    drained = Math.min(drained, required);
                }
                double leftover = required - drained;
                if (leftover > 0.01) {
                    remaining.add(leftover);
                }
            } else if (io == IO.OUT) {
                // Fill demon will into the air (chunk)
                if (!simulate) {
                    WorldDemonWillHandler.fillWill(be.getLevel(), be.getBlockPos(), willType, required, true);
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

        // Check all will types for changes
        double totalWill = 0;
        for (EnumDemonWillType type : EnumDemonWillType.values()) {
            totalWill += WorldDemonWillHandler.getCurrentWill(be.getLevel(), be.getBlockPos(), type);
        }

        if (Math.abs(totalWill - lastWillAmount) > 0.01) {
            lastWillAmount = totalWill;
            return true;
        }
        return false;
    }
}
