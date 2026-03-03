package com.starmaylight.ex_additional_compat.capability.flux;

import com.Da_Technomancer.crossroads.API.technomancy.IFluxLink;
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
 * Capability trait for Crossroads Flux (Temporal Entropy) in Multiblocked machines.
 * Provides internal flux buffer and auto-imports from adjacent IFluxLink blocks.
 *
 * Architecture:
 * - Internal buffer stores flux for recipe processing
 * - update() auto-imports flux from adjacent Crossroads flux blocks (IFluxLink)
 * - Static registry allows FluxCapabilityProxy to find this trait
 *
 * Note: IFluxLink is NOT a Forge capability and extends 4 complex interfaces,
 * so we cannot implement it directly. Instead, we use internal storage + auto-import.
 */
public class FluxCapabilityTrait extends SingleCapabilityTrait {

    // Static registry for Proxy -> Trait access
    private static final Map<BlockPos, FluxCapabilityTrait> TRAIT_REGISTRY = new ConcurrentHashMap<>();

    private int flux = 0;
    private int maxFlux = 64;

    public FluxCapabilityTrait() {
        super(FluxMultiblockCapability.CAP);
    }

    // --- Flux buffer access (for Proxy) ---

    public int getFlux() { return flux; }
    public int getMaxFlux() { return maxFlux; }

    public void addFlux(int amount) {
        flux = Math.max(0, Math.min(maxFlux, flux + amount));
    }

    public void setFlux(int amount) {
        flux = Math.max(0, Math.min(maxFlux, amount));
    }

    // --- Serialization ---

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            if (json.has("maxFlux")) {
                maxFlux = json.get("maxFlux").getAsInt();
            }
        }
    }

    @Override
    public JsonElement deserialize() {
        JsonElement base = super.deserialize();
        if (base != null && base.isJsonObject()) {
            base.getAsJsonObject().addProperty("maxFlux", maxFlux);
        }
        return base;
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        flux = compound.getInt("flux_amount");
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.writeToNBT(compound);
        compound.putInt("flux_amount", flux);
    }

    // --- Auto-import from adjacent IFluxLink blocks ---

    @Override
    public void update() {
        super.update();
        if (component == null || component.getLevel() == null || component.getLevel().isClientSide()) return;
        if (flux >= maxFlux) return; // Buffer full

        BlockPos pos = component.getBlockPos();
        for (Direction dir : Direction.values()) {
            BlockEntity adjacent = component.getLevel().getBlockEntity(pos.relative(dir));
            if (adjacent instanceof IFluxLink fluxLink) {
                int available = fluxLink.getFlux();
                int space = maxFlux - flux;
                int transfer = Math.min(available, space);
                if (transfer > 0) {
                    fluxLink.addFlux(-transfer);
                    flux += transfer;
                    if (flux >= maxFlux) break;
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
     * Find the FluxCapabilityTrait for a given BlockEntity (at its position or adjacent).
     */
    public static FluxCapabilityTrait getTraitFor(BlockEntity be) {
        if (be == null) return null;
        BlockPos pos = be.getBlockPos();

        FluxCapabilityTrait direct = TRAIT_REGISTRY.get(pos);
        if (direct != null) return direct;

        for (Map.Entry<BlockPos, FluxCapabilityTrait> entry : TRAIT_REGISTRY.entrySet()) {
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
