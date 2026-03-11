package com.starmaylight.ex_additional_compat.capability.demonwill;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.multiblocked.api.capability.IO;
import com.lowdragmc.multiblocked.api.capability.MultiblockCapability;
import com.lowdragmc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.lowdragmc.multiblocked.api.capability.trait.CapabilityTrait;
import com.lowdragmc.multiblocked.api.gui.recipe.ContentWidget;
import com.lowdragmc.multiblocked.api.recipe.Content;
import com.lowdragmc.multiblocked.api.recipe.ContentModifier;
import com.lowdragmc.multiblocked.api.recipe.serde.content.IContentSerializer;
import com.lowdragmc.multiblocked.api.tile.ComponentTileEntity;
import com.starmaylight.ex_additional_compat.capability.TraitIOHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * Multiblocked capability for Blood Magic Demon Will system.
 * Chunk-based I/O via WorldDemonWillHandler.
 *
 * Will type is stored in Content.uiName as "DEFAULT", "CORROSIVE", etc.
 */
public class DemonWillMultiblockCapability extends MultiblockCapability<Double> {

    /** Valid will type names matching EnumDemonWillType ordinals. */
    public static final String[] WILL_TYPES = {"DEFAULT", "CORROSIVE", "DESTRUCTIVE", "VENGEFUL", "STEADFAST"};

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

        @Override
        public JsonElement toJsonContent(Content content) {
            JsonObject json = new JsonObject();
            json.add("content", toJson((Double) content.content));
            json.addProperty("chance", content.chance);
            if (content.slotName != null) json.addProperty("slotName", content.slotName);
            // Store will type as extra field
            String willType = content.uiName != null ? content.uiName : "DEFAULT";
            json.addProperty("willType", willType);
            return json;
        }

        @Override
        public Content fromJsonContent(JsonElement jsonElement) {
            JsonObject json = jsonElement.getAsJsonObject();
            Double value = fromJson(json.get("content"));
            float chance = json.has("chance") ? json.get("chance").getAsFloat() : 1.0f;
            String slotName = json.has("slotName") ? json.get("slotName").getAsString() : null;
            // Read will type; store in uiName
            String willType = json.has("willType") ? json.get("willType").getAsString() : "DEFAULT";
            return new Content(value, chance, slotName, willType);
        }
    };

    public static final DemonWillMultiblockCapability CAP = new DemonWillMultiblockCapability();

    private DemonWillMultiblockCapability() {
        super("bloodmagic_demonwill", 0xFF8B0000, SERIALIZER);
    }

    @Override
    public Double defaultContent() { return 10.0; }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull BlockEntity blockEntity) {
        // Trait-based: check trait exists AND mbdIO direction is compatible
        if (blockEntity instanceof ComponentTileEntity<?> comp) {
            return TraitIOHelper.isTraitIOCompatible(io, comp, "bloodmagic_demonwill");
        }
        return true; // Demon Will is chunk-based, any non-MBD block can serve
    }

    @Override
    public Double copyInner(Double content) { return content; }

    @Override
    public Double copyWithModifier(Double content, ContentModifier modifier) {
        return modifier.apply(content).doubleValue();
    }

    @Override
    protected CapabilityProxy<? extends Double> createProxy(@Nonnull IO io, @Nonnull BlockEntity blockEntity) {
        return new DemonWillCapabilityProxy(blockEntity);
    }

    @Override
    public ContentWidget<Double> createContentWidget() { return new DemonWillContentWidget(); }

    @Override
    public boolean hasTrait() { return true; }

    @Override
    public CapabilityTrait createTrait() { return new DemonWillCapabilityTrait(); }

    @Override
    public BlockInfo[] getCandidates() { return new BlockInfo[]{BlockInfo.fromBlock(net.minecraft.world.level.block.Blocks.AIR)}; }
}
