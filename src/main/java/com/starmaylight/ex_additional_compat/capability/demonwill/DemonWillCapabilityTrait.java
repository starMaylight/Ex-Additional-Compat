package com.starmaylight.ex_additional_compat.capability.demonwill;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.multiblocked.api.capability.trait.SingleCapabilityTrait;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

/**
 * Capability trait for Blood Magic Demon Will in Multiblocked machines.
 * Provides configuration for will type and auto-I/O behavior.
 */
public class DemonWillCapabilityTrait extends SingleCapabilityTrait {

    private int willTypeIndex = 0;

    public DemonWillCapabilityTrait() {
        super(DemonWillMultiblockCapability.CAP);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            if (json.has("willType")) {
                willTypeIndex = json.get("willType").getAsInt();
            }
        }
    }

    @Override
    public JsonElement deserialize() {
        JsonElement base = super.deserialize();
        if (base != null && base.isJsonObject()) {
            base.getAsJsonObject().addProperty("willType", willTypeIndex);
        }
        return base;
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        willTypeIndex = compound.getInt("will_type");
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.writeToNBT(compound);
        compound.putInt("will_type", willTypeIndex);
    }

    public int getWillTypeIndex() { return willTypeIndex; }
}
