package dev.doublekekse.scriboodle.packet;

import dev.doublekekse.scriboodle.Scriboodle;
import dev.doublekekse.scriboodle.client.ScriboodleClient;
import dev.doublekekse.scriboodle.data.PaginatedScribbleData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public record PaginatedScribblePacket(
    PaginatedScribbleData paginatedScribbleData,
    int slot
) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PaginatedScribblePacket> STREAM_CODEC = StreamCodec.composite(
        PaginatedScribbleData.STREAM_CODEC, PaginatedScribblePacket::paginatedScribbleData,
        ByteBufCodecs.INT, PaginatedScribblePacket::slot,
        PaginatedScribblePacket::new
    );
    public static final CustomPacketPayload.Type<PaginatedScribblePacket> TYPE = new CustomPacketPayload.Type<>(Scriboodle.id("paginated_scribble_packet"));

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

//    public static void handleServer(PaginatedScribblePacket packet, ServerPlayNetworking.Context ctx) {
//        if (!Inventory.isHotbarSlot(packet.slot) && packet.slot != Inventory.SLOT_OFFHAND) {
//            return;
//        }
//
//        var stack = ctx.player().getInventory().getItem(packet.slot);
//
//        var style = stack.get(ScriboodleComponents.SCRIBBLE_STYLE);
//        if (style == null || !style.validate(packet.paginatedScribbleData)) {
//            return;
//        }
//
//        var ref = stack.get(ScriboodleComponents.SCRIBBLE_REFERENCE);
//        if (ref != null) {
//            ((MinecraftServerDuck) ctx.server()).scriboodle$getScribbleManager().set(ref, packet.paginatedScribbleData);
//        }
//    }

    @Environment(EnvType.CLIENT)
    public static void handleClient(PaginatedScribblePacket packet, ClientPlayNetworking.Context ctx) {
        if (!Inventory.isHotbarSlot(packet.slot) && packet.slot != Inventory.SLOT_OFFHAND) {
            return;
        }

        ScriboodleClient.openScreen(ctx.player(), packet.paginatedScribbleData, packet.slot);
    }
}
