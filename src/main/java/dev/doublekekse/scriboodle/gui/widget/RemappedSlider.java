package dev.doublekekse.scriboodle.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public abstract class RemappedSlider extends AbstractSliderButton {
    double min;
    double range;

    public RemappedSlider(int x, int y, int width, int height, double initialValue, double min, double max) {
        var range = max - min;

        super(x, y, width, height, Component.empty(), (initialValue- min) / range);

        this.min = min;
        this.range = range;
    }

    @Override
    protected void updateMessage() {

    }

    public void setRemappedValue(double value) {
        setValue((value - min) / range);
    }

    @Override
    protected void applyValue() {
        applyRemappedValue(value * range + min);
    }

    public abstract void applyRemappedValue(double value);
}
