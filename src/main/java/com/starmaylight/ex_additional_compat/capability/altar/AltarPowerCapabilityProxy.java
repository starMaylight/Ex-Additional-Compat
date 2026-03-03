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
 * Two access modes:
 * 1. Direct: getTileEntity() is an IPowerProvider (real Enchanted altar adjacent)
 * 2. Trait buffer: AltarPowerCapabilityTrait internal storage (auto-imported from nearby altars)
 *
 * The proxy tries direct access first, then falls back to trait buffer.
 */
public class AltarPowerCapabilityProxy extends CapabilityProxy<Double> {

    private double lastPower = -1;

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

        // Try direct access to real Enchanted altar
        if (be instanceof IPowerProvider provider) {
            return handleWithProvider(provider, left, simulate);
        }

        // Fallback: use trait internal buffer
        AltarPowerCapabilityTrait trait = AltarPowerCapabilityTrait.getTraitFor(be);
        if (trait != null) {
            return handleWithTrait(trait, left, simulate);
        }

        return left;
    }

    private List<Double> handleWithProvider(IPowerProvider provider, List<Double> left, boolean simulate) {
        List<Double> remaining = new ArrayList<>();
        for (Double required : left) {
            if (required == null || required <= 0) continue;

            if (simulate) {
                // IPowerProvider only has tryConsumePower which actually consumes
                // Assume available for simulation
                continue;
            }

            boolean success = provider.tryConsumePower(required);
            if (!success) {
                remaining.add(required);
            }
        }
        return remaining.isEmpty() ? null : remaining;
    }

    private List<Double> handleWithTrait(AltarPowerCapabilityTrait trait, List<Double> left, boolean simulate) {
        List<Double> remaining = new ArrayList<>();
        for (Double required : left) {
            if (required == null || required <= 0) continue;

            if (simulate) {
                // Check if trait has enough power for simulation
                if (trait.getCurrentPower() < required) {
                    remaining.add(required);
                }
                continue;
            }

            boolean success = trait.tryConsumePower(required);
            if (!success) {
                remaining.add(required);
            }
        }
        return remaining.isEmpty() ? null : remaining;
    }

    @Override
    protected boolean hasInnerChanged() {
        BlockEntity be = getTileEntity();
        if (be == null) return false;

        // IPowerProvider doesn't expose current power level
        // When using provider directly, always return true
        if (be instanceof IPowerProvider) {
            return true;
        }

        // When using trait buffer, check if power changed
        AltarPowerCapabilityTrait trait = AltarPowerCapabilityTrait.getTraitFor(be);
        if (trait != null) {
            double current = trait.getCurrentPower();
            if (Math.abs(current - lastPower) > 0.01) {
                lastPower = current;
                return true;
            }
        }
        return false;
    }
}
