package dev.doublekekse.scriboodle.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public abstract class ChangeableSlider extends AbstractSliderButton {
    public ChangeableSlider(int x, int y, int width, int height, double initialValue) {
        super(x, y, width, height, Component.empty(), initialValue);
    }

    @Override
    public void setValue(double newValue) {
        super.setValue(newValue);
    }

    @Override
    protected void updateMessage() {

    }
}
