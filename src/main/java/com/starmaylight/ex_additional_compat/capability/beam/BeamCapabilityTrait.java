package com.starmaylight.ex_additional_compat.capability.beam;

import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.beams.BeamUnit;
import com.Da_Technomancer.crossroads.API.beams.IBeamHandler;
import com.lowdragmc.multiblocked.api.capability.trait.SingleCapabilityTrait;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capability trait for Crossroads Beam in Multiblocked machines.
 * Receives beams from external Crossroads beam emitters via IBeamHandler capability.
 * Stores the last received beam for recipe processing.
 *
 * Architecture:
 * 1. External beam emitter hits the multiblock component
 * 2. Crossroads calls setBeam() on this trait's IBeamHandler
 * 3. Trait stores the received beam
 * 4. BeamCapabilityProxy reads stored beam during recipe processing
 */
public class BeamCapabilityTrait extends SingleCapabilityTrait {

    // Static registry for Proxy -> Trait access
    private static final Map<BlockPos, BeamCapabilityTrait> TRAIT_REGISTRY = new ConcurrentHashMap<>();

    // Last beam received from external source (per-tick transient)
    private BeamUnit lastReceivedBeam = BeamUnit.EMPTY;

    private final LazyOptional<IBeamHandler> beamHandlerOpt = LazyOptional.of(() -> new IBeamHandler() {
        @Override
        public void setBeam(@Nonnull BeamUnit beam) {
            lastReceivedBeam = beam != null ? beam : BeamUnit.EMPTY;
        }
    });

    public BeamCapabilityTrait() {
        super(BeamMultiblockCapability.CAP);
    }

    /**
     * Get the last beam received from external sources.
     * Called by BeamCapabilityProxy during recipe processing.
     */
    public BeamUnit getLastReceivedBeam() {
        return lastReceivedBeam;
    }

    /**
     * Clear the stored beam (called after recipe tick to reset transient state).
     */
    public void clearBeam() {
        lastReceivedBeam = BeamUnit.EMPTY;
    }

    // --- Static Registry (like LPCapabilityTrait) ---

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
        beamHandlerOpt.invalidate();
        unregister();
    }

    private void unregister() {
        if (component != null && component.getBlockPos() != null) {
            TRAIT_REGISTRY.remove(component.getBlockPos());
        }
    }

    /**
     * Find the BeamCapabilityTrait for a given BlockEntity (at its position or adjacent).
     */
    public static BeamCapabilityTrait getTraitFor(BlockEntity be) {
        if (be == null) return null;
        BlockPos pos = be.getBlockPos();

        // Direct position match
        BeamCapabilityTrait direct = TRAIT_REGISTRY.get(pos);
        if (direct != null) return direct;

        // Check adjacent positions (manhattan distance <= 1)
        for (Map.Entry<BlockPos, BeamCapabilityTrait> entry : TRAIT_REGISTRY.entrySet()) {
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

    // --- Forge Capability exposure (like HeatCapabilityTrait) ---

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capability == Capabilities.BEAM_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == Capabilities.BEAM_CAPABILITY) return beamHandlerOpt.cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        // Beam is transient, no persistence needed
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.writeToNBT(compound);
    }
}
