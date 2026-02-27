package com.starmaylight.ex_additional_compat.jei.arthana;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Loads Arthana drop data from multiple sources:
 * 1. Enchanted mod JAR (data/<namespace>/arthana/<entity>.json)
 * 2. Datapacks in the game directory (kubejs/data, datapacks, etc.)
 *
 * JSON format:
 *   { "result": { "item": "enchanted:bat_wing", "count": 1 } }
 *
 * The entity type is derived from the file path:
 *   data/minecraft/arthana/bat.json -> minecraft:bat
 */
public final class ArthanaDropRecipeLoader {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    private ArthanaDropRecipeLoader() {}

    /**
     * Load all Arthana drop recipes from all sources.
     * Called during JEI recipe registration.
     */
    public static List<ArthanaDropRecipe> loadRecipes() {
        List<ArthanaDropRecipe> recipes = new ArrayList<>();
        Set<String> loadedIds = new HashSet<>(); // Track entity IDs to avoid duplicates

        // Source 1: Enchanted mod JAR
        loadFromEnchantedJar(recipes, loadedIds);

        // Source 2: All loaded mod JARs (other mods may add arthana drops)
        loadFromAllModJars(recipes, loadedIds);

        // Source 3: kubejs/data directory (datapack-style)
        loadFromKubeJSData(recipes, loadedIds);

        // Source 4: World datapacks
        loadFromDatapacks(recipes, loadedIds);

        LOGGER.info("ArthanaDropRecipeLoader: Loaded {} arthana drop recipes total", recipes.size());
        return Collections.unmodifiableList(recipes);
    }

    /**
     * Load from the Enchanted mod's JAR.
     */
    private static void loadFromEnchantedJar(List<ArthanaDropRecipe> recipes, Set<String> loadedIds) {
        try {
            var modFileInfo = ModList.get().getModFileById("enchanted");
            if (modFileInfo == null) return;

            Path dataRoot = modFileInfo.getFile().findResource("data");
            if (!Files.exists(dataRoot)) return;

            scanDataDirectory(dataRoot, recipes, loadedIds, "Enchanted JAR");
        } catch (Exception e) {
            LOGGER.error("ArthanaDropRecipeLoader: Failed to scan Enchanted JAR", e);
        }
    }

    /**
     * Load from all other loaded mod JARs that might contain arthana data.
     */
    private static void loadFromAllModJars(List<ArthanaDropRecipe> recipes, Set<String> loadedIds) {
        for (var modInfo : ModList.get().getMods()) {
            String modId = modInfo.getModId();
            if ("enchanted".equals(modId)) continue; // Already scanned
            if ("minecraft".equals(modId) || "forge".equals(modId)) continue;

            try {
                var modFileInfo = ModList.get().getModFileById(modId);
                if (modFileInfo == null) continue;

                Path dataRoot = modFileInfo.getFile().findResource("data");
                if (!Files.exists(dataRoot)) continue;

                // Quick check: does this mod have any arthana directory?
                boolean hasArthana;
                try (Stream<Path> paths = Files.walk(dataRoot, 3)) {
                    hasArthana = paths.anyMatch(p -> {
                        String ps = p.toString().replace('\\', '/');
                        return ps.endsWith("/arthana") && Files.isDirectory(p);
                    });
                }
                if (hasArthana) {
                    scanDataDirectory(dataRoot, recipes, loadedIds, "mod:" + modId);
                }
            } catch (Exception ignored) {
                // Some mods may not have standard data directories
            }
        }
    }

    /**
     * Load from kubejs/data directory (KubeJS datapack).
     */
    private static void loadFromKubeJSData(List<ArthanaDropRecipe> recipes, Set<String> loadedIds) {
        try {
            Path kubeJSData = FMLPaths.GAMEDIR.get().resolve("kubejs").resolve("data");
            if (Files.exists(kubeJSData) && Files.isDirectory(kubeJSData)) {
                scanDataDirectory(kubeJSData, recipes, loadedIds, "kubejs/data");
            }
        } catch (Exception e) {
            LOGGER.warn("ArthanaDropRecipeLoader: Failed to scan kubejs/data", e);
        }
    }

    /**
     * Load from world datapacks directory.
     */
    private static void loadFromDatapacks(List<ArthanaDropRecipe> recipes, Set<String> loadedIds) {
        try {
            // Check for global datapacks
            Path globalDatapacks = FMLPaths.GAMEDIR.get().resolve("datapacks");
            if (Files.exists(globalDatapacks) && Files.isDirectory(globalDatapacks)) {
                try (Stream<Path> packs = Files.list(globalDatapacks)) {
                    packs.filter(Files::isDirectory).forEach(pack -> {
                        Path dataDir = pack.resolve("data");
                        if (Files.exists(dataDir)) {
                            scanDataDirectory(dataDir, recipes, loadedIds, "datapack:" + pack.getFileName());
                        }
                    });
                }
            }
        } catch (Exception e) {
            LOGGER.warn("ArthanaDropRecipeLoader: Failed to scan datapacks", e);
        }
    }

    /**
     * Scan a data/ directory for arthana JSON files.
     * Works for both JAR-internal paths (NIO FileSystem) and real filesystem paths.
     */
    private static void scanDataDirectory(Path dataRoot, List<ArthanaDropRecipe> recipes,
                                          Set<String> loadedIds, String source) {
        try (Stream<Path> paths = Files.walk(dataRoot)) {
            paths.filter(p -> {
                String pathStr = p.toString().replace('\\', '/');
                return pathStr.contains("/arthana/") && pathStr.endsWith(".json");
            }).forEach(jsonPath -> {
                try {
                    processArthanaFile(jsonPath, dataRoot, recipes, loadedIds, source);
                } catch (Exception e) {
                    LOGGER.warn("ArthanaDropRecipeLoader: Failed to parse {} from {}: {}",
                            jsonPath, source, e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.warn("ArthanaDropRecipeLoader: Failed to walk {} in {}", dataRoot, source);
        }
    }

    /**
     * Process a single arthana JSON file.
     */
    private static void processArthanaFile(Path jsonPath, Path dataRoot,
                                           List<ArthanaDropRecipe> recipes,
                                           Set<String> loadedIds, String source) throws Exception {
        // Read and parse JSON
        JsonObject json;
        try (BufferedReader reader = Files.newBufferedReader(jsonPath)) {
            JsonElement element = GsonHelper.fromJson(GSON, reader, JsonElement.class);
            if (element == null || !element.isJsonObject()) return;
            json = element.getAsJsonObject();
        }

        // Parse result
        if (!json.has("result")) return;
        JsonObject resultObj = GsonHelper.getAsJsonObject(json, "result");
        String itemId = GsonHelper.getAsString(resultObj, "item");
        int count = GsonHelper.getAsInt(resultObj, "count", 1);

        // Look up item
        ResourceLocation itemRL = new ResourceLocation(itemId);
        var item = ForgeRegistries.ITEMS.getValue(itemRL);
        if (item == null || item == Items.AIR) {
            LOGGER.warn("ArthanaDropRecipeLoader: Unknown item {} in {} ({})", itemId, jsonPath, source);
            return;
        }

        // Extract namespace and entity name from path
        Path relativePath = dataRoot.relativize(jsonPath);
        String relStr = relativePath.toString().replace('\\', '/');

        // Parse: namespace/arthana/entity_name.json
        int firstSlash = relStr.indexOf('/');
        int arthanaStart = relStr.indexOf("/arthana/");
        if (firstSlash < 0 || arthanaStart < 0) {
            LOGGER.warn("ArthanaDropRecipeLoader: Unexpected path structure: {} ({})", relStr, source);
            return;
        }

        String namespace = relStr.substring(0, firstSlash);
        String entityPath = relStr.substring(arthanaStart + "/arthana/".length(),
                relStr.length() - ".json".length());
        int lastSlash = entityPath.lastIndexOf('/');
        String entityName = lastSlash >= 0 ? entityPath.substring(lastSlash + 1) : entityPath;

        ResourceLocation entityId = new ResourceLocation(namespace, entityName);

        // Skip duplicates (later sources override earlier ones)
        String dedupeKey = entityId.toString();
        if (loadedIds.contains(dedupeKey)) {
            LOGGER.debug("ArthanaDropRecipeLoader: Skipping duplicate {} from {}", entityId, source);
            return;
        }

        // Look up entity type
        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entityId);
        if (entityType == null) {
            LOGGER.warn("ArthanaDropRecipeLoader: Unknown entity {} from {} ({})", entityId, jsonPath, source);
            return;
        }

        loadedIds.add(dedupeKey);
        recipes.add(new ArthanaDropRecipe(entityType, new ItemStack(item, count)));
        LOGGER.info("ArthanaDropRecipeLoader: Loaded {} -> {} x{} (from {})",
                entityId, itemId, count, source);
    }
}
