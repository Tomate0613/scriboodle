package dev.doublekekse.scriboodle.gui.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public record ScribbleRenderState(
    RenderPipeline pipeline,
    TextureSetup textureSetup,
    Matrix3x2f pose,
    int x, int y,
    int width, int height,
    int mouseX, int mouseY,
    double radius, int color,
    ScreenRectangle scissorArea,
    ScreenRectangle bounds
) implements GuiElementRenderState {
    public ScribbleRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        int x, int y,
        int width, int height,
        int mouseX, int mouseY,
        double radius, int color,
        ScreenRectangle scissorArea
    ) {
        this(pipeline, textureSetup, pose, x, y, width, height, mouseX, mouseY, radius, color, scissorArea, getBounds(x, y, width, height, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        var r = (float) radius;

        var mX = mouseX - x;
        var mY = mouseY - y;

        vertexConsumer.addVertexWith2DPose(this.pose(), this.x, this.y).setLineWidth(r).setUv(0, 0).setUv2(mX, mY).setUv1(width, height).setColor(color);
        vertexConsumer.addVertexWith2DPose(this.pose(), this.x, this.y + height).setLineWidth(r).setUv(0, 1).setUv2(mX, mY).setUv1(width, height).setColor(color);
        vertexConsumer.addVertexWith2DPose(this.pose(), this.x + width, this.y + height).setLineWidth(r).setUv(1, 1).setUv2(mX, mY).setUv1(width, height).setColor(color);
        vertexConsumer.addVertexWith2DPose(this.pose(), this.x + width, this.y).setLineWidth(r).setUv(1, 0).setUv2(mX, mY).setUv1(width, height).setColor(color);
    }

    @Nullable
    private static ScreenRectangle getBounds(
        final int x, final int y, final int width, final int height, final Matrix3x2fc pose, @Nullable final ScreenRectangle scissorArea
    ) {
        ScreenRectangle bounds = new ScreenRectangle(x, y, width, height).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}
