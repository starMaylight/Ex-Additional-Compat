package com.starmaylight.ex_additional_compat.mixin.multiblocked;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.multiblocked.api.capability.MultiblockCapability;
import com.lowdragmc.multiblocked.api.json.RecipeTypeAdapter;
import com.lowdragmc.multiblocked.api.recipe.Content;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to fix NPE in RecipeTypeAdapter.serializeIO() when a recipe's
 * I/O map contains a capability reference whose serializer is null.
 *
 * Always handles serialization ourselves to ensure custom capabilities
 * are properly serialized with debug logging.
 */
@Mixin(value = RecipeTypeAdapter.class, remap = false)
public abstract class MixinRecipeTypeAdapter {

    private static final Logger EXCOMPAT_LOGGER = LogUtils.getLogger();

    /**
     * Always intercept serializeIO to ensure custom capabilities are properly handled.
     * Logs all capabilities for debugging.
     */
    @Inject(method = "serializeIO", at = @At("HEAD"), cancellable = true)
    private void excompat$safeSerializeIO(
            ImmutableMap<MultiblockCapability<?>, ImmutableList<Content>> io,
            CallbackInfoReturnable<JsonElement> cir) {

        EXCOMPAT_LOGGER.info("[ExCompat] serializeIO called with {} capabilities: {}",
                io.size(), io.keySet().stream()
                        .map(c -> c == null ? "null" : c.name)
                        .reduce((a, b) -> a + ", " + b).orElse("(empty)"));

        JsonObject result = new JsonObject();
        io.forEach((capability, contents) -> {
            if (capability == null) {
                EXCOMPAT_LOGGER.warn("[ExCompat] serializeIO: skipping null capability");
                return;
            }
            if (capability.serializer == null) {
                EXCOMPAT_LOGGER.warn("[ExCompat] serializeIO: skipping capability '{}' with null serializer",
                        capability.name);
                return;
            }
            JsonArray array = new JsonArray();
            result.add(capability.name, array);
            for (Content content : contents) {
                try {
                    JsonElement json = capability.serializer.toJsonContent(content);
                    array.add(json);
                    EXCOMPAT_LOGGER.info("[ExCompat] serializeIO: {} -> content={}, json={}",
                            capability.name, content.content, json);
                } catch (Exception e) {
                    EXCOMPAT_LOGGER.error("[ExCompat] serializeIO: error serializing content for '{}': {}",
                            capability.name, e.getMessage());
                }
            }
        });
        EXCOMPAT_LOGGER.info("[ExCompat] serializeIO result: {}", result);
        cir.setReturnValue(result);
    }
}
