package com.starmaylight.ex_additional_compat.mixin.multiblocked;

import com.lowdragmc.multiblocked.api.capability.MultiblockCapability;
import com.lowdragmc.multiblocked.api.registry.MbdCapabilities;
import com.starmaylight.ex_additional_compat.capability.ExCompatCapabilityRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to provide a fallback lookup for custom capabilities in
 * {@link MbdCapabilities#get(String)}.
 *
 * <p>When Multiblocked deserializes RecipeMap JSON files during its
 * {@code enqueueWork()}, it calls {@code MbdCapabilities.get(name)} for
 * each capability in the recipe. Our custom capabilities haven't been
 * added to the official {@code CAPABILITY_REGISTRY} yet at that point
 * (our {@code enqueueWork()} runs after Multiblocked's).
 *
 * <p>This mixin intercepts the return value: if the normal lookup returns
 * {@code null}, it checks the {@link ExCompatCapabilityRegistry} fallback
 * map (populated during mod construction).
 */
@Mixin(value = MbdCapabilities.class, remap = false)
public abstract class MixinMbdCapabilities {

    /**
     * After the original get() returns, if result is null, check our
     * early-registered capabilities as a fallback.
     */
    @Inject(method = "get", at = @At("RETURN"), cancellable = true)
    private static void excompat$fallbackGet(String name,
                                              CallbackInfoReturnable<MultiblockCapability<?>> cir) {
        if (cir.getReturnValue() == null) {
            MultiblockCapability<?> cap = ExCompatCapabilityRegistry.get(name);
            if (cap != null) {
                cir.setReturnValue(cap);
            }
        }
    }
}
