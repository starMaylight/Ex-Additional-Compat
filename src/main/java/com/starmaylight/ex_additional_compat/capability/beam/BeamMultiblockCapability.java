package com.starmaylight.ex_additional_compat.capability.beam;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import com.Da_Technomancer.crossroads.API.Capabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * Multiblocked capability for Crossroads MC Beam energy system.
 * INPUT ONLY in initial implementation.
 * Content type: int[4] = {energy, potential, stability, void}
 */
public class BeamMultiblockCapability extends MultiblockCapability<int[]> {

    public static final IContentSerializer<int[]> SERIALIZER = new IContentSerializer<>() {
        @Override
        public int[] fromJson(JsonElement json) {
            if (json.isJsonArray()) {
                JsonArray arr = json.getAsJsonArray();
                int[] r = new int[4];
                for (int i = 0; i < Math.min(4, arr.size()); i++) r[i] = arr.get(i).getAsInt();
                return r;
            }
            return new int[]{100, 0, 0, 0};
        }
        @Override
        public JsonElement toJson(int[] content) {
            JsonArray arr = new JsonArray();
            for (int v : content) arr.add(v);
            return arr;
        }
        @Override
        public int[] of(Object o) {
            if (o instanceof int[] a) return a.clone();
            return new int[]{100, 0, 0, 0};
        }
        @Override
        public void toNetwork(FriendlyByteBuf buf, int[] value) {
            for (int v : value) buf.writeInt(v);
        }
        @Override
        public int[] fromNetwork(FriendlyByteBuf buf) {
            return new int[]{buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt()};
        }
    };

    public static final BeamMultiblockCapability CAP = new BeamMultiblockCapability();

    private BeamMultiblockCapability() {
        super("crossroads_beam", 0xFFFFFF00, SERIALIZER);
    }

    @Override
    public int[] defaultContent() { return new int[]{100, 0, 0, 0}; }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull BlockEntity blockEntity) {
        if (io == IO.OUT) return false; // Beam is input-only
        if (blockEntity instanceof ComponentTileEntity<?> comp) {
            return TraitIOHelper.isTraitIOCompatible(io, comp, "crossroads_beam");
        }
        return blockEntity.getCapability(Capabilities.BEAM_CAPABILITY).isPresent();
    }

    @Override
    public int[] copyInner(int[] content) { return content.clone(); }

    @Override
    public int[] copyWithModifier(int[] content, ContentModifier modifier) {
        int[] result = new int[4];
        for (int i = 0; i < 4; i++) result[i] = modifier.apply(content[i]).intValue();
        return result;
    }

    @Override
    protected CapabilityProxy<? extends int[]> createProxy(@Nonnull IO io, @Nonnull BlockEntity blockEntity) {
        return new BeamCapabilityProxy(blockEntity);
    }

    @Override
    public ContentWidget<int[]> createContentWidget() { return new BeamContentWidget(); }

    @Override
    public boolean hasTrait() { return true; }

    @Override
    public CapabilityTrait createTrait() { return new BeamCapabilityTrait(); }

    @Override
    public BlockInfo[] getCandidates() { return new BlockInfo[]{BlockInfo.fromBlock(net.minecraft.world.level.block.Blocks.AIR)}; }
}
