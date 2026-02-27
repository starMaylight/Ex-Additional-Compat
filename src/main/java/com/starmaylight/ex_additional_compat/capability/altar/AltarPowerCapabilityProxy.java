package com.starmaylight.ex_additional_compat.capability.altar;

import com.lowdragmc.multiblocked.api.capability.IO;
import com.lowdragmc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.lowdragmc.multiblocked.api.recipe.Recipe;
import com.favouriteless.enchanted.api.power.IPowerProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxy for consuming Altar Power from Enchanted: Witchcraft altars.
 * INPUT ONLY - this proxy does not support power output.
 *
 * The proxy checks if the connected block entity is an IPowerProvider (altar)
 * and calls tryConsumePower() to consume power for recipe processing.
 */
public class AltarPowerCapabilityProxy extends CapabilityProxy<Double> {

    public AltarPowerCapabilityProxy(BlockEntity blockEntity) {
        super(AltarPowerMultiblockCapability.CAP, blockEntity);
    }

    @Override
    protected List<Double> handleRecipeInner(@Nonnull IO io, @Nonnull Recipe recipe,
                                              @Nonnull List<Double> left, String slotName,
                                              boolean simulate) {
        // Only support INPUT
        if (io != IO.IN) return left;

        BlockEntity be = getTileEntity();
        if (be == null) return left;

        if (!(be instanceof IPowerProvider provider)) return left;

        List<Double> remaining = new ArrayList<>();

        for (Double required : left) {
            if (required == null || required <= 0) continue;

            if (simulate) {
                // For simulation, we can't truly check without consuming
                // IPowerProvider only has tryConsumePower which actually consumes
                // We assume it's available for simulation purposes
                // This is a limitation of the Enchanted API
                continue;
            }

            boolean success = provider.tryConsumePower(required);
            if (!success) {
                remaining.add(required);
            }
        }

        return remaining.isEmpty() ? null : remaining;
    }

    @Override
    protected boolean hasInnerChanged() {
        // IPowerProvider doesn't expose current power level via API
        // Always return true so the recipe system retries each tick
        // (altar recharges over time, so state changes frequently)
        return true;
    }
}
