package dev.doublekekse.scriboodle.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.doublekekse.scriboodle.Scriboodle;
import dev.doublekekse.scriboodle.data.PaginatedScribbleData;
import dev.doublekekse.scriboodle.packet.PaginatedScribblePacket;
import dev.doublekekse.scriboodle.registry.ScriboodleComponents;
import dev.doublekekse.scriboodle.gui.screen.ScribbleScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Player;

public class ScriboodleClient implements ClientModInitializer {
    public static final VertexFormat SCRIBBLE_FORMAT = VertexFormat.builder()
        .add("Position", VertexFormatElement.POSITION)
        .add("FillColor", VertexFormatElement.COLOR)
        .add("UV0", VertexFormatElement.UV0)
        .add("UV1", VertexFormatElement.UV1)
        .add("UV2", VertexFormatElement.UV2)
        .add("Radius", VertexFormatElement.LINE_WIDTH)
        .build();

    public static final RenderPipeline SCRIBBLE_GUI = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Scriboodle.id("pipeline/scribble"))
            .withVertexShader(Scriboodle.id("core/scribble"))
            .withFragmentShader(Scriboodle.id("core/scribble"))
            .withVertexFormat(SCRIBBLE_FORMAT, VertexFormat.Mode.QUADS)
            .build()
    );

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(PaginatedScribblePacket.TYPE, PaginatedScribblePacket::handleClient);
    }

    public static void openScreen(Player player, PaginatedScribbleData scribble, int slot) {
        var stack = player.getInventory().getItem(slot);
        var style = stack.get(ScriboodleComponents.SCRIBBLE_STYLE);

        if (style != null && style.validate(scribble)) {
            Minecraft.getInstance().setScreen(new ScribbleScreen(player, slot, scribble, style));
        }
    }
}
