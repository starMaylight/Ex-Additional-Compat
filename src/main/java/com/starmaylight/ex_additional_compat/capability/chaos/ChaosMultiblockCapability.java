package com.starmaylight.ex_additional_compat.capability.chaos;

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
import com.google.gson.JsonObject;
import net.foxmcloud.draconicadditions.blocks.tileentity.TileChaosHolderBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * Multiblocked capability for Draconic Additions Chaos energy system.
 */
public class ChaosMultiblockCapability extends MultiblockCapability<Integer> {

    public static final IContentSerializer<Integer> SERIALIZER = new IContentSerializer<>() {
        @Override
        public Integer fromJson(JsonElement json) { return json.getAsInt(); }
        @Override
        public JsonElement toJson(Integer content) { return new JsonPrimitive(content); }
        @Override
        public Integer of(Object o) { return o instanceof Number n ? n.intValue() : 0; }
        @Override
        public void toNetwork(FriendlyByteBuf buf, Integer value) { buf.writeInt(value); }
        @Override
        public Integer fromNetwork(FriendlyByteBuf buf) { return buf.readInt(); }
    };

    public static final ChaosMultiblockCapability CAP = new ChaosMultiblockCapability();

    private ChaosMultiblockCapability() {
        super("draconic_chaos", 0xFF6A0DAD, SERIALIZER);
    }

    @Override
    public Integer defaultContent() { return 1000; }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull BlockEntity blockEntity) {
        // Direct: native Draconic Additions chaos holder block
        if (blockEntity instanceof TileChaosHolderBase) return true;
        // Trait-based: Multiblocked component with chaos trait configured
        if (blockEntity instanceof ComponentTileEntity<?> component) {
            JsonObject traits = component.getDefinition().traits;
            return traits != null && traits.has("draconic_chaos");
        }
        return false;
    }

    @Override
    public Integer copyInner(Integer content) { return content; }

    @Override
    public Integer copyWithModifier(Integer content, ContentModifier modifier) {
        return modifier.apply(content).intValue();
    }

    @Override
    protected CapabilityProxy<? extends Integer> createProxy(@Nonnull IO io, @Nonnull BlockEntity blockEntity) {
        return new ChaosCapabilityProxy(blockEntity);
    }

    @Override
    public ContentWidget<Integer> createContentWidget() { return new ChaosContentWidget(); }

    @Override
    public boolean hasTrait() { return true; }

    @Override
    public CapabilityTrait createTrait() { return new ChaosCapabilityTrait(); }

    @Override
    public BlockInfo[] getCandidates() { return new BlockInfo[]{BlockInfo.fromBlock(net.minecraft.world.level.block.Blocks.AIR)}; }
}
