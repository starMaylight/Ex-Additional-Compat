package com.starmaylight.ex_additional_compat.capability.altar;

import com.favouriteless.enchanted.api.power.IPowerProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.multiblocked.api.capability.trait.SingleCapabilityTrait;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capability trait for Enchanted Altar Power in Multiblocked machines.
 * Provides internal power buffer and auto-imports from nearby IPowerProvider (altar) blocks.
 *
 * Architecture:
 * - Internal power buffer stores altar power for recipe processing
 * - update() scans nearby blocks for IPowerProvider (Enchanted altars) and imports power
 * - Static registry allows AltarPowerCapabilityProxy to find this trait
 *
 * Note: IPowerProvider only has tryConsumePower(double) - no query method.
 * We periodically try to consume power from nearby altars to fill our buffer.
 */
public class AltarPowerCapabilityTrait extends SingleCapabilityTrait {

    // Static registry for Proxy -> Trait access
    private static final Map<BlockPos, AltarPowerCapabilityTrait> TRAIT_REGISTRY = new ConcurrentHashMap<>();

    private double currentPower = 0;
    private double maxPower = 5000.0;
    private int scanRadius = 8; // Search radius for nearby altars

    public AltarPowerCapabilityTrait() {
        super(AltarPowerMultiblockCapability.CAP);
    }

    // --- Power buffer access (for Proxy) ---

    public double getCurrentPower() { return currentPower; }
    public double getMaxPower() { return maxPower; }

    /**
     * Try to consume power from the internal buffer.
     * @return true if successful, false if not enough power
     */
    public boolean tryConsumePower(double amount) {
        if (currentPower >= amount) {
            currentPower -= amount;
            return true;
        }
        return false;
    }

    /**
     * Add power to the internal buffer (from altar import).
     */
    public void addPower(double amount) {
        currentPower = Math.min(maxPower, currentPower + amount);
    }

    // --- Serialization ---

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            if (json.has("maxPower")) {
                maxPower = json.get("maxPower").getAsDouble();
            }
            if (json.has("scanRadius")) {
                scanRadius = json.get("scanRadius").getAsInt();
            }
        }
    }

    @Override
    public JsonElement deserialize() {
        JsonElement base = super.deserialize();
        if (base != null && base.isJsonObject()) {
            JsonObject json = base.getAsJsonObject();
            json.addProperty("maxPower", maxPower);
            json.addProperty("scanRadius", scanRadius);
        }
        return base;
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        currentPower = compound.getDouble("altar_power");
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.writeToNBT(compound);
        compound.putDouble("altar_power", currentPower);
    }

    // --- Auto-import from nearby IPowerProvider (altar) blocks ---

    @Override
    public void update() {
        super.update();
        if (component == null || component.getLevel() == null || component.getLevel().isClientSide()) return;
        if (currentPower >= maxPower) return; // Buffer full

        // Only scan every 20 ticks (1 second) to reduce overhead
        if (component.getLevel().getGameTime() % 20 != 0) return;

        double needed = maxPower - currentPower;
        if (needed <= 0) return;

        // Try to consume power from nearby altars
        BlockPos center = component.getBlockPos();
        double importAmount = Math.min(needed, 500.0); // Import up to 500 per scan

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-scanRadius, -scanRadius, -scanRadius),
                center.offset(scanRadius, scanRadius, scanRadius))) {
            BlockEntity be = component.getLevel().getBlockEntity(pos);
            if (be instanceof IPowerProvider provider) {
                if (provider.tryConsumePower(importAmount)) {
                    currentPower += importAmount;
                    break; // Successfully imported from one altar
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
     * Find the AltarPowerCapabilityTrait for a given BlockEntity (at its position or adjacent).
     */
    public static AltarPowerCapabilityTrait getTraitFor(BlockEntity be) {
        if (be == null) return null;
        BlockPos pos = be.getBlockPos();

        AltarPowerCapabilityTrait direct = TRAIT_REGISTRY.get(pos);
        if (direct != null) return direct;

        for (Map.Entry<BlockPos, AltarPowerCapabilityTrait> entry : TRAIT_REGISTRY.entrySet()) {
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
