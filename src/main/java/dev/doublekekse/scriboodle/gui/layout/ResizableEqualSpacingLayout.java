package dev.doublekekse.scriboodle.gui.layout;

import net.minecraft.client.gui.layouts.EqualSpacingLayout;

public class ResizableEqualSpacingLayout extends EqualSpacingLayout {
    public ResizableEqualSpacingLayout(int width, int height, Orientation orientation) {
        super(width, height, orientation);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
