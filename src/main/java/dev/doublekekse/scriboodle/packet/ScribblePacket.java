package dev.doublekekse.scriboodle.packet;

import dev.doublekekse.scriboodle.Scriboodle;
import dev.doublekekse.scriboodle.data.ScribbleData;
import dev.doublekekse.scriboodle.duck.MinecraftServerDuck;
import dev.doublekekse.scriboodle.registry.ScriboodleComponents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public record ScribblePacket(
    ScribbleData scribbleData,
    int page,
    int slot
) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ScribblePacket> STREAM_CODEC = StreamCodec.composite(
        ScribbleData.STREAM_CODEC, ScribblePacket::scribbleData,
        ByteBufCodecs.INT, ScribblePacket::page,
        ByteBufCodecs.INT, ScribblePacket::slot,
        ScribblePacket::new
    );
    public static final CustomPacketPayload.Type<ScribblePacket> TYPE = new CustomPacketPayload.Type<>(Scriboodle.id("scribble_packet"));

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
        if (style == null || !style.validate(packet.scribbleData, packet.page)) {
            return;
        }

        var ref = stack.get(ScriboodleComponents.SCRIBBLE_REFERENCE);
        if (ref != null) {
            ((MinecraftServerDuck) ctx.server()).scriboodle$getScribbleManager().set(ref, packet.page, packet.scribbleData);
        }
    }

//    @Environment(EnvType.CLIENT)
//    public static void handleClient(ScribblePacket packet, ClientPlayNetworking.Context ctx) {
//        if (!Inventory.isHotbarSlot(packet.slot) && packet.slot != Inventory.SLOT_OFFHAND) {
//            return;
//        }
//
//        ScriboodleClient.openScreen(ctx.player(), packet.scribbleData, packet.slot);
//    }
}
