package com.starmaylight.ex_additional_compat.capability.chaos;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.multiblocked.api.capability.trait.SingleCapabilityTrait;
import net.foxmcloud.draconicadditions.blocks.tileentity.TileChaosHolderBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capability trait for Draconic Additions Chaos in Multiblocked machines.
 * Provides internal chaos buffer and auto-imports from adjacent TileChaosHolderBase blocks.
 *
 * Architecture:
 * - Internal buffer stores chaos for recipe processing
 * - update() auto-imports chaos from adjacent DraconicAdditions chaos holders
 * - Static registry allows ChaosCapabilityProxy to find this trait
 *
 * Note: TileChaosHolderBase is a BlockEntity class, cannot extend it.
 * Using internal storage + auto-import pattern instead.
 */
public class ChaosCapabilityTrait extends SingleCapabilityTrait {

    // Static registry for Proxy -> Trait access
    private static final Map<BlockPos, ChaosCapabilityTrait> TRAIT_REGISTRY = new ConcurrentHashMap<>();

    private int chaos = 0;
    private int maxChaos = 10000;

    public ChaosCapabilityTrait() {
        super(ChaosMultiblockCapability.CAP);
    }

    // --- Chaos buffer access (for Proxy) ---

    public int getChaos() { return chaos; }
    public int getMaxChaos() { return maxChaos; }

    public void addChaos(int amount) {
        chaos = Math.max(0, Math.min(maxChaos, chaos + amount));
    }

    public void subtractChaos(int amount) {
        chaos = Math.max(0, chaos - amount);
    }

    public void setChaos(int amount) {
        chaos = Math.max(0, Math.min(maxChaos, amount));
    }

    // --- Serialization ---

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            if (json.has("maxChaos")) {
                maxChaos = json.get("maxChaos").getAsInt();
            }
        }
    }

    @Override
    public JsonElement deserialize() {
        JsonElement base = super.deserialize();
        if (base != null && base.isJsonObject()) {
            base.getAsJsonObject().addProperty("maxChaos", maxChaos);
        }
        return base;
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        chaos = compound.getInt("chaos_amount");
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.writeToNBT(compound);
        compound.putInt("chaos_amount", chaos);
    }

    // --- Auto-import from adjacent TileChaosHolderBase blocks ---

    @Override
    public void update() {
        super.update();
        if (component == null || component.getLevel() == null || component.getLevel().isClientSide()) return;
        if (chaos >= maxChaos) return; // Buffer full

        BlockPos pos = component.getBlockPos();
        for (Direction dir : Direction.values()) {
            BlockEntity adjacent = component.getLevel().getBlockEntity(pos.relative(dir));
            if (adjacent instanceof TileChaosHolderBase chaosHolder) {
                int available = chaosHolder.chaos.get();
                int space = maxChaos - chaos;
                int transfer = Math.min(available, space);
                if (transfer > 0) {
                    chaosHolder.chaos.subtract(transfer);
                    chaos += transfer;
                    if (chaos >= maxChaos) break;
                }
            }
        }
    }

    // --- Static Registry ---

    @Override
    public void onLoad() {
        super.onLoad();
        if (component != null && component.getBlockPos() != null) {
            TRAIT_REGISTRY.put(component.getBlockPos(), this);
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        unregister();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        unregister();
    }

    private void unregister() {
        if (component != null && component.getBlockPos() != null) {
            TRAIT_REGISTRY.remove(component.getBlockPos());
        }
    }

    /**
     * Find the ChaosCapabilityTrait for a given BlockEntity (at its position or adjacent).
     */
    public static ChaosCapabilityTrait getTraitFor(BlockEntity be) {
        if (be == null) return null;
        BlockPos pos = be.getBlockPos();

        ChaosCapabilityTrait direct = TRAIT_REGISTRY.get(pos);
        if (direct != null) return direct;

        for (Map.Entry<BlockPos, ChaosCapabilityTrait> entry : TRAIT_REGISTRY.entrySet()) {
            BlockPos traitPos = entry.getKey();
            int dist = Math.abs(traitPos.getX() - pos.getX())
                     + Math.abs(traitPos.getY() - pos.getY())
                     + Math.abs(traitPos.getZ() - pos.getZ());
            if (dist <= 1) {
                return entry.getValue();
            }
        }
        return null;
    }
}
