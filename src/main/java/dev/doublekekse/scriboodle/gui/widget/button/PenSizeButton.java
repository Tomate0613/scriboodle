package dev.doublekekse.scriboodle.gui.widget.button;


import dev.doublekekse.scriboodle.Scriboodle;
import dev.doublekekse.scriboodle.gui.widget.ScribbleArea;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class PenSizeButton extends Button {
    private static final Identifier SPRITE = Scriboodle.id("textures/gui/widget/pen_size.png");

    public PenSizeButton(int x, int y, Component message) {
        //noinspection DataFlowIssue
        super(x, y, 16, 16, message, null, Button.DEFAULT_NARRATION);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (!input.hasShiftDown()) {
            ScribbleArea.increaseRadius(true);
        } else {
            ScribbleArea.decreaseRadius(true);
        }
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            SPRITE,
            getX(),
            getY(),
            ScribbleArea.radiusIndex * 16,
            0,
            16,
            16,
            16 * 5,
            16,
            0xffffffff
        );
    }
}
