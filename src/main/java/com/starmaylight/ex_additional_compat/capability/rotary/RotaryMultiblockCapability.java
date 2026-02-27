package com.starmaylight.ex_additional_compat.capability.rotary;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.multiblocked.api.capability.IO;
import com.lowdragmc.multiblocked.api.capability.MultiblockCapability;
import com.lowdragmc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.lowdragmc.multiblocked.api.capability.trait.CapabilityTrait;
import com.lowdragmc.multiblocked.api.gui.recipe.ContentWidget;
import com.lowdragmc.multiblocked.api.recipe.ContentModifier;
import com.lowdragmc.multiblocked.api.recipe.serde.content.IContentSerializer;
import com.Da_Technomancer.crossroads.API.Capabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * Multiblocked capability for Crossroads MC Rotary (axle) energy system.
 * SIMPLIFIED VERSION - reads/writes energy values without joining propagation system.
 */
public class RotaryMultiblockCapability extends MultiblockCapability<Double> {

    public static final IContentSerializer<Double> SERIALIZER = new IContentSerializer<>() {
        @Override
        public Double fromJson(JsonElement json) { return json.getAsDouble(); }
        @Override
        public JsonElement toJson(Double content) { return new JsonPrimitive(content); }
        @Override
        public Double of(Object o) { return o instanceof Number n ? n.doubleValue() : 0.0; }
        @Override
        public void toNetwork(FriendlyByteBuf buf, Double value) { buf.writeDouble(value); }
        @Override
        public Double fromNetwork(FriendlyByteBuf buf) { return buf.readDouble(); }
    };

    public static final RotaryMultiblockCapability CAP = new RotaryMultiblockCapability();

    private RotaryMultiblockCapability() {
        super("crossroads_rotary", 0xFF8B8B00, SERIALIZER);
    }

    @Override
    public Double defaultContent() { return 50.0; }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull BlockEntity blockEntity) {
        return blockEntity.getCapability(Capabilities.AXLE_CAPABILITY).isPresent();
    }

    @Override
    public Double copyInner(Double content) { return content; }

    @Override
    public Double copyWithModifier(Double content, ContentModifier modifier) {
        return modifier.apply(content).doubleValue();
    }

    @Override
    protected CapabilityProxy<? extends Double> createProxy(@Nonnull IO io, @Nonnull BlockEntity blockEntity) {
        return new RotaryCapabilityProxy(blockEntity);
    }

    @Override
    public ContentWidget<Double> createContentWidget() { return new RotaryContentWidget(); }

    @Override
    public boolean hasTrait() { return true; }

    @Override
    public CapabilityTrait createTrait() { return new RotaryCapabilityTrait(); }

    @Override
    public BlockInfo[] getCandidates() { return new BlockInfo[]{BlockInfo.fromBlock(net.minecraft.world.level.block.Blocks.AIR)}; }
}
