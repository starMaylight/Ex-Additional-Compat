package com.starmaylight.ex_additional_compat.capability.altar;

import com.lowdragmc.multiblocked.api.capability.trait.SingleCapabilityTrait;
import com.favouriteless.enchanted.api.power.IPowerConsumer;
import com.favouriteless.enchanted.api.power.IPowerConsumer.IPowerPosHolder;
import com.favouriteless.enchanted.common.altar.SimplePowerPosHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/**
 * Capability trait for Enchanted Altar Power in Multiblocked machines.
 * Implements IPowerConsumer to register with nearby altars.
 * INPUT ONLY - does not provide power output capability.
 */
public class AltarPowerCapabilityTrait extends SingleCapabilityTrait implements IPowerConsumer {

    private final IPowerPosHolder posHolder;

    public AltarPowerCapabilityTrait() {
        super(AltarPowerMultiblockCapability.CAP);
        this.posHolder = new SimplePowerPosHolder(BlockPos.ZERO);
    }

    @Override
    public IPowerPosHolder getPosHolder() {
        return posHolder;
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        if (compound.contains("altar_positions")) {
            ListTag posList = compound.getList("altar_positions", Tag.TAG_COMPOUND);
            for (int i = 0; i < posList.size(); i++) {
                CompoundTag posTag = posList.getCompound(i);
                BlockPos pos = new BlockPos(
                    posTag.getInt("x"),
                    posTag.getInt("y"),
                    posTag.getInt("z")
                );
                posHolder.add(pos);
            }
        }
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.writeToNBT(compound);
        ListTag posList = new ListTag();
        for (BlockPos pos : posHolder.getPositions()) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            posList.add(posTag);
        }
        compound.put("altar_positions", posList);
    }
}
