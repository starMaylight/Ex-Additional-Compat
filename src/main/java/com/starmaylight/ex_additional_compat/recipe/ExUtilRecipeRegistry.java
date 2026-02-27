package com.starmaylight.ex_additional_compat.recipe;

import com.mojang.logging.LogUtils;
import com.starmaylight.ex_additional_compat.jei.exutil.EnchanterRecipe;
import com.starmaylight.ex_additional_compat.jei.exutil.ResonatorRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Static registry of Extra Utilities Reborn Enchanter and Resonator recipes.
 * These recipes are hardcoded in the original mod's procedure classes,
 * so we mirror them here for JEI display and Mixin-based modification.
 *
 * Shared between:
 * - JEI plugin (Feature 10: recipe display)
 * - Mixin hooks (Feature 11: recipe interception)
 * - KubeJS bindings (recipe addition/removal from scripts)
 *
 * IMPORTANT: Custom recipes added via addEnchanterRecipeById/addResonatorRecipeById
 * before init() are queued in PENDING lists and appended AFTER default recipes during
 * init(). This ensures defaultEnchanterCount/defaultResonatorCount only counts actual
 * default recipes, so isDefaultEnchanterRecipe/isDefaultResonatorRecipe works correctly.
 */
public final class ExUtilRecipeRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<EnchanterRecipe> ENCHANTER_RECIPES = new ArrayList<>();
    private static final List<ResonatorRecipe> RESONATOR_RECIPES = new ArrayList<>();

    /** Pending custom recipes added before init() — appended after defaults in init(). */
    private static final List<EnchanterRecipe> PENDING_ENCHANTER = new ArrayList<>();
    private static final List<ResonatorRecipe> PENDING_RESONATOR = new ArrayList<>();

    /** Tracks the number of default recipes added during init, so custom recipes can be distinguished. */
    private static int defaultEnchanterCount = 0;
    private static int defaultResonatorCount = 0;

    private static boolean initialized = false;

    private ExUtilRecipeRegistry() {}

    /**
     * Initialize the recipe registry with all default hardcoded recipes.
     * Safe to call multiple times; only initializes once.
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        LOGGER.info("ExUtilRecipeRegistry: Initializing hardcoded recipes...");
        initEnchanterRecipes();
        initResonatorRecipes();
        defaultEnchanterCount = ENCHANTER_RECIPES.size();
        defaultResonatorCount = RESONATOR_RECIPES.size();
        LOGGER.info("ExUtilRecipeRegistry: {} default Enchanter, {} default Resonator recipes.",
                defaultEnchanterCount, defaultResonatorCount);

        // Speed upgrade conversion recipes — NOT in the original mod's bytecode.
        // The original Resonator only references speed upgrades as speed bonus items in slot 2.
        // These are added as CUSTOM recipes so the Mixin processes them.
        addResonatorInternal("extrautilitiesrebirth:speed_upgrade", 1,
                "extrautilitiesrebirth:enchanted_speed_upgrade", 1, 100, 16000);
        addResonatorInternal("extrautilitiesrebirth:enchanted_speed_upgrade", 1,
                "extrautilitiesrebirth:super_speed_upgrade", 1, 100, 64000);

        // Flush any custom recipes that were added before init() via KubeJS etc.
        if (!PENDING_ENCHANTER.isEmpty()) {
            LOGGER.info("ExUtilRecipeRegistry: Appending {} pending custom Enchanter recipes.",
                    PENDING_ENCHANTER.size());
            ENCHANTER_RECIPES.addAll(PENDING_ENCHANTER);
            PENDING_ENCHANTER.clear();
        }
        if (!PENDING_RESONATOR.isEmpty()) {
            LOGGER.info("ExUtilRecipeRegistry: Appending {} pending custom Resonator recipes.",
                    PENDING_RESONATOR.size());
            RESONATOR_RECIPES.addAll(PENDING_RESONATOR);
            PENDING_RESONATOR.clear();
        }

        LOGGER.info("ExUtilRecipeRegistry: Total {} Enchanter, {} Resonator recipes (incl. custom).",
                ENCHANTER_RECIPES.size(), RESONATOR_RECIPES.size());
    }

    /**
     * Enchanter recipes (6 total, from EnchanterUpdateTickProcedure).
     * Format: inputMain + inputCatalyst -> output (ticks, totalFE)
     */
    private static void initEnchanterRecipes() {
        // Counts from original bytecode analysis (EnchanterUpdateTickProcedure):
        // Format: mainId, mainCount, catalystId, catalystCount, outputId, outputCount, ticks, totalFE

        // 1. Bookshelf ×1 + Lapis ×1 -> Magical Wood ×1 (1600 ticks = 80s, 64000 FE)
        addEnchanterInternal("minecraft:bookshelf", 1, "minecraft:lapis_lazuli", 1,
                "extrautilitiesrebirth:magical_wood", 1, 1600, 64000);

        // 2. Gold Ingot ×1 + Lapis ×1 -> Enchanted Ingot ×1 (200 ticks = 10s, 8000 FE)
        addEnchanterInternal("minecraft:gold_ingot", 1, "minecraft:lapis_lazuli", 1,
                "extrautilitiesrebirth:enchanted_ingot", 1, 200, 8000);

        // 3. Gold Block ×1 + Lapis ×9 -> Block of Enchaned Metal ×1 (600 ticks = 30s, 24000 FE)
        // Note: "enchaned" is a typo in the original mod, not "enchanted"
        // Bytecode: dvar_14(main)=1.0, dvar_12(catalyst)=9.0
        addEnchanterInternal("minecraft:gold_block", 1, "minecraft:lapis_lazuli", 9,
                "extrautilitiesrebirth:block_of_enchaned_metal", 1, 600, 24000);

        // 4. Iron Block ×8 + Nether Star ×9 -> Block of Evil Infused Iron ×8 (4800 ticks = 240s, 192000 FE)
        // Bytecode: dvar_14(main)=8.0, dvar_12(catalyst)=9.0
        addEnchanterInternal("minecraft:iron_block", 8, "minecraft:nether_star", 9,
                "extrautilitiesrebirth:block_of_evil_infused_iron", 8, 4800, 192000);

        // 5. Apple ×16 + Lapis ×4 -> Magical Apple ×16 (400 ticks = 20s, 16000 FE)
        // Bytecode: dvar_14(main)=16.0, dvar_12(catalyst)=4.0
        addEnchanterInternal("minecraft:apple", 16, "minecraft:lapis_lazuli", 4,
                "extrautilitiesrebirth:magical_apple", 16, 400, 16000);

        // 6. Iron Ingot ×8 + Nether Star ×1 -> Evil Infused Iron Ingot ×8 (1600 ticks = 80s, 24000 FE)
        // Bytecode: dvar_14(main)=8.0, dvar_12(catalyst)=1.0
        addEnchanterInternal("minecraft:iron_ingot", 8, "minecraft:nether_star", 1,
                "extrautilitiesrebirth:evil_infused_iron_ingot", 8, 1600, 24000);
    }

    /**
     * Resonator recipes (7 default, from ResonatorUpdateTickProcedure bytecode).
     * Format: input -> output (ticks, totalFE)
     * All recipes use 100 ticks (5 seconds) processing time.
     *
     * NOTE: Speed upgrade conversion recipes (speed_upgrade → enchanted_speed_upgrade,
     * enchanted_speed_upgrade → super_speed_upgrade) are NOT default recipes.
     * The original bytecode only references speed upgrades as SPEED BONUS items in slot 2,
     * not as conversion recipes. These are added as custom recipes after defaultResonatorCount
     * is set, so the Mixin handles them.
     */
    private static void initResonatorRecipes() {
        // All Resonator recipes consume 1 input and produce 1 output (from bytecode analysis).
        // Format: inputId, inputCount, outputId, outputCount, ticks, totalFE

        // 1. Polished Stone ×1 -> Stoneburnt ×1 (100 ticks, 8000 FE)
        addResonatorInternal("extrautilitiesrebirth:polished_stone", 1,
                "extrautilitiesrebirth:stoneburnt", 1, 100, 8000);

        // 2. Smooth Quartz ×1 -> Quartzburnt ×1 (100 ticks, 8000 FE)
        addResonatorInternal("minecraft:smooth_quartz", 1,
                "extrautilitiesrebirth:quartzburnt", 1, 100, 8000);

        // 3. Stoneburnt ×1 -> Rainbow Stone ×1 (100 ticks, 64000 FE)
        addResonatorInternal("extrautilitiesrebirth:stoneburnt", 1,
                "extrautilitiesrebirth:rainbow_stone", 1, 100, 64000);

        // 4. Lapis Lazuli ×1 -> Lunar Reactive Dust ×1 (100 ticks, 16000 FE)
        addResonatorInternal("minecraft:lapis_lazuli", 1,
                "extrautilitiesrebirth:lunar_reactive_dust", 1, 100, 16000);

        // 5. Coal ×1 -> Red Coal ×1 (100 ticks, 16000 FE)
        addResonatorInternal("minecraft:coal", 1,
                "extrautilitiesrebirth:red_coal", 1, 100, 16000);

        // 6. Light Weighted Pressure Plate ×1 -> Upgrade Base ×1 (100 ticks, 8000 FE)
        addResonatorInternal("minecraft:light_weighted_pressure_plate", 1,
                "extrautilitiesrebirth:upgrade_base", 1, 100, 8000);

        // 7. Iron Bars ×1 -> Wireless RF Heating Coil ×1 (100 ticks, 16000 FE)
        addResonatorInternal("minecraft:iron_bars", 1,
                "extrautilitiesrebirth:wireless_rf_heating_coil", 1, 100, 16000);
    }

    private static void addEnchanterInternal(String mainId, int mainCount,
                                             String catalystId, int catalystCount,
                                             String outputId, int outputCount,
                                             int ticks, int totalFE) {
        ItemStack main = itemFromId(mainId, mainCount);
        ItemStack catalyst = itemFromId(catalystId, catalystCount);
        ItemStack output = itemFromId(outputId, outputCount);
        if (!main.isEmpty() && !catalyst.isEmpty() && !output.isEmpty()) {
            ENCHANTER_RECIPES.add(new EnchanterRecipe(main, catalyst, output, ticks, totalFE));
        } else {
            LOGGER.warn("ExUtilRecipeRegistry: Skipping Enchanter recipe {} + {} -> {} (item not found)",
                    mainId, catalystId, outputId);
        }
    }

    private static void addResonatorInternal(String inputId, int inputCount,
                                             String outputId, int outputCount,
                                             int ticks, int totalFE) {
        ItemStack input = itemFromId(inputId, inputCount);
        ItemStack output = itemFromId(outputId, outputCount);
        if (!input.isEmpty() && !output.isEmpty()) {
            RESONATOR_RECIPES.add(new ResonatorRecipe(input, output, ticks, totalFE));
        } else {
            LOGGER.warn("ExUtilRecipeRegistry: Skipping Resonator recipe {} -> {} (item not found)",
                    inputId, outputId);
        }
    }

    private static ItemStack itemFromId(String id, int count) {
        ResourceLocation rl = new ResourceLocation(id);
        var item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            // Try blocks (some ExUtil items are block items)
            var block = ForgeRegistries.BLOCKS.getValue(rl);
            if (block != null && block != net.minecraft.world.level.block.Blocks.AIR) {
                return new ItemStack(block.asItem(), count);
            }
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, count);
    }

    // ===== Public API =====

    /**
     * Get all registered Enchanter recipes (unmodifiable view).
     */
    public static List<EnchanterRecipe> getEnchanterRecipes() {
        return Collections.unmodifiableList(ENCHANTER_RECIPES);
    }

    /**
     * Get all registered Resonator recipes (unmodifiable view).
     */
    public static List<ResonatorRecipe> getResonatorRecipes() {
        return Collections.unmodifiableList(RESONATOR_RECIPES);
    }

    /**
     * Add a new Enchanter recipe (for KubeJS/Mixin use).
     * If init() hasn't been called yet, queues the recipe for later.
     */
    public static void addEnchanterRecipe(EnchanterRecipe recipe) {
        if (initialized) {
            ENCHANTER_RECIPES.add(recipe);
        } else {
            PENDING_ENCHANTER.add(recipe);
        }
        LOGGER.info("ExUtilRecipeRegistry: Added custom Enchanter recipe -> {} (pending={})",
                recipe.getOutput().getDisplayName().getString(), !initialized);
    }

    /**
     * Add a new Resonator recipe (for KubeJS/Mixin use).
     * If init() hasn't been called yet, queues the recipe for later.
     */
    public static void addResonatorRecipe(ResonatorRecipe recipe) {
        if (initialized) {
            RESONATOR_RECIPES.add(recipe);
        } else {
            PENDING_RESONATOR.add(recipe);
        }
        LOGGER.info("ExUtilRecipeRegistry: Added custom Resonator recipe -> {} (pending={})",
                recipe.getOutput().getDisplayName().getString(), !initialized);
    }

    /**
     * Add a new Enchanter recipe by item IDs (for KubeJS string-based use).
     * @param mainId main input item ID (e.g. "minecraft:diamond")
     * @param mainCount main input count
     * @param catalystId catalyst item ID
     * @param catalystCount catalyst count
     * @param outputId output item ID
     * @param outputCount output count
     * @param ticks processing time in ticks
     * @param totalFE total FE energy cost
     */
    public static void addEnchanterRecipeById(String mainId, int mainCount,
                                               String catalystId, int catalystCount,
                                               String outputId, int outputCount,
                                               int ticks, int totalFE) {
        ItemStack main = itemFromId(mainId, mainCount);
        ItemStack catalyst = itemFromId(catalystId, catalystCount);
        ItemStack output = itemFromId(outputId, outputCount);
        if (!main.isEmpty() && !catalyst.isEmpty() && !output.isEmpty()) {
            EnchanterRecipe recipe = new EnchanterRecipe(main, catalyst, output, ticks, totalFE);
            if (initialized) {
                ENCHANTER_RECIPES.add(recipe);
            } else {
                PENDING_ENCHANTER.add(recipe);
            }
            LOGGER.info("ExUtilRecipeRegistry: Added custom Enchanter recipe via IDs: {} + {} -> {} (pending={})",
                    mainId, catalystId, outputId, !initialized);
        } else {
            LOGGER.warn("ExUtilRecipeRegistry: Failed to add Enchanter recipe {} + {} -> {} (item not found)",
                    mainId, catalystId, outputId);
        }
    }

    /**
     * Add a new Resonator recipe by item IDs (for KubeJS string-based use).
     * @param inputId input item ID
     * @param inputCount input count
     * @param outputId output item ID
     * @param outputCount output count
     * @param ticks processing time in ticks
     * @param totalFE total FE energy cost
     */
    public static void addResonatorRecipeById(String inputId, int inputCount,
                                               String outputId, int outputCount,
                                               int ticks, int totalFE) {
        ItemStack input = itemFromId(inputId, inputCount);
        ItemStack output = itemFromId(outputId, outputCount);
        if (!input.isEmpty() && !output.isEmpty()) {
            ResonatorRecipe recipe = new ResonatorRecipe(input, output, ticks, totalFE);
            if (initialized) {
                RESONATOR_RECIPES.add(recipe);
            } else {
                PENDING_RESONATOR.add(recipe);
            }
            LOGGER.info("ExUtilRecipeRegistry: Added custom Resonator recipe via IDs: {} -> {} (pending={})",
                    inputId, outputId, !initialized);
        } else {
            LOGGER.warn("ExUtilRecipeRegistry: Failed to add Resonator recipe {} -> {} (item not found)",
                    inputId, outputId);
        }
    }

    /**
     * Remove an Enchanter recipe by output item ID.
     * @return true if a recipe was removed
     */
    public static boolean removeEnchanterRecipe(String outputId) {
        ResourceLocation rl = new ResourceLocation(outputId);
        Iterator<EnchanterRecipe> it = ENCHANTER_RECIPES.iterator();
        while (it.hasNext()) {
            EnchanterRecipe r = it.next();
            if (rl.equals(ForgeRegistries.ITEMS.getKey(r.getOutput().getItem()))) {
                it.remove();
                LOGGER.info("ExUtilRecipeRegistry: Removed Enchanter recipe -> {}", outputId);
                return true;
            }
        }
        return false;
    }

    /**
     * Remove a Resonator recipe by output item ID.
     * @return true if a recipe was removed
     */
    public static boolean removeResonatorRecipe(String outputId) {
        ResourceLocation rl = new ResourceLocation(outputId);
        Iterator<ResonatorRecipe> it = RESONATOR_RECIPES.iterator();
        while (it.hasNext()) {
            ResonatorRecipe r = it.next();
            if (rl.equals(ForgeRegistries.ITEMS.getKey(r.getOutput().getItem()))) {
                it.remove();
                LOGGER.info("ExUtilRecipeRegistry: Removed Resonator recipe -> {}", outputId);
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a recipe is a default (hardcoded in original mod) Enchanter recipe.
     * Default recipes should be handled by the original execute() method.
     */
    public static boolean isDefaultEnchanterRecipe(EnchanterRecipe recipe) {
        int idx = ENCHANTER_RECIPES.indexOf(recipe);
        return idx >= 0 && idx < defaultEnchanterCount;
    }

    /**
     * Check if a recipe is a default (hardcoded in original mod) Resonator recipe.
     * Default recipes should be handled by the original execute() method.
     */
    public static boolean isDefaultResonatorRecipe(ResonatorRecipe recipe) {
        int idx = RESONATOR_RECIPES.indexOf(recipe);
        return idx >= 0 && idx < defaultResonatorCount;
    }

    /**
     * Check if an ItemStack is a speed upgrade item (any tier).
     * Used by Mixin hooks for speed upgrade detection.
     */
    public static boolean isSpeedUpgrade(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (rl == null) return false;
        String path = rl.getPath();
        return path.equals("speed_upgrade")
                || path.equals("enchanted_speed_upgrade")
                || path.equals("super_speed_upgrade");
    }

    /**
     * Find an Enchanter recipe matching the given inputs.
     * Used by MixinEnchanterUpdateTick to check custom recipes.
     *
     * @param main The main input ItemStack
     * @param catalyst The catalyst input ItemStack
     * @return The matching recipe, or null if none found
     */
    public static EnchanterRecipe findEnchanterRecipe(ItemStack main, ItemStack catalyst) {
        for (EnchanterRecipe r : ENCHANTER_RECIPES) {
            if (main.getItem() == r.getInputMain().getItem()
                    && catalyst.getItem() == r.getInputCatalyst().getItem()) {
                return r;
            }
        }
        return null;
    }

    /**
     * Find a Resonator recipe matching the given input.
     * Used by MixinResonatorUpdateTick to check custom recipes.
     *
     * @param input The input ItemStack
     * @return The matching recipe, or null if none found
     */
    public static ResonatorRecipe findResonatorRecipe(ItemStack input) {
        for (ResonatorRecipe r : RESONATOR_RECIPES) {
            if (input.getItem() == r.getInput().getItem()) {
                return r;
            }
        }
        return null;
    }
}
