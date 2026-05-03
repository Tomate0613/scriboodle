package dev.doublekekse.scriboodle.gui.widget.button;


import dev.doublekekse.scriboodle.gui.widget.ScribbleArea;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ScribbleItemButton extends Button {
    ScribbleArea scribbleArea;
    ItemStack stack;

    public ScribbleItemButton(int x, int y, Component message, OnPress onPress, ItemStack stack, ScribbleArea scribbleArea) {
        super(x, y, 16, 16, message, onPress, Button.DEFAULT_NARRATION);

        this.stack = stack;
        this.scribbleArea = scribbleArea;
    }

    int outlineColor() {
        return scribbleArea.realColor;
    }

    protected boolean outlineVisible() {
        return false;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.fakeItem(stack, getX(), getY());

        if (outlineVisible()) {
            graphics.outline(getX() - 1, getY() - 1, width + 2, height + 2, outlineColor());
        }
    }
}
