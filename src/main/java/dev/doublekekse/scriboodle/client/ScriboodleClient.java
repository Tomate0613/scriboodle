package dev.doublekekse.scriboodle.client;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
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
    // TODO Figure out a way to make this cleaner, probably not use vanilla BufferBuilder
    public static final VertexFormat SCRIBBLE_FORMAT = VertexFormat.builder(0)
        .addAttribute("Position", GpuFormat.RGB32_FLOAT)
        .addAttribute("Color", GpuFormat.RGBA8_UNORM)
        .addAttribute("UV0", GpuFormat.RG32_FLOAT)
        .addAttribute("UV1", GpuFormat.RG16_SINT)
        .addAttribute("UV2", GpuFormat.RG16_SINT)
        // This is used for radius, but it seems like i can no longer give it any name when using BufferBuilder
        .addAttribute("LineWidth", GpuFormat.R32_FLOAT)
        .build();

    public static final RenderPipeline SCRIBBLE_GUI = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Scriboodle.id("pipeline/scribble"))
            .withVertexShader(Scriboodle.id("core/scribble"))
            .withFragmentShader(Scriboodle.id("core/scribble"))
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withVertexBinding(0, SCRIBBLE_FORMAT)
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
            Minecraft.getInstance().gui.setScreen(new ScribbleScreen(player, slot, scribble, style));
        }
    }
}
