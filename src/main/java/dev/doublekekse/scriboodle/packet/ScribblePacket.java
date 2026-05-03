package dev.doublekekse.scriboodle.packet;

import dev.doublekekse.scriboodle.Scriboodle;
import dev.doublekekse.scriboodle.client.ScriboodleClient;
import dev.doublekekse.scriboodle.data.PaginatedScribbleData;
import dev.doublekekse.scriboodle.duck.MinecraftServerDuck;
import dev.doublekekse.scriboodle.registry.ScriboodleComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public record ScribblePacket(
    PaginatedScribbleData paginatedScribbleData,
    int slot
) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ScribblePacket> STREAM_CODEC = StreamCodec.composite(
        PaginatedScribbleData.STREAM_CODEC, ScribblePacket::paginatedScribbleData,
        ByteBufCodecs.INT, ScribblePacket::slot,
        ScribblePacket::new
    );
    public static final CustomPacketPayload.Type<ScribblePacket> TYPE = new CustomPacketPayload.Type<>(Scriboodle.id("serverbound_scribble_packet"));

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(ScribblePacket packet, ServerPlayNetworking.Context ctx) {
        if (!Inventory.isHotbarSlot(packet.slot) && packet.slot != Inventory.SLOT_OFFHAND) {
            return;
        }

        var stack = ctx.player().getInventory().getItem(packet.slot);

        var style = stack.get(ScriboodleComponents.SCRIBBLE_STYLE);
        if (style == null || !style.validate(packet.paginatedScribbleData)) {
            return;
        }

        var ref = stack.get(ScriboodleComponents.SCRIBBLE_REFERENCE);
        if (ref != null) {
            ((MinecraftServerDuck) ctx.server()).scriboodle$getScribbleManager().set(ref, packet.paginatedScribbleData);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void handleClient(ScribblePacket packet, ClientPlayNetworking.Context ctx) {
        if (!Inventory.isHotbarSlot(packet.slot) && packet.slot != Inventory.SLOT_OFFHAND) {
            return;
        }

        ScriboodleClient.openScreen(ctx.player(), packet.paginatedScribbleData, packet.slot);
    }
}
