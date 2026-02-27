package com.starmaylight.ex_additional_compat.kubejs.crossroads;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;

import java.util.Map;

/**
 * Helper for converting various fluid input representations to JSON.
 * Crossroads uses a custom FluidIngredient format that supports both
 * exact fluid matching and fluid tag matching.
 */
public class FluidInputHelper {

    /**
     * Convert a fluid input object to JSON.
     * Accepts: FluidStackJS, JsonObject, or Map-like object with "fluid"/"tag" and "amount" keys.
     */
    @SuppressWarnings("unchecked")
    public static JsonObject toJson(Object input) {
        if (input instanceof JsonObject jo) {
            return jo;
        }
        if (input instanceof FluidStackJS fluid) {
            return fluid.toJson();
        }
        if (input instanceof Map<?, ?> map) {
            JsonObject jo = new JsonObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                Object value = entry.getValue();
                if (value instanceof Number num) {
                    jo.addProperty(key, num);
                } else if (value instanceof String str) {
                    jo.addProperty(key, str);
                } else if (value instanceof Boolean bool) {
                    jo.addProperty(key, bool);
                }
            }
            return jo;
        }
        // Fallback: try to parse as FluidStackJS
        FluidStackJS fluid = FluidStackJS.of(input);
        return fluid.toJson();
    }
}
