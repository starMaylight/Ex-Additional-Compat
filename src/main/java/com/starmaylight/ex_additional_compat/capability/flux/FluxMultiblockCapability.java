package com.starmaylight.ex_additional_compat.capability.flux;

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
import com.Da_Technomancer.crossroads.API.technomancy.IFluxLink;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * Multiblocked capability for Crossroads MC Temporal Entropy (Flux) system.
 */
public class FluxMultiblockCapability extends MultiblockCapability<Integer> {

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

    public static final FluxMultiblockCapability CAP = new FluxMultiblockCapability();

    private FluxMultiblockCapability() {
        super("crossroads_flux", 0xFF1A1A1A, SERIALIZER);
    }

    @Override
    public Integer defaultContent() { return 16; }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull BlockEntity blockEntity) {
        // Direct: native Crossroads IFluxLink block
        if (blockEntity instanceof IFluxLink) return true;
        // Trait-based: Multiblocked component with flux trait configured
        if (blockEntity instanceof ComponentTileEntity<?> component) {
            JsonObject traits = component.getDefinition().traits;
            return traits != null && traits.has("crossroads_flux");
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
        return new FluxCapabilityProxy(blockEntity);
    }

    @Override
    public ContentWidget<Integer> createContentWidget() { return new FluxContentWidget(); }

    @Override
    public boolean hasTrait() { return true; }

    @Override
    public CapabilityTrait createTrait() { return new FluxCapabilityTrait(); }

    @Override
    public BlockInfo[] getCandidates() { return new BlockInfo[]{BlockInfo.fromBlock(net.minecraft.world.level.block.Blocks.AIR)}; }
}
