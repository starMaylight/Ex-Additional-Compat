package com.starmaylight.ex_additional_compat.mixin.multiblocked;

import com.lowdragmc.multiblocked.api.capability.MultiblockCapability;
import com.lowdragmc.multiblocked.api.capability.trait.CapabilityTrait;
import com.lowdragmc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Mixin to call trait.onLoad() when a ComponentTileEntity's level is set.
 *
 * Problem: Multiblocked's ComponentTileEntity.initTrait() creates traits and calls
 * setComponent(), but NEVER calls trait.onLoad(). Our custom traits rely on onLoad()
 * to register in static TRAIT_REGISTRY maps, which proxies use to find traits during
 * recipe processing. Without this mixin, TRAIT_REGISTRY is always empty and all
 * proxy-trait connections fail.
 *
 * Solution: Inject at the tail of setLevel() (called after construction + NBT loading)
 * to invoke onLoad() on each trait, ensuring they can register themselves.
 */
@Mixin(value = ComponentTileEntity.class, remap = false)
public abstract class MixinComponentTileEntity {

    @Shadow
    protected Map<MultiblockCapability<?>, CapabilityTrait> traits;

    @Unique
    private boolean excompat$traitsLoaded = false;

    @Inject(method = "setLevel", at = @At("TAIL"), remap = true)
    private void excompat$callTraitOnLoad(Level world, CallbackInfo ci) {
        if (!excompat$traitsLoaded && this.traits != null && !this.traits.isEmpty()) {
            excompat$traitsLoaded = true;
            for (CapabilityTrait trait : this.traits.values()) {
                try {
                    trait.onLoad();
                } catch (Exception e) {
                    // Safely ignore individual trait onLoad failures
                }
            }
        }
    }
}
