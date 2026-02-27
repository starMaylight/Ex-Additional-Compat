package com.starmaylight.ex_additional_compat.capability.lp;

import com.lowdragmc.multiblocked.api.capability.IO;
import com.lowdragmc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.lowdragmc.multiblocked.api.recipe.Recipe;
import wayoftime.bloodmagic.core.data.Binding;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.util.helper.NetworkHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxy for handling LP I/O between Multiblocked machines and Blood Magic Soul Networks.
 *
 * LP operations require a BloodOrb to be present in the LPCapabilityTrait's orb slot.
 * The orb provides the owner UUID, which is used to access the player's Soul Network.
 *
 * This proxy accesses the BloodOrb through LPCapabilityTrait's static registry,
 * which maps BlockEntity positions to their associated trait instances.
 *
 * IN: Syphons LP from the owner's network (network.syphon(amount))
 * OUT: Adds LP to the owner's network (network.add(amount, orbTier))
 */
public class LPCapabilityProxy extends CapabilityProxy<Integer> {

    public LPCapabilityProxy(BlockEntity blockEntity) {
        super(LPMultiblockCapability.CAP, blockEntity);
    }

    @Override
    protected List<Integer> handleRecipeInner(@Nonnull IO io, @Nonnull Recipe recipe,
                                               @Nonnull List<Integer> left, String slotName,
                                               boolean simulate) {
        BlockEntity be = getTileEntity();
        if (be == null || be.getLevel() == null) return left;

        // Get the LPCapabilityTrait associated with this machine
        LPCapabilityTrait trait = LPCapabilityTrait.getTraitFor(be);
        if (trait == null) return left;

        // Get BloodOrb from the trait's orb slot
        ItemStack orbStack = trait.getOrb();
        if (orbStack.isEmpty()) return left;

        // Get Binding (player UUID) from the orb
        Binding binding = Binding.fromStack(orbStack);
        if (binding == null) return left;

        // Access the player's Soul Network
        SoulNetwork network = NetworkHelper.getSoulNetwork(binding);
        if (network == null) return left;

        List<Integer> remaining = new ArrayList<>();

        for (Integer required : left) {
            if (required == null || required <= 0) continue;

            if (io == IO.IN) {
                // Syphon LP from player's network
                if (simulate) {
                    if (network.getCurrentEssence() < required) {
                        remaining.add(required - network.getCurrentEssence());
                    }
                } else {
                    int syphoned = network.syphon(required);
                    int leftover = required - syphoned;
                    if (leftover > 0) {
                        remaining.add(leftover);
                    }
                }
            } else if (io == IO.OUT) {
                // Add LP to player's network
                if (!simulate) {
                    network.add(required, network.getOrbTier());
                }
                // LP output always succeeds
            }
        }

        return remaining.isEmpty() ? null : remaining;
    }

    @Override
    protected boolean hasInnerChanged() {
        // Soul Network state is external to the block, so always recheck
        return true;
    }
}
