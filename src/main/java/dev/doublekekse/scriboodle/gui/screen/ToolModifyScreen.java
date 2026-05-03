package dev.doublekekse.scriboodle.gui.screen;

import dev.doublekekse.scriboodle.ColorUtils;
import dev.doublekekse.scriboodle.Scriboodle;
import dev.doublekekse.scriboodle.data.RawScribbleData;
import dev.doublekekse.scriboodle.data.ScribbleData;
import dev.doublekekse.scriboodle.gui.widget.ChangeableSlider;
import dev.doublekekse.scriboodle.gui.widget.RemappedSlider;
import dev.doublekekse.scriboodle.math.Vec2d;
import dev.doublekekse.scriboodle.pen.PenApi;
import dev.doublekekse.scriboodle.tools.*;
import dev.doublekekse.scriboodle.tools.Shape;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.function.Consumer;

public class ToolModifyScreen extends Screen {
    Consumer<Tool> callback;

    Tool tool;
    boolean requiresUpload = false;
    int color;
    int radiusIndex;
    ScrollableLayout scrollLayout;
    LinearLayout mainLayout;
    Preview mainPreview;
    SpacingSlider spacingSlider;

    protected ToolModifyScreen(Consumer<Tool> callback, Tool tool, int color, int radiusIndex) {
        super(Component.translatable("scriboodle.screen.modify_tool"));

        this.callback = callback;
        this.tool = tool;
        this.color = color;
        this.radiusIndex = radiusIndex;
    }

    @Override
    protected void init() {
        super.init();

        requiresUpload = true;

        mainLayout = LinearLayout.horizontal().spacing(5);
        var toolsLayout = LinearLayout.vertical().spacing(10);
        toolsLayout.addChild(new SpacerElement(5, 5));

        toolsLayout.defaultCellSetting().alignHorizontallyCenter();

        toolsLayout.addChild(shapes());
        toolsLayout.addChild(patterns());
        toolsLayout.addChild(dynamics());
        toolsLayout.addChild(radii());
        toolsLayout.addChild(spacing());

        toolsLayout.addChild(new OpacitySlider(5 * 65, 10));
        toolsLayout.addChild(new SpacerElement(5, 5));

        toolsLayout.arrangeElements();
        scrollLayout = new ScrollableLayout(minecraft, toolsLayout, height);

        mainPreview = new Preview(0, 0, previewSize(), previewSize(), "main");
        mainLayout.addChild(scrollLayout);
        mainLayout.addChild(mainPreview, LayoutSettings::alignVerticallyMiddle);

        mainLayout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    private LinearLayout shapes() {
        var layout = LinearLayout.horizontal().spacing(5);

        layout.addChild(new SelectShapeButton(Shape.CIRCLE, "circle"));
        layout.addChild(new SelectShapeButton(Shape.SQUARE_ROTATED, "square_rotated"));
        layout.addChild(new SelectShapeButton(Shape.SPLOT, "splot"));
        layout.addChild(new SelectShapeButton(Shape.SPLOT_OTHER, "splot_other"));
        layout.addChild(new SelectShapeButton(Shape.BEANS, "beans"));

        return layout;
    }

    private LinearLayout patterns() {
        var layout = LinearLayout.horizontal().spacing(5);

        layout.addChild(new SelectPatternButton(Pattern.DIRECT, "direct"));
        layout.addChild(new SelectPatternButton(Pattern.SOFT, "soft"));
        layout.addChild(new SelectPatternButton(Pattern.ROUGH, "rough"));

        return layout;
    }

    private LinearLayout dynamics() {
        var layout = LinearLayout.horizontal().spacing(5);

        layout.addChild(new SelectDynamicsButton(Dynamics.DIRECT, "direct"));

        if (PenApi.getInstance().hasPen()) {
            layout.addChild(new SelectDynamicsButton(Dynamics.PRESSURE_OPACITY, "pressure_opacity"));
            layout.addChild(new SelectDynamicsButton(Dynamics.PRESSURE_RADIUS, "pressure_radius"));
        }

        layout.addChild(new SelectDynamicsButton(Dynamics.DISTANCE_A, "distance_a"));
        layout.addChild(new SelectDynamicsButton(Dynamics.DISTANCE_B, "distance_b"));

        return layout;
    }

    private LinearLayout radii() {
        var layout = LinearLayout.horizontal().spacing(5);

        layout.addChild(new SelectRadiiButton(Tool.RADII_SMALL, "small"));
        layout.addChild(new SelectRadiiButton(Tool.RADII_BIG, "big"));

        return layout;
    }

    private LinearLayout spacing() {
        var layout = LinearLayout.vertical().spacing(5);
        var presetsLayout = LinearLayout.horizontal().spacing(5);

        presetsLayout.addChild(new SelectPenDistanceButton(.1));
        presetsLayout.addChild(new SelectPenDistanceButton(.2));
        presetsLayout.addChild(new SelectPenDistanceButton(.3));
        presetsLayout.addChild(new SelectPenDistanceButton(.35));
        presetsLayout.addChild(new SelectPenDistanceButton(.5));

        spacingSlider = new SpacingSlider(5 * 65, 10);

        layout.addChild(presetsLayout);
        layout.addChild(spacingSlider);

        return layout;
    }

    int previewSize() {
        return Math.max(Math.min(width - scrollLayout.getWidth() - 15, height), 40);
    }

    @Override
    protected void repositionElements() {

        scrollLayout.setMaxHeight(height);
        scrollLayout.arrangeElements();
        mainPreview.setSize(previewSize(), previewSize());
        mainLayout.arrangeElements();
        requiresUpload = true;
        FrameLayout.centerInRectangle(mainLayout, getRectangle());
    }

    class Preview extends AbstractWidget {
        final Identifier scribbleId;
        DynamicTexture texture;

        public Preview(int x, int y, int width, int height, String id) {
            super(x, y, width, height, Component.empty());

            scribbleId = Scriboodle.id("preview_" + id);
            tex();
        }

        @Override
        public void setSize(int width, int height) {
            super.setSize(width, height);

            tex();
        }

        void tex() {
            if (texture != null) {
                texture.close();
            }
            texture = new DynamicTexture(scribbleId::getPath, getWidth(), getHeight(), true);
            minecraft.getTextureManager().register(scribbleId, texture);
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, getBackgroundColor());
            updateTextureIfNeeded();
            graphics.blit(RenderPipelines.GUI_TEXTURED, scribbleId, getX(), getY(), 0, 0, width, height, width, height, 0xffffffff);
        }

        protected ScribbleData data() {
            var width = texture.getPixels().getWidth();
            var height = texture.getPixels().getHeight();

            var access = new CanvasAccess.Simple(new RawScribbleData(width, height), getBackgroundColor());

            Vec2d prev = null;
            double distance = 0;

            for (double i = 0; i < 1; i += 0.01) {
                var r = i * .5;
                var pressure = ((1 - ((i * i * i))) * (i)) * 2;
                var tX = (Math.sin(i * Math.TAU) * r) + 0.6;
                var tY = (Math.cos(i * Math.TAU) * r) + 0.4;

                Vec2d to = new Vec2d(tX * width, tY * height);
                if (i == 0) {
                    prev = to;
                    continue;
                }

                distance += prev.distanceTo(to);

                prev = tool.drawStroke(access, prev, to, tool.radius(radiusIndex, pressure), pressure, distance, color);
            }

            return access.data;
        }


        private void updateTextureIfNeeded() {
            if (requiresUpload) {
                var pixels = this.texture.getPixels();
                var data = data();
                data.write(pixels);
                texture.upload();
            }
        }

        @Override
        protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
        }


        int getBackgroundColor() {
            if (ColorUtils.isLight(color)) {
                return 0xff000000;
            }

            return 0xffffffff;
        }
    }

    abstract class ToolSettingButton extends AbstractButton {
        final Identifier scribbleId;
        final DynamicTexture texture;
        Tool previousTool;

        protected ToolSettingButton(String id) {
            super(0, 0, 60, 60, Component.empty());

            scribbleId = Scriboodle.id("scribble_tool_setting_" + id);
            texture = new DynamicTexture(() -> "Scribble_tool_setting_" + id, getWidth(), getHeight(), true);
            minecraft.getTextureManager().register(scribbleId, texture);
        }

        int getBackgroundColor() {
            if (ColorUtils.isLight(color)) {
                if (isUsed()) {
                    return 0xff000000;
                }

                return 0xff222222;
            }

            if (isUsed()) {
                return 0xffffffff;
            }

            return 0xdddddddd;
        }

        @Override
        protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, getBackgroundColor());

            updateTextureIfNeeded();
            graphics.blit(RenderPipelines.GUI_TEXTURED, scribbleId, getX(), getY(), 0, 0, width, height, width, height, 0xffffffff);

            if (isUsed()) {
                graphics.outline(getX(), getY(), width, height, ARGB.color(0xff, DyeColor.RED.getFireworkColor()));
            }
        }

        @Override
        public void onPress(@NonNull InputWithModifiers input) {
            tool = applySetting(tool);
            requiresUpload = true;
        }

        protected ScribbleData data() {
            var tool = applySetting(previewTool());

            if (tool.equals(previousTool)) {
                return null;
            }

            previousTool = tool;
            var access = new CanvasAccess.Simple(new RawScribbleData(width, height), getBackgroundColor());

            Vec2d prev = null;
            double distance = 0;

            for (double i = 0; i < 1; i += 0.01) {
                var r = i * .5;
                var pressure = ((1 - ((i * i * i))) * (i)) * 2;
                var tX = (Math.sin(i * Math.TAU) * r) + 0.6;
                var tY = (Math.cos(i * Math.TAU) * r) + 0.4;

                if (i == 0) {
                    prev = new Vec2d(tX * width, tY * height);
                    continue;
                }

                var to = new Vec2d(tX * width, tY * height);
                distance += prev.distanceTo(to);

                prev = tool.drawStroke(access, prev, to, tool.radius(2, pressure), pressure, distance, color);
            }

            return access.data;
        }

        private void updateTextureIfNeeded() {
            if (requiresUpload && !this.texture.getPixels().isClosed()) {
                var pixels = this.texture.getPixels();
                var data = data();

                if (data == null) {
                    return;
                }

                data.write(pixels);

                texture.upload();
            }
        }

        abstract boolean isUsed();

        abstract Tool applySetting(Tool tool);

        abstract Tool previewTool();

        @Override
        protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {

        }
    }

    class SelectPatternButton extends ToolSettingButton {
        Pattern pattern;

        SelectPatternButton(Pattern pattern, String id) {
            super("pattern_" + id);

            this.pattern = pattern;
        }

        @Override
        boolean isUsed() {
            return tool.pattern().equals(pattern);
        }

        @Override
        Tool applySetting(Tool tool) {
            return tool.with(pattern);
        }

        @Override
        Tool previewTool() {
            return tool.with(Tool.RADII_BIG);
        }
    }


    class SelectShapeButton extends ToolSettingButton {
        Shape shape;

        SelectShapeButton(Shape shape, String id) {
            super("shape_" + id);

            this.shape = shape;
        }

        @Override
        boolean isUsed() {
            return tool.shape().equals(shape);
        }

        @Override
        Tool applySetting(Tool tool) {
            return tool.with(shape);
        }

        @Override
        Tool previewTool() {
            return tool.with(Tool.RADII_BIG);
        }
    }

    class SelectDynamicsButton extends ToolSettingButton {
        Dynamics dynamics;

        SelectDynamicsButton(Dynamics dynamics, String id) {
            super("dynamics_" + id);

            this.dynamics = dynamics;
        }

        @Override
        boolean isUsed() {
            return tool.dynamics().equals(dynamics);
        }

        @Override
        Tool applySetting(Tool tool) {
            return tool.with(dynamics);
        }

        @Override
        Tool previewTool() {
            return tool.with(Tool.RADII_SMALL);
        }
    }

    class SelectRadiiButton extends ToolSettingButton {
        double[] radii;

        SelectRadiiButton(double[] radii, String id) {
            super("radii_" + id);

            this.radii = radii;
        }

        @Override
        boolean isUsed() {
            return Arrays.equals(tool.radii(), radii);
        }

        @Override
        Tool applySetting(Tool tool) {
            return tool.with(radii);
        }

        @Override
        Tool previewTool() {
            return tool;
        }
    }

    class SelectPenDistanceButton extends ToolSettingButton {
        double penDistance;

        SelectPenDistanceButton(double penDistance) {
            super("pen_distance_" + penDistance);

            this.penDistance = penDistance;
        }

        @Override
        public void onPress(@NonNull InputWithModifiers input) {
            super.onPress(input);
            spacingSlider.setRemappedValue(penDistance);
        }

        @Override
        boolean isUsed() {
            return tool.spacing() == penDistance;
        }

        @Override
        Tool applySetting(Tool tool) {
            return tool.withSpacing(penDistance);
        }

        @Override
        Tool previewTool() {
            return tool;
        }
    }

    class SpacingSlider extends RemappedSlider {
        public SpacingSlider(int width, int height) {
            super(0, 0, width, height, tool.spacing(), 0.1, .5);
        }

        @Override
        public void applyRemappedValue(double value) {
            tool = tool.withSpacing(value);
            requiresUpload = true;
        }
    }

    class OpacitySlider extends ChangeableSlider {
        public OpacitySlider(int width, int height) {
            super(0, 0, width, height, tool.opacity());
        }

        @Override
        protected void applyValue() {
            tool = tool.withOpacity(Math.max(0.01, value));
            requiresUpload = true;
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        requiresUpload = false;
    }

    @Override
    public void onClose() {
        callback.accept(tool);
    }
}
