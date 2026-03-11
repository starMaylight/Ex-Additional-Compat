package com.starmaylight.ex_additional_compat.capability;

import com.google.gson.JsonObject;
import com.lowdragmc.multiblocked.api.capability.IO;
import com.lowdragmc.multiblocked.api.tile.ComponentTileEntity;

/**
 * Helper to check if a Multiblocked component's trait IO configuration
 * is compatible with a requested IO direction during recipe matching.
 */
public final class TraitIOHelper {
    private TraitIOHelper() {}

    /**
     * Check if the component has a trait with the given name and its mbdIO
     * is compatible with the requested IO direction.
     *
     * @param requestedIO the IO direction the recipe needs
     * @param component   the Multiblocked component
     * @param traitName   the capability/trait name (e.g. "crossroads_flux")
     * @return true if the trait exists and its mbdIO allows the requested direction
     */
    public static boolean isTraitIOCompatible(IO requestedIO, ComponentTileEntity<?> component, String traitName) {
        JsonObject traits = component.getDefinition().traits;
        if (traits == null || !traits.has(traitName)) return false;

        IO mbdIO = IO.BOTH; // default per SingleCapabilityTrait
        try {
            JsonObject traitConfig = traits.getAsJsonObject(traitName);
            if (traitConfig != null && traitConfig.has("mbdIO")) {
                int ordinal = traitConfig.get("mbdIO").getAsInt();
                IO[] values = IO.values();
                if (ordinal >= 0 && ordinal < values.length) {
                    mbdIO = values[ordinal];
                }
            }
        } catch (Exception ignored) {
            // Malformed config → default to BOTH
        }

        if (mbdIO == IO.BOTH) return true;
        if (mbdIO == IO.NONE) return false;
        return mbdIO == requestedIO;
    }
}
