package com.starmaylight.ex_additional_compat.mixin.enchanted;

import com.favouriteless.enchanted.api.rites.AbstractRite;
import com.favouriteless.enchanted.common.init.registry.RiteTypes;
import com.favouriteless.enchanted.common.rites.RiteType;
import com.starmaylight.ex_additional_compat.kubejs.enchanted.EnchantedRiteRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

/**
 * Extends RiteTypes.getRiteAt() to also check rites registered by ExCompat.
 *
 * The original method only iterates over Enchanted's own DeferredRegister entries.
 * This mixin injects at RETURN to additionally check our custom registered rites,
 * allowing KubeJS-defined rites to be activated by gold chalk.
 */
@Mixin(value = RiteTypes.class, remap = false)
public class MixinRiteTypes {

    @Inject(method = "getRiteAt", at = @At("RETURN"), cancellable = true)
    private static void excompat$onGetRiteAt(ServerLevel level, BlockPos pos, UUID caster,
                                              CallbackInfoReturnable<AbstractRite> cir) {
        AbstractRite currentBest = cir.getReturnValue();
        int currentDiff = currentBest != null ? currentBest.differenceAt((Level) level, pos) : Integer.MAX_VALUE;

        List<RiteType<?>> customRites = EnchantedRiteRegistry.getRegisteredRites();

        // Check all rites registered through ExCompat
        for (RiteType<?> type : customRites) {
            try {
                AbstractRite rite = type.create(level, pos, caster);
                int diff = rite.differenceAt((Level) level, pos);

                if (diff != -1 && diff < currentDiff) {
                    currentBest = rite;
                    currentDiff = diff;
                }
            } catch (Exception ignored) {
                // Skip any rite that fails to create or evaluate
            }
        }

        if (currentBest != cir.getReturnValue()) {
            cir.setReturnValue(currentBest);
        }
    }
}
