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
/*
 *
 *   Copyright (c) 2023. Favouriteless
 *   Enchanted, a minecraft mod.
 *   GNU GPLv3 License
 *
 *       This file is part of Enchanted.
 *
 *       Enchanted is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       Enchanted is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU General Public License for more details.
 *
 *       You should have received a copy of the GNU General Public License
 *       along with Enchanted.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
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
