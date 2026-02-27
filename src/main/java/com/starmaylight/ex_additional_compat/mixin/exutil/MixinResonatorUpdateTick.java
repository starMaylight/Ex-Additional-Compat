package com.starmaylight.ex_additional_compat.mixin.exutil;

import com.starmaylight.ex_additional_compat.jei.exutil.ResonatorRecipe;
import com.starmaylight.ex_additional_compat.recipe.ExUtilRecipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import inzhefop.extrautilitiesrebirth.procedures.ResonatorUpdateTickProcedure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept Extra Utilities Reborn Resonator recipe processing.
 *
 * Only intercepts CUSTOM (non-default) recipes added via ExUtilRecipeRegistry.
 * Default recipes are left to the original execute() method.
 *
 * Processing logic mirrors the original bytecode exactly:
 * - Energy: gate check (stored > totalFE), bulk extraction on completion
 * - Ticks: stored as double in NBT "ticks" key
 * - Speed upgrades: slot 2, bonus = 1.0 + count * 1.5625 per tick
 * - Item I/O: copy+shrink+setStackInSlot (bypasses handler validation)
 *
 * Target: inzhefop.extrautilitiesrebirth.procedures.ResonatorUpdateTickProcedure
 * Method: static void execute(LevelAccessor, double, double, double)
 *
 * Resonator slot layout (from original bytecode):
 * - Slot 0: Input
 * - Slot 1: Output
 * - Slot 2: Speed upgrade
 * - Energy: IEnergyStorage capability
 * - NBT "ticks": Processing progress counter (double)
 */
@Mixin(value = ResonatorUpdateTickProcedure.class, remap = false)
public abstract class MixinResonatorUpdateTick {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void excompat$checkCustomRecipe(
            LevelAccessor world, double x, double y, double z,
            CallbackInfo ci) {

        // Ensure registry is initialized
        ExUtilRecipeRegistry.init();

        BlockPos pos = new BlockPos(x, y, z);
        BlockEntity be = world.getBlockEntity(pos);
        if (be == null) return;

        // Get item handler capability
        IItemHandler items = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElse(null);
        if (items == null || items.getSlots() < 2) return;

        ItemStack input = items.getStackInSlot(0);
        if (input.isEmpty()) return;

        // Look up recipe in our registry
        ResonatorRecipe recipe = ExUtilRecipeRegistry.findResonatorRecipe(input);

        // Default recipes or no match: let original method handle it
        if (recipe == null || ExUtilRecipeRegistry.isDefaultResonatorRecipe(recipe)) return;

        // ===== Custom recipe found — handle it ourselves =====

        ItemStack outputSlot = items.getStackInSlot(1);
        int requiredInput = recipe.getInput().getCount();
        int outputCount = recipe.getOutput().getCount();

        // Input count check
        if (input.getCount() < requiredInput) {
            resetTicksAndCancel(world, be, pos, ci);
            return;
        }

        // Output slot compatibility check (matches original logic)
        if (!outputSlot.isEmpty()) {
            if (outputSlot.getItem() != recipe.getOutput().getItem()) {
                // Output slot has wrong item — reset ticks and abort
                resetTicksAndCancel(world, be, pos, ci);
                return;
            }
            if (outputSlot.getCount() >= (65 - outputCount)) {
                // Output slot full — reset ticks and abort
                resetTicksAndCancel(world, be, pos, ci);
                return;
            }
        }

        // Energy gate check (original: stored > totalFE)
        IEnergyStorage energy = be.getCapability(CapabilityEnergy.ENERGY).orElse(null);
        if (energy == null) {
            resetTicksAndCancel(world, be, pos, ci);
            return;
        }
        if (energy.getEnergyStored() <= recipe.getTotalFE()) {
            // Not enough energy — reset ticks and abort
            resetTicksAndCancel(world, be, pos, ci);
            return;
        }

        // ===== Phase 3: Increment ticks (server-side only) =====
        var tileData = be.getTileData();
        if (!world.isClientSide()) {
            // Speed upgrade check (slot 2)
            double speedBonus = 0.0;
            if (items.getSlots() > 2) {
                ItemStack upgradeSlot = items.getStackInSlot(2);
                if (ExUtilRecipeRegistry.isSpeedUpgrade(upgradeSlot)) {
                    speedBonus = upgradeSlot.getCount();
                }
            }

            double ticks = tileData.getDouble("ticks");
            ticks += 1.0 + speedBonus * 1.5625;
            tileData.putDouble("ticks", ticks);

            // Notify block update
            if (world instanceof Level level) {
                BlockState state = level.getBlockState(pos);
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        // ===== Phase 4: Check completion =====
        double currentTicks = tileData.getDouble("ticks");
        if (currentTicks >= (double) recipe.getTicks()) {
            // --- Consume input: copy + shrink + setStackInSlot ---
            if (items instanceof IItemHandlerModifiable modifiable) {
                ItemStack inputCopy = items.getStackInSlot(0).copy();
                inputCopy.shrink(requiredInput);
                modifiable.setStackInSlot(0, inputCopy);
            }

            // --- Extract energy in bulk ---
            energy.extractEnergy(recipe.getTotalFE(), false);

            // --- Place output: setStackInSlot ---
            if (items instanceof IItemHandlerModifiable modifiable) {
                ItemStack out = recipe.getOutput().copy();
                out.setCount(items.getStackInSlot(1).getCount() + outputCount);
                modifiable.setStackInSlot(1, out);
            }

            // --- Reset ticks ---
            if (!world.isClientSide()) {
                tileData.putDouble("ticks", 0.0);
                if (world instanceof Level level) {
                    BlockState state = level.getBlockState(pos);
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        }

        be.setChanged();
        ci.cancel();
    }

    /**
     * Reset ticks to 0 and cancel original method.
     * Called when conditions are not met (no energy, output full, etc.)
     * Matches original behavior: when recipe/energy check fails, ticks reset to 0.
     */
    private static void resetTicksAndCancel(LevelAccessor world, BlockEntity be,
                                             BlockPos pos, CallbackInfo ci) {
        if (!world.isClientSide()) {
            be.getTileData().putDouble("ticks", 0.0);
            if (world instanceof Level level) {
                BlockState state = level.getBlockState(pos);
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
        be.setChanged();
        ci.cancel();
    }
}
