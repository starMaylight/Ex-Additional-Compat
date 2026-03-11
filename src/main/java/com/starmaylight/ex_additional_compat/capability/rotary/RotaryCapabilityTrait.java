package com.starmaylight.ex_additional_compat.capability.rotary;

import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.rotary.IAxleHandler;
import com.Da_Technomancer.crossroads.API.rotary.IAxisHandler;
import com.Da_Technomancer.crossroads.API.rotary.ICogHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.multiblocked.api.capability.trait.SingleCapabilityTrait;
import com.lowdragmc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Capability trait for Crossroads Rotary energy in Multiblocked machines.
 * Exposes both IAxleHandler (for direct axle connections like HandCrank)
 * and ICogHandler (for gear meshing via Crossroads gear propagation system).
 */
public class RotaryCapabilityTrait extends SingleCapabilityTrait {

    private double energy = 0;
    private double moInertia = 1.0;

    // Axis propagation state (set during gear discovery)
    private IAxisHandler axis = null;
    private double rotRatio = 1.0;
    private byte updateKey = -1;
    private boolean renderOffset = false;

    private final IAxleHandler axleHandler = new IAxleHandler() {
        @Override
        public double getSpeed() {
            return axis != null ? rotRatio * axis.getBaseSpeed() : 0;
        }

        @Override
        public double getEnergy() { return energy; }

        @Override
        public void setEnergy(double e) {
            energy = e;
            RotaryCapabilityTrait.this.markAsDirty();
        }

        @Override
        public void addEnergy(double e, boolean absolute) {
            energy += absolute ? Math.abs(e) : e;
            RotaryCapabilityTrait.this.markAsDirty();
        }

        @Override
        public double getMoInertia() { return moInertia; }

        @Override
        public double getRotationRatio() { return rotRatio; }

        @Override
        public float getAngle(float partialTicks) {
            return axis != null ? axis.getAngle(rotRatio, partialTicks, renderOffset, 22.5f) : 0;
        }

        @Override
        public void propagate(@Nullable IAxisHandler source, byte key, double ratio,
                              double speedIn, boolean negated) {
            if (key == updateKey) return;
            if (source != null && !source.addToList(axleHandler)) return;
            rotRatio = ratio == 0 ? 1 : ratio;
            renderOffset = negated;
            updateKey = key;
            axis = source;
        }

        @Override
        public void disconnect() {
            axis = null;
        }
    };

    private final ICogHandler cogHandler = new ICogHandler() {
        @Override
        public void connect(IAxisHandler axisHandler, byte key, double ratio,
                            double speed, Direction dir, boolean inverted) {
            axleHandler.propagate(axisHandler, key, ratio, speed, !inverted);
        }

        @Override
        public IAxleHandler getAxle() {
            return axleHandler;
        }
    };

    private final LazyOptional<IAxleHandler> axleHandlerOpt = LazyOptional.of(() -> axleHandler);
    private final LazyOptional<ICogHandler> cogHandlerOpt = LazyOptional.of(() -> cogHandler);

    public RotaryCapabilityTrait() {
        super(RotaryMultiblockCapability.CAP);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            if (json.has("moInertia")) {
                moInertia = json.get("moInertia").getAsDouble();
            }
        }
    }

    @Override
    public JsonElement deserialize() {
        JsonElement base = super.deserialize();
        if (base != null && base.isJsonObject()) {
            base.getAsJsonObject().addProperty("moInertia", moInertia);
        }
        return base;
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        energy = compound.getDouble("rotary_energy");
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.writeToNBT(compound);
        compound.putDouble("rotary_energy", energy);
    }

    // --- GUI: show current speed and energy ---

    @Override
    public void createUI(ComponentTileEntity<?> comp, WidgetGroup group, Player player) {
        super.createUI(comp, group, player);
        group.addWidget(new LabelWidget(x, y,
                () -> String.format("Speed: %.2f  Energy: %.1f", axleHandler.getSpeed(), energy))
                .setTextColor(0xFFFFFF).setDropShadow(true));
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capability == Capabilities.AXLE_CAPABILITY
                || capability == Capabilities.COG_CAPABILITY
                || super.hasCapability(capability, facing);
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == Capabilities.AXLE_CAPABILITY) return axleHandlerOpt.cast();
        if (capability == Capabilities.COG_CAPABILITY) return cogHandlerOpt.cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        axleHandlerOpt.invalidate();
        cogHandlerOpt.invalidate();
    }
}
