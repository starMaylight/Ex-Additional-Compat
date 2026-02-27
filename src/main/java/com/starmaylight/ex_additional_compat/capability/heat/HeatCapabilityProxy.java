package com.starmaylight.ex_additional_compat.capability.heat;

import com.lowdragmc.multiblocked.api.capability.IO;
import com.lowdragmc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.lowdragmc.multiblocked.api.recipe.Recipe;
import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.heat.IHeatHandler;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxy for handling heat energy I/O between Multiblocked machines and Crossroads blocks.
 *
 * Heat system:
 * - IHeatHandler.getTemp() returns current temperature
 * - IHeatHandler.addHeat(double) adds heat (positive = heating, negative = cooling)
 * - Temperature cannot go below -273°C (absolute zero)
 */
public class HeatCapabilityProxy extends CapabilityProxy<Double> {

    private double lastTemp = 0;

    public HeatCapabilityProxy(BlockEntity blockEntity) {
        super(HeatMultiblockCapability.CAP, blockEntity);
    }

    @Override
    protected List<Double> handleRecipeInner(@Nonnull IO io, @Nonnull Recipe recipe,
                                              @Nonnull List<Double> left, String slotName,
                                              boolean simulate) {
        BlockEntity be = getTileEntity();
        if (be == null) return left;

        IHeatHandler handler = be.getCapability(Capabilities.HEAT_CAPABILITY).resolve().orElse(null);
        if (handler == null) return left;

        List<Double> remaining = new ArrayList<>();

        for (Double required : left) {
            if (required == null || required <= 0) continue;

            if (io == IO.IN) {
                // Machine consumes heat: extract heat from the Crossroads block
                double currentTemp = handler.getTemp();
                if (currentTemp <= -273.0) {
                    remaining.add(required);
                    continue;
                }
                // Don't extract more heat than available above absolute zero
                double extractable = Math.min(required, currentTemp + 273.0);
                if (!simulate) {
                    handler.addHeat(-extractable);
                }
                double leftover = required - extractable;
                if (leftover > 0.01) {
                    remaining.add(leftover);
                }
            } else if (io == IO.OUT) {
                // Machine outputs heat: add heat to the Crossroads block
                if (!simulate) {
                    handler.addHeat(required);
                }
                // Heat output always succeeds (heat can always be added)
            }
        }

        return remaining.isEmpty() ? null : remaining;
    }

    @Override
    protected boolean hasInnerChanged() {
        BlockEntity be = getTileEntity();
        if (be == null) return false;

        IHeatHandler handler = be.getCapability(Capabilities.HEAT_CAPABILITY).resolve().orElse(null);
        if (handler == null) return false;

        double currentTemp = handler.getTemp();
        if (Math.abs(currentTemp - lastTemp) > 0.01) {
            lastTemp = currentTemp;
            return true;
        }
        return false;
    }
}
