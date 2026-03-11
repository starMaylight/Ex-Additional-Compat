package com.starmaylight.ex_additional_compat.capability;

import com.lowdragmc.multiblocked.api.capability.MultiblockCapability;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Early capability registry for ExAdditionalCompat custom capabilities.
 *
 * <p>Problem: Multiblocked deserializes RecipeMap JSON files in its own
 * {@code enqueueWork()} during {@code FMLCommonSetupEvent}. During deserialization,
 * {@code MbdCapabilities.get(name)} is called for each capability. If a capability
 * isn't registered yet, its recipe entries are silently dropped.
 *
 * <p>Our capabilities are registered in a later {@code enqueueWork()} call (because
 * Multiblocked loads first as a dependency). This means our capabilities are missing
 * from the registry when recipes are parsed, so Recipe objects lack custom capability
 * entries — causing them to not display in JEI or the controller GUI.
 *
 * <p>We cannot use {@code MbdCapabilities.registerCapability()} earlier because:
 * <ul>
 *   <li>In the constructor: {@code registerAnyCapabilityBlocks()} creates .any predicate
 *       blocks for all registered capabilities during block registration, which shifts
 *       existing block registry IDs and breaks world saves.</li>
 *   <li>Directly in FMLCommonSetupEvent: runs on a worker thread, potentially
 *       concurrent with other code accessing the registry.</li>
 * </ul>
 *
 * <p>Solution: This class holds a static fallback map that is populated during mod
 * construction (before any setup events). A Mixin on {@code MbdCapabilities.get()}
 * checks this fallback when the normal registry returns null. This ensures capabilities
 * are found during recipe deserialization without affecting block registration.
 *
 * <p>The normal {@code enqueueWork()} registration still runs to add capabilities
 * to the official registry (for {@code CAPABILITY_ORDER}, traits, etc.).
 */
public final class ExCompatCapabilityRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Fallback map: capability name -> MultiblockCapability instance.
     * Populated during mod construction, before any FMLCommonSetupEvent fires.
     */
    private static final Map<String, MultiblockCapability<?>> EARLY_CAPABILITIES = new HashMap<>();

    private ExCompatCapabilityRegistry() {}

    /**
     * Register a capability in the early fallback registry.
     * Call this from the mod constructor.
     */
    public static void register(MultiblockCapability<?> cap) {
        EARLY_CAPABILITIES.put(cap.name, cap);
        LOGGER.info("[ExCompat] Early-registered capability: {}", cap.name);
    }

    /**
     * Look up a capability by name from the early registry.
     * Called by the MixinMbdCapabilities fallback.
     *
     * @return the capability, or null if not in our registry
     */
    @Nullable
    public static MultiblockCapability<?> get(String name) {
        return EARLY_CAPABILITIES.get(name);
    }

    /**
     * @return true if the early registry contains the given capability name
     */
    public static boolean contains(String name) {
        return EARLY_CAPABILITIES.containsKey(name);
    }
}
