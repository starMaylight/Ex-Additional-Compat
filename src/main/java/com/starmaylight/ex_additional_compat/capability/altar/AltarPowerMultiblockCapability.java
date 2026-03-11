package com.starmaylight.ex_additional_compat.capability.altar;

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
import com.lowdragmc.multiblocked.api.tile.ComponentTileEntity;
import com.starmaylight.ex_additional_compat.capability.TraitIOHelper;
import com.favouriteless.enchanted.api.power.IPowerProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * Multiblocked capability for Enchanted: Witchcraft Altar Power system.
 * INPUT ONLY - output is intentionally not supported.
 */
public class AltarPowerMultiblockCapability extends MultiblockCapability<Double> {

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

    public static final AltarPowerMultiblockCapability CAP = new AltarPowerMultiblockCapability();

    private AltarPowerMultiblockCapability() {
        super("enchanted_altar", 0xFF7B2D8B, SERIALIZER);
    }

    @Override
    public Double defaultContent() { return 50.0; }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull BlockEntity blockEntity) {
        if (io == IO.OUT) return false; // Altar power is input-only
        // Direct: native Enchanted altar power provider
        if (blockEntity instanceof IPowerProvider) return true;
        // Trait-based: check trait exists AND mbdIO direction is compatible
        if (blockEntity instanceof ComponentTileEntity<?> comp) {
            return TraitIOHelper.isTraitIOCompatible(io, comp, "enchanted_altar");
        }
        return false;
    }

    @Override
    public Double copyInner(Double content) { return content; }

    @Override
    public Double copyWithModifier(Double content, ContentModifier modifier) {
        return modifier.apply(content).doubleValue();
    }

    @Override
    protected CapabilityProxy<? extends Double> createProxy(@Nonnull IO io, @Nonnull BlockEntity blockEntity) {
        return new AltarPowerCapabilityProxy(blockEntity);
    }

    @Override
    public ContentWidget<Double> createContentWidget() { return new AltarPowerContentWidget(); }

    @Override
    public boolean hasTrait() { return true; }

    @Override
    public CapabilityTrait createTrait() { return new AltarPowerCapabilityTrait(); }

    @Override
    public BlockInfo[] getCandidates() { return new BlockInfo[]{BlockInfo.fromBlock(net.minecraft.world.level.block.Blocks.AIR)}; }
}
