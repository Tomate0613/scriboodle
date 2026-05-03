package dev.doublekekse.scriboodle.mixin;

import dev.doublekekse.scriboodle.client.ScriboodleClient;
import dev.doublekekse.scriboodle.duck.GuiGraphicsExtractorDuck;
import dev.doublekekse.scriboodle.gui.state.ScribbleRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin implements GuiGraphicsExtractorDuck {
    @Shadow
    @Final
    public GuiRenderState guiRenderState;

    @Shadow
    @Final
    private Matrix3x2fStack pose;

    @Shadow
    @Final
    public GuiGraphicsExtractor.ScissorStack scissorStack;

    @Override
    public void scriboodle$scribble(final AbstractTexture texture, final int x, final int y, final int width, final int height, final int mouseX, final int mouseY, final double radius, final int color) {
        guiRenderState.addGuiElement(
            new ScribbleRenderState(
                ScriboodleClient.SCRIBBLE_GUI,
                TextureSetup.singleTexture(texture.getTextureView(),
                    texture.getSampler()),
                new Matrix3x2f(pose),
                x, y,
                width, height,
                mouseX, mouseY,
                radius,
                color,
                scissorStack.peek()
            )
        );
    }
}
