package com.starmaylight.ex_additional_compat.mixin.multiblocked;

import com.Da_Technomancer.crossroads.API.technomancy.IFluxLink;
import com.Da_Technomancer.essentials.tileentities.ILinkTE;
import com.lowdragmc.multiblocked.api.tile.ComponentTileEntity;
import com.starmaylight.ex_additional_compat.capability.flux.FluxCapabilityTrait;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * Mixin to make ComponentTileEntity implement IFluxLink so that
 * Crossroads' FluxUtil.performTransfer() can push flux to MBD machines.
 *
 * The Crossroads flux transfer system checks:
 *   be instanceof IFluxLink && ((IFluxLink) be).allowAccepting()
 * Without this mixin, ComponentTileEntity fails the instanceof check
 * and no flux is transferred even when links are established.
 *
 * All methods delegate to FluxCapabilityTrait if present.
 * When no trait is present, safe defaults are returned (no flux, no links).
 */
@Mixin(value = ComponentTileEntity.class, remap = false)
public abstract class MixinComponentFluxLink implements IFluxLink {

    // --- Helper to find the trait ---

    private FluxCapabilityTrait excompat$getFluxTrait() {
        return FluxCapabilityTrait.getTraitFor((BlockEntity) (Object) this);
    }

    // --- IFluxLink methods ---

    @Override
    public int getFlux() {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        return trait != null ? trait.getFlux() : 0;
    }

    @Override
    public int getReadingFlux() {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        return trait != null ? trait.getReadingFlux() : 0;
    }

    @Override
    public void addFlux(int amount) {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        if (trait != null) {
            trait.addFlux(amount);
        }
    }

    @Override
    public int getMaxFlux() {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        return trait != null ? trait.getMaxFlux() : 0;
    }

    @Override
    public boolean canAcceptLinks() {
        return excompat$getFluxTrait() != null;
    }

    @Override
    public int[] getRenderedArcs() {
        return new int[0];
    }

    // --- ILinkTE methods ---

    @Override
    public boolean canBeginLinking() {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        return trait != null && trait.canBeginLinking();
    }

    @Override
    public boolean canLink(ILinkTE other) {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        return trait != null && trait.canLink(other);
    }

    @Override
    public Set<BlockPos> getLinks() {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        return trait != null ? trait.getLinks() : Collections.emptySet();
    }

    @Override
    public boolean createLinkSource(ILinkTE other, @Nullable Player player) {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        return trait != null && trait.createLinkSource(other, player);
    }

    @Override
    public void removeLinkSource(BlockPos pos) {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        if (trait != null) {
            trait.removeLinkSource(pos);
        }
    }

    @Override
    public BlockEntity getTE() {
        return (BlockEntity) (Object) this;
    }

    @Override
    public int getMaxLinks() {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        return trait != null ? trait.getMaxLinks() : 0;
    }

    @Override
    public int getRange() {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        return trait != null ? trait.getRange() : 0;
    }

    @Override
    public Color getColor() {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        return trait != null ? trait.getColor() : Color.RED;
    }

    // --- ILongReceiver ---

    @Override
    public void receiveLong(byte identifier, long message, @Nullable ServerPlayer player) {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        if (trait != null) {
            trait.receiveLong(identifier, message, player);
        }
    }

    // --- IIntArrayReceiver ---

    @Override
    public void receiveInts(byte identifier, int[] data, @Nullable ServerPlayer player) {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        if (trait != null) {
            trait.receiveInts(identifier, data, player);
        }
    }

    // --- IInfoTE ---

    @Override
    public void addInfo(ArrayList<Component> chat, Player player, BlockHitResult hit) {
        FluxCapabilityTrait trait = excompat$getFluxTrait();
        if (trait != null) {
            trait.addInfo(chat, player, hit);
        }
    }

    // --- IFluxLink defaults we need to override for safety ---

    @Override
    public boolean allowAccepting() {
        // Only accept flux if we have a flux trait
        return excompat$getFluxTrait() != null;
    }
}
