package com.starmaylight.ex_additional_compat.capability.flux;

import com.Da_Technomancer.crossroads.API.IInfoTE;
import com.Da_Technomancer.crossroads.API.packets.IIntArrayReceiver;
import com.Da_Technomancer.crossroads.API.technomancy.IFluxLink;
import com.Da_Technomancer.essentials.tileentities.ILinkTE;
import com.Da_Technomancer.essentials.tileentities.LinkHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.multiblocked.api.capability.trait.SingleCapabilityTrait;
import com.lowdragmc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capability trait for Crossroads Flux (Temporal Entropy) in Multiblocked machines.
 * Provides internal flux buffer with two import mechanisms:
 *
 * 1. Auto-import from adjacent IFluxLink blocks (every tick)
 * 2. Link-based import via Essentials LinkingTool (IFluxLink implementation)
 *
 * Implements IFluxLink (not just ILinkTE) so that Crossroads' FluxHelper.canLink()
 * passes the instanceof check. FluxHelper.canLink() checks:
 *   other instanceof IFluxLink && ((IFluxLink) other).canAcceptLinks()
 *
 * Linking flow:
 * - Player clicks Entropy Node with Linking Tool -> stores position on tool
 * - Player clicks MBD machine with Linking Tool -> MixinComponentUse delegates to
 *   FluxInteractionHelper -> calls LinkHelper.wrench(this, stack, player)
 * - This trait stores the link and pulls flux from the linked block in update()
 */
public class FluxCapabilityTrait extends SingleCapabilityTrait implements IFluxLink {

    // Static registry for Proxy -> Trait access
    private static final Map<BlockPos, FluxCapabilityTrait> TRAIT_REGISTRY = new ConcurrentHashMap<>();

    private int flux = 0;
    private int maxFlux = 64;

    // Link system (Essentials ILinkTE)
    private LinkHelper linkHelper;

    public FluxCapabilityTrait() {
        super(FluxMultiblockCapability.CAP);
    }

    /** Lazy-init LinkHelper (needs component to be set first) */
    public LinkHelper getLinkHelper() {
        if (linkHelper == null) {
            linkHelper = new LinkHelper(this);
        }
        return linkHelper;
    }

    // --- IFluxLink implementation ---

    @Override
    public int getFlux() { return flux; }

    @Override
    public int getReadingFlux() { return flux; }

    @Override
    public void addFlux(int amount) {
        int old = flux;
        flux = Math.max(0, Math.min(maxFlux, flux + amount));
        if (flux != old) markAsDirty();
    }

    @Override
    public int getMaxFlux() { return maxFlux; }

    @Override
    public boolean canAcceptLinks() { return true; }

    @Override
    public int[] getRenderedArcs() { return new int[0]; }

    public void setFlux(int amount) {
        int old = flux;
        flux = Math.max(0, Math.min(maxFlux, amount));
        if (flux != old) markAsDirty();
    }

    // --- ILinkTE implementation (for Essentials Linking Tool) ---

    @Override
    public boolean canBeginLinking() {
        // Must be false: MBD machine acts as a SINK (like FluxSinkTileEntity).
        // If true, clicking MBD first stores its position on the tool, then
        // clicking Entropy Node checks stored position -> finds ComponentTileEntity
        // -> not ILinkTE -> "Invalid pair". By returning false, the player must
        // click the Entropy Node first, then the MBD machine.
        return false;
    }

    @Override
    public boolean canLink(ILinkTE other) {
        // Only link to IFluxLink blocks (Entropy Nodes, etc.)
        return other instanceof IFluxLink;
    }

    @Override
    public Set<BlockPos> getLinks() {
        return getLinkHelper().getLinksRelative();
    }

    @Override
    public boolean createLinkSource(ILinkTE other, @Nullable Player player) {
        return getLinkHelper().addLink(other, player);
    }

    @Override
    public void removeLinkSource(BlockPos pos) {
        getLinkHelper().removeLink(pos);
    }

    @Override
    public BlockEntity getTE() {
        return component;
    }

    @Override
    public int getMaxLinks() {
        return 16; // SINK behaviour: accept many links
    }

    @Override
    public int getRange() {
        return 16; // Same as Crossroads default
    }

    @Override
    public Color getColor() {
        return Color.RED; // Flux link color
    }

    // ILongReceiver (inherited from ILinkTE -> IFluxLink)
    @Override
    public void receiveLong(byte identifier, long message, @Nullable ServerPlayer player) {
        getLinkHelper().handleIncomingPacket(identifier, message);
    }

    // IIntArrayReceiver (inherited from IFluxLink)
    @Override
    public void receiveInts(byte identifier, int[] data, @Nullable ServerPlayer player) {
        // No-op: we don't use rendered arcs on this side
    }

    // IInfoTE (inherited from IFluxLink)
    @Override
    public void addInfo(ArrayList<Component> chat, Player player, BlockHitResult hit) {
        chat.add(new TextComponent("Flux: " + flux + "/" + maxFlux));
    }

    // --- GUI: show current flux amount ---

    @Override
    public void createUI(ComponentTileEntity<?> comp, WidgetGroup group, net.minecraft.world.entity.player.Player player) {
        super.createUI(comp, group, player);
        group.addWidget(new LabelWidget(x, y,
                () -> "Flux: " + this.getFlux() + " / " + this.getMaxFlux())
                .setTextColor(0xFFFF4444).setDropShadow(true));
    }

    // --- Serialization ---

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            if (json.has("maxFlux")) {
                maxFlux = json.get("maxFlux").getAsInt();
            }
        }
    }

    @Override
    public JsonElement deserialize() {
        JsonElement base = super.deserialize();
        if (base != null && base.isJsonObject()) {
            base.getAsJsonObject().addProperty("maxFlux", maxFlux);
        }
        return base;
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        flux = compound.getInt("flux_amount");
        getLinkHelper().readNBT(compound);
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.writeToNBT(compound);
        compound.putInt("flux_amount", flux);
        getLinkHelper().writeNBT(compound);
    }

    // --- Auto-import from adjacent + linked IFluxLink blocks ---

    @Override
    public boolean hasUpdate() { return true; }

    @Override
    public void update() {
        super.update();
        if (component == null || component.getLevel() == null || component.getLevel().isClientSide()) return;
        if (flux >= maxFlux) return; // Buffer full

        BlockPos pos = component.getBlockPos();

        // Collect linked positions to avoid double-import.
        // Linked blocks already push flux via Crossroads' FluxHelper.performTransfer()
        // through MixinComponentFluxLink, so we must NOT also pull from them here.
        Set<BlockPos> linkedPositions = new java.util.HashSet<>();
        for (BlockPos lp : getLinkHelper().getLinksAbsolute()) {
            linkedPositions.add(lp);
        }

        // Auto-import from adjacent IFluxLink blocks (only those NOT linked)
        for (Direction dir : Direction.values()) {
            BlockPos adjacentPos = pos.relative(dir);
            // Skip blocks that are linked — they push flux via performTransfer()
            if (linkedPositions.contains(adjacentPos)) continue;

            BlockEntity adjacent = component.getLevel().getBlockEntity(adjacentPos);
            if (adjacent instanceof IFluxLink fluxLink) {
                int available = fluxLink.getFlux();
                int space = maxFlux - flux;
                int transfer = Math.min(available, space);
                if (transfer > 0) {
                    fluxLink.addFlux(-transfer);
                    addFlux(transfer); // Use addFlux() for consistency
                    if (flux >= maxFlux) return;
                }
            }
        }

        // NOTE: Linked blocks (via Linking Tool) are NOT pulled here.
        // Crossroads' Entropy Node pushes flux to this machine via
        // FluxHelper.performTransfer() -> MixinComponentFluxLink.addFlux()
        // which delegates to this trait's addFlux(). No pull needed.
    }

    // --- Static Registry ---

    @Override
    public void onLoad() {
        super.onLoad();
        if (component != null && component.getBlockPos() != null) {
            TRAIT_REGISTRY.put(component.getBlockPos(), this);
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        unregister();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        unregister();
    }

    private void unregister() {
        if (component != null && component.getBlockPos() != null) {
            TRAIT_REGISTRY.remove(component.getBlockPos());
        }
    }

    /**
     * Find the FluxCapabilityTrait for a given BlockEntity (at its position or adjacent).
     */
    public static FluxCapabilityTrait getTraitFor(BlockEntity be) {
        if (be == null) return null;
        BlockPos pos = be.getBlockPos();

        FluxCapabilityTrait direct = TRAIT_REGISTRY.get(pos);
        if (direct != null) return direct;

        for (Map.Entry<BlockPos, FluxCapabilityTrait> entry : TRAIT_REGISTRY.entrySet()) {
            BlockPos traitPos = entry.getKey();
            int dist = Math.abs(traitPos.getX() - pos.getX())
                     + Math.abs(traitPos.getY() - pos.getY())
                     + Math.abs(traitPos.getZ() - pos.getZ());
            if (dist <= 1) {
                return entry.getValue();
            }
        }
        return null;
    }
}
