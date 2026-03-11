package com.starmaylight.ex_additional_compat.kubejs.enchanted;

import com.favouriteless.enchanted.api.rites.AbstractRite;
import com.favouriteless.enchanted.common.rites.RiteType;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Forge Event-based handler for activating ExCompat custom Enchanted rites.
 *
 * Replaces the previous MixinRiteTypes approach (which injected into the
 * internal RiteTypes.getRiteAt() method) with a pure event-driven system
 * that only uses Enchanted's public API (AbstractRite).
 *
 * Flow:
 * 1. Player right-clicks Gold Chalk block
 * 2. This handler intercepts via PlayerInteractEvent.RightClickBlock (HIGH priority)
 * 3. If an ExCompat rite is already active at that position → stop it
 * 4. Otherwise, iterate ExCompat-registered rites and find best match
 * 5. If match found → activate rite, cancel event (Enchanted won't process)
 * 6. If no match → let event pass through (Enchanted handles normally)
 *
 * Rite lifecycle is managed here via TickEvent.ServerTickEvent,
 * calling AbstractRite.tick() (API) each server tick.
 * This avoids depending on Enchanted's internal RiteManager.
 */
@Mod.EventBusSubscriber(modid = "ex_additional_compat", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RiteActivationHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Gold Chalk block registry name — used to detect clicks without importing internal classes */
    private static final ResourceLocation GOLD_CHALK_ID = new ResourceLocation("enchanted", "chalk_gold");

    /** Active ExCompat rites tracked by chalk position, for stop-detection and ticking */
    private static final Map<BlockPos, AbstractRite> ACTIVE_RITES = new ConcurrentHashMap<>();

    private RiteActivationHandler() {}

    // --- Gold Chalk click interception ---

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getWorld();
        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();
        Block block = level.getBlockState(pos).getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        if (!GOLD_CHALK_ID.equals(blockId)) return;

        // Check if an ExCompat rite is already active at this position
        AbstractRite activeRite = ACTIVE_RITES.get(pos);
        if (activeRite != null) {
            if (!activeRite.isRemoved) {
                // Re-click on active rite → stop it
                LOGGER.info("[ExCompat Rite] Stopping active rite at {}", pos);
                activeRite.stopExecuting();
                ACTIVE_RITES.remove(pos);
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
            // Stale entry (rite already finished) → clean up
            ACTIVE_RITES.remove(pos);
        }

        if (!(level instanceof ServerLevel serverLevel)) return;
        Player player = event.getPlayer();

        // Find best matching ExCompat custom rite at this position
        List<RiteType<?>> customRites = EnchantedRiteRegistry.getRegisteredRites();
        if (customRites.isEmpty()) return;

        AbstractRite bestRite = null;
        int bestDiff = Integer.MAX_VALUE;

        for (RiteType<?> type : customRites) {
            try {
                AbstractRite candidate = type.create(serverLevel, pos, player.getUUID());
                int diff = candidate.differenceAt(level, pos);
                if (diff != -1 && diff < bestDiff) {
                    bestRite = candidate;
                    bestDiff = diff;
                }
            } catch (Exception e) {
                LOGGER.debug("[ExCompat Rite] Failed to evaluate rite type: {}", e.getMessage());
            }
        }

        if (bestRite == null) {
            // No ExCompat rite matches → let Enchanted handle the click
            return;
        }

        // Activate the best matching rite
        LOGGER.info("[ExCompat Rite] Activating custom rite at {} (diff={})", pos, bestDiff);
        bestRite.start();
        ACTIVE_RITES.put(pos, bestRite);

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    // --- Rite lifecycle: tick active rites every server tick ---

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.side != LogicalSide.SERVER) return;
        if (ACTIVE_RITES.isEmpty()) return;

        Iterator<Map.Entry<BlockPos, AbstractRite>> it = ACTIVE_RITES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, AbstractRite> entry = it.next();
            AbstractRite rite = entry.getValue();

            if (rite.isRemoved) {
                it.remove();
                continue;
            }

            try {
                rite.tick();
            } catch (Exception e) {
                LOGGER.error("[ExCompat Rite] Error ticking rite at {}, stopping", entry.getKey(), e);
                try { rite.stopExecuting(); } catch (Exception ignored) {}
                it.remove();
            }

            // Check again after tick (rite might have self-terminated)
            if (rite.isRemoved) {
                it.remove();
            }
        }
    }

    // --- Cleanup on server shutdown ---

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (!ACTIVE_RITES.isEmpty()) {
            LOGGER.info("[ExCompat Rite] Server stopping, clearing {} active rites", ACTIVE_RITES.size());
            for (AbstractRite rite : ACTIVE_RITES.values()) {
                try {
                    if (!rite.isRemoved) rite.stopExecuting();
                } catch (Exception ignored) {}
            }
            ACTIVE_RITES.clear();
        }
    }
}
