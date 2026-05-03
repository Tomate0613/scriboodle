package dev.doublekekse.scriboodle.gui.layout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class FlexHorizontalLayout implements Layout {
    private GridLayout wrapped;
    private int previousMaxItemsPerRow;
    private int maxItemsPerRow;
    private int nextChildIndex = 0;
    private Integer spacing;

    private record Child(LayoutElement element, LayoutSettings cellSettings) {
    }

    private final List<Child> children = new ArrayList<>();

    public FlexHorizontalLayout(int maxItemsPerRow) {
        this(0, 0, maxItemsPerRow);
    }

    public FlexHorizontalLayout(final int x, final int y, int maxItemsPerRow) {
        this.wrapped = new GridLayout(x, y);
        this.maxItemsPerRow = maxItemsPerRow;
        this.previousMaxItemsPerRow = maxItemsPerRow;
    }

    public FlexHorizontalLayout maxItemsPerRow(int size) {
        maxItemsPerRow = size;
        return this;
    }

    public FlexHorizontalLayout spacing(final int spacing) {
        wrapped.spacing(spacing);
        this.spacing = spacing;

        return this;
    }

    public LayoutSettings newCellSettings() {
        return this.wrapped.newCellSettings();
    }

    public LayoutSettings defaultCellSetting() {
        return this.wrapped.defaultCellSetting();
    }


    private <T extends LayoutElement> T internalAddChild(final T child, final LayoutSettings cellSettings) {
        var c = wrapped.addChild(child, nextChildIndex / maxItemsPerRow, nextChildIndex % maxItemsPerRow, cellSettings);
        nextChildIndex++;
        return c;
    }

    public <T extends LayoutElement> T addChild(final T child, final LayoutSettings cellSettings) {
        children.add(new Child(child, cellSettings));
        return internalAddChild(child, cellSettings);
    }

    public <T extends LayoutElement> T addChild(final T child) {
        return this.addChild(child, this.newCellSettings());
    }

    public <T extends LayoutElement> T addChild(final T child, final Consumer<LayoutSettings> layoutSettingsAdjustments) {
        var cellSettings = Util.make(this.newCellSettings(), layoutSettingsAdjustments);
        return addChild(child, cellSettings);
    }

    @Override
    public void visitChildren(final @NonNull Consumer<LayoutElement> layoutElementVisitor) {
        this.wrapped.visitChildren(layoutElementVisitor);
    }

    @Override
    public void arrangeElements() {
        if (previousMaxItemsPerRow != maxItemsPerRow) {
            nextChildIndex = 0;
            var old = wrapped;
            wrapped = new GridLayout(old.getX(), old.getY());
            if (spacing != null) {
                wrapped.spacing(spacing);
            }
            for (var c : children) {
                internalAddChild(c.element, c.cellSettings);
            }

            previousMaxItemsPerRow = maxItemsPerRow;
        }

        wrapped.arrangeElements();
    }

    @Override
    public int getWidth() {
        return wrapped.getWidth();
    }

    @Override
    public int getHeight() {
        return wrapped.getHeight();
    }

    @Override
    public void setX(final int x) {
        wrapped.setX(x);
    }

    @Override
    public void setY(final int y) {
        wrapped.setY(y);
    }

    @Override
    public int getX() {
        return wrapped.getX();
    }

    @Override
    public int getY() {
        return wrapped.getY();
    }
}
