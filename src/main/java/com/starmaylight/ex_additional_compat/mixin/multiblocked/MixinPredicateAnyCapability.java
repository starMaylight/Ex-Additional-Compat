package com.starmaylight.ex_additional_compat.mixin.multiblocked;

import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.multiblocked.api.capability.MultiblockCapability;
import com.lowdragmc.multiblocked.api.pattern.predicates.PredicateAnyCapability;
import com.lowdragmc.multiblocked.api.pattern.predicates.SimplePredicate;
import com.lowdragmc.multiblocked.api.registry.MbdCapabilities;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

/**
 * Mixin to fix NPE in PredicateAnyCapability.buildPredicate() when
 * getAnyBlock() returns null for custom capabilities that don't have
 * a registered "multiblocked:<name>_any" block component.
 *
 * The original candidates lambda does: cap.getAnyBlock().defaultBlockState()
 * which is lazily evaluated and throws NPE if getAnyBlock() returns null.
 *
 * This mixin wraps the candidates supplier with a null-safe version that
 * defers the getAnyBlock() check to invocation time, so that blocks
 * registered after buildPredicate() are properly found.
 */
@Mixin(value = PredicateAnyCapability.class, remap = false)
public abstract class MixinPredicateAnyCapability extends SimplePredicate {

    @Shadow
    public String capability;

    /**
     * Inject at the end of buildPredicate() to wrap the candidates supplier
     * with a null-safe version. The original lambda calls getAnyBlock()
     * lazily, but NPEs if the block is not registered. We wrap it so that
     * at invocation time, if getAnyBlock() is still null, we return AIR.
     */
    @Inject(method = "buildPredicate", at = @At("RETURN"))
    private void excompat$fixNullAnyBlock(CallbackInfoReturnable<SimplePredicate> cir) {
        // Capture the original candidates supplier set by buildPredicate()
        final Supplier<BlockInfo[]> originalCandidates = this.candidates;

        // Wrap it with a null-safe version that checks getAnyBlock() at call time
        this.candidates = () -> {
            MultiblockCapability<?> cap = MbdCapabilities.get(this.capability);
            if (cap != null && cap.getAnyBlock() == null) {
                // getAnyBlock() is null at invocation time — the original lambda
                // would NPE. Return a safe fallback.
                return new BlockInfo[]{BlockInfo.fromBlock(Blocks.AIR)};
            }
            // getAnyBlock() is valid (or cap not found), use original behavior
            try {
                return originalCandidates.get();
            } catch (NullPointerException e) {
                // Safety net in case of any other null reference
                return new BlockInfo[]{BlockInfo.fromBlock(Blocks.AIR)};
            }
        };
    }
}
