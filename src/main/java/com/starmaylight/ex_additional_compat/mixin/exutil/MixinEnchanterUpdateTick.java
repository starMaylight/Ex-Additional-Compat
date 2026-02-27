package com.starmaylight.ex_additional_compat.mixin.exutil;

import com.mojang.logging.LogUtils;
import com.starmaylight.ex_additional_compat.jei.exutil.EnchanterRecipe;
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
import org.slf4j.Logger;
import inzhefop.extrautilitiesrebirth.procedures.EnchanterUpdateTickProcedure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept Extra Utilities Reborn Enchanter recipe processing.
 *
 * Only intercepts CUSTOM (non-default) recipes added via ExUtilRecipeRegistry.
 * Default recipes are left to the original execute() method.
 *
 * Processing logic mirrors the original bytecode exactly:
 * - Energy: gate check (stored >= totalFE), bulk extraction on completion
 * - Progress: stored as double in NBT "progress" key, "progresstime" for target
 * - Speed upgrades: slot 3, bonus = 1.0 + count * 1.54 per tick,
 *   energy cost scales linearly with speed upgrade count
 * - Item I/O: copy+shrink+setStackInSlot (bypasses handler validation)
 *
 * Target: inzhefop.extrautilitiesrebirth.procedures.EnchanterUpdateTickProcedure
 * Method: static void execute(LevelAccessor, double, double, double)
 *
 * Enchanter slot layout (from original bytecode):
 * - Slot 0: Main input
 * - Slot 1: Catalyst input
 * - Slot 2: Output
 * - Slot 3: Speed upgrade
 * - Energy: IEnergyStorage capability
 * - NBT "progress": Current progress counter (double)
 * - NBT "progresstime": Required ticks for current recipe (double)
 */
@Mixin(value = EnchanterUpdateTickProcedure.class, remap = false)
public abstract class MixinEnchanterUpdateTick {

    private static final Logger EXCOMPAT_LOGGER = LogUtils.getLogger();
    private static boolean loggedOnce = false;

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
        if (items == null || items.getSlots() < 3) return;

        ItemStack mainInput = items.getStackInSlot(0);
        ItemStack catalyst = items.getStackInSlot(1);
        if (mainInput.isEmpty() || catalyst.isEmpty()) return;

        // Look up recipe in our registry
        EnchanterRecipe recipe = ExUtilRecipeRegistry.findEnchanterRecipe(mainInput, catalyst);

        if (!loggedOnce) {
            EXCOMPAT_LOGGER.info("[ExCompat] Enchanter mixin active! main={}, catalyst={}, recipe={}, isDefault={}",
                    mainInput.getItem(), catalyst.getItem(),
                    recipe != null ? recipe.getOutput().getItem() : "null",
                    recipe != null ? ExUtilRecipeRegistry.isDefaultEnchanterRecipe(recipe) : "N/A");
            loggedOnce = true;
        }

        // Default recipes or no match: let original method handle it
        if (recipe == null || ExUtilRecipeRegistry.isDefaultEnchanterRecipe(recipe)) return;

        // ===== Custom recipe found — handle it ourselves =====

        ItemStack outputSlot = items.getStackInSlot(2);
        int mainInputCount = recipe.getInputMain().getCount();
        int catalystCount = recipe.getInputCatalyst().getCount();
        int outputCount = recipe.getOutput().getCount();

        // Speed upgrade check (slot 3)
        double speedBonus = 0.0;
        int totalFE = recipe.getTotalFE();
        if (items.getSlots() > 3) {
            ItemStack upgradeSlot = items.getStackInSlot(3);
            if (ExUtilRecipeRegistry.isSpeedUpgrade(upgradeSlot)) {
                speedBonus = upgradeSlot.getCount();
                // Energy cost scales linearly: base + (base * count * 0.154)
                totalFE = (int) (recipe.getTotalFE() * (1.0 + speedBonus * 0.154));
            }
        }

        // Gate checks (match original bytecode):
        // 1. Enough main input
        if (items.getStackInSlot(0).getCount() < mainInputCount) {
            resetProgressAndCancel(world, be, pos, ci);
            return;
        }
        // 2. Enough catalyst
        if (items.getStackInSlot(1).getCount() < catalystCount) {
            resetProgressAndCancel(world, be, pos, ci);
            return;
        }
        // 3. Output slot has room
        if (outputSlot.getCount() >= (65 - outputCount)) {
            resetProgressAndCancel(world, be, pos, ci);
            return;
        }
        // 4. Energy gate check (stored >= totalFE)
        IEnergyStorage energy = be.getCapability(CapabilityEnergy.ENERGY).orElse(null);
        if (energy == null) {
            resetProgressAndCancel(world, be, pos, ci);
            return;
        }
        if (energy.getEnergyStored() < totalFE) {
            resetProgressAndCancel(world, be, pos, ci);
            return;
        }
        // 5. Output slot empty OR contains matching item
        if (!outputSlot.isEmpty() && outputSlot.getItem() != recipe.getOutput().getItem()) {
            resetProgressAndCancel(world, be, pos, ci);
            return;
        }

        // ===== Phase 3: Increment progress (server-side only) =====
        // Store progresstime for GUI display consistency
        var tileData = be.getTileData();
        if (!world.isClientSide()) {
            tileData.putDouble("progresstime", (double) recipe.getTicks());

            double progress = tileData.getDouble("progress");
            progress += 1.0 + speedBonus * 1.54;
            tileData.putDouble("progress", progress);

            // Notify block update
            if (world instanceof Level level) {
                BlockState state = level.getBlockState(pos);
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        // ===== Phase 4: Check completion =====
        double currentProgress = tileData.getDouble("progress");
        double progressTime = tileData.getDouble("progresstime");
        if (progressTime <= 0) progressTime = recipe.getTicks();

        if (currentProgress >= progressTime) {
            if (items instanceof IItemHandlerModifiable modifiable) {
                // --- Place output: setStackInSlot (slot 2) ---
                ItemStack out = recipe.getOutput().copy();
                out.setCount(items.getStackInSlot(2).getCount() + outputCount);
                modifiable.setStackInSlot(2, out);

                // --- Consume main input: copy + shrink + setStackInSlot (slot 0) ---
                ItemStack mainCopy = items.getStackInSlot(0).copy();
                mainCopy.shrink(mainInputCount);
                modifiable.setStackInSlot(0, mainCopy);

                // --- Extract energy in bulk ---
                energy.extractEnergy(totalFE, false);

                // --- Consume catalyst: copy + shrink + setStackInSlot (slot 1) ---
                ItemStack catalystCopy = items.getStackInSlot(1).copy();
                catalystCopy.shrink(catalystCount);
                modifiable.setStackInSlot(1, catalystCopy);
            }

            // --- Reset progress ---
            if (!world.isClientSide()) {
                tileData.putDouble("progress", 0.0);
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
     * Reset progress to 0 and cancel original method.
     * Called when conditions are not met (no energy, output full, etc.)
     * Matches original behavior: when checks fail, progress resets to 0.
     */
    private static void resetProgressAndCancel(LevelAccessor world, BlockEntity be,
                                                BlockPos pos, CallbackInfo ci) {
        if (!world.isClientSide()) {
            be.getTileData().putDouble("progress", 0.0);
            if (world instanceof Level level) {
                BlockState state = level.getBlockState(pos);
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
        be.setChanged();
        ci.cancel();
    }
}
