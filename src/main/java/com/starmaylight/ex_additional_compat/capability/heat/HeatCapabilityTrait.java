package com.starmaylight.ex_additional_compat.capability.heat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.multiblocked.api.capability.trait.SingleCapabilityTrait;
import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.heat.IHeatHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Capability trait for Crossroads heat energy in Multiblocked machines.
 * Provides internal heat buffer and exposes IHeatHandler capability.
 */
public class HeatCapabilityTrait extends SingleCapabilityTrait {

    private double temperature = 0;
    private double maxTemperature = 1000.0;

    private final LazyOptional<IHeatHandler> heatHandlerOpt = LazyOptional.of(() -> new IHeatHandler() {
        @Override
        public double getTemp() { return temperature; }

        @Override
        public void setTemp(double tempIn) {
            temperature = Math.max(-273.0, tempIn);
            HeatCapabilityTrait.this.markAsDirty();
        }

        @Override
        public void addHeat(double heat) {
            temperature = Math.max(-273.0, temperature + heat);
            if (temperature > maxTemperature) temperature = maxTemperature;
            HeatCapabilityTrait.this.markAsDirty();
        }
    });

    public HeatCapabilityTrait() {
        super(HeatMultiblockCapability.CAP);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            if (json.has("maxTemperature")) {
                maxTemperature = json.get("maxTemperature").getAsDouble();
            }
        }
    }

    @Override
    public JsonElement deserialize() {
        JsonElement base = super.deserialize();
        if (base != null && base.isJsonObject()) {
            base.getAsJsonObject().addProperty("maxTemperature", maxTemperature);
        }
        return base;
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        temperature = compound.getDouble("heat_temp");
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.writeToNBT(compound);
        compound.putDouble("heat_temp", temperature);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capability == Capabilities.HEAT_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == Capabilities.HEAT_CAPABILITY) return heatHandlerOpt.cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        heatHandlerOpt.invalidate();
    }
}
