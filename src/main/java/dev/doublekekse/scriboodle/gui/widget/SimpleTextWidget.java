package dev.doublekekse.scriboodle.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class SimpleTextWidget extends AbstractWidget {
    Font font;

    public SimpleTextWidget(int x, int y, int width, int height, Font font, Component message) {
        super(x, y, width, height, message);

        this.font = font;
    }

    public SimpleTextWidget(Font font, Component message) {
        this(0, 0, font.width(message), font.lineHeight, font, message);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.text(font, getMessage(), getX(), getY() + getHeight() / 2, 0xffffffff);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {

    }
}
