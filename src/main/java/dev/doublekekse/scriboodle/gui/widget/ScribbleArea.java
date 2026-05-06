package dev.doublekekse.scriboodle.gui.widget;

import dev.doublekekse.scriboodle.ColorUtils;
import dev.doublekekse.scriboodle.Scriboodle;
import dev.doublekekse.scriboodle.registry.ScriboodleCursorTypes;
import dev.doublekekse.scriboodle.data.ScribbleData;
import dev.doublekekse.scriboodle.duck.GuiGraphicsExtractorDuck;
import dev.doublekekse.scriboodle.math.Vec2d;
import dev.doublekekse.scriboodle.pen.PenApi;
import dev.doublekekse.scriboodle.registry.ScriboodleSoundEvents;
import dev.doublekekse.scriboodle.tools.CanvasAccess;
import dev.doublekekse.scriboodle.tools.Tool;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import org.joml.Math;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ScribbleArea extends AbstractWidget implements CanvasAccess {
    public static final List<Tool> DEFAULT_TOOLS = List.of(
        Tool.FEATHER,
        PenApi.getInstance().hasPen()
            ? Tool.WATER_PEN
            : Tool.WATER
    );

    private static final double VOLUME_BASE = 0.2;
    private static final double VOLUME_FACTOR = 0.005;
    private static final double DEFAULT_PRESSURE = .5;
    private static final int RADII_COUNT = 5;
    private static final Identifier SCRIBBLE_ID = Scriboodle.id("scribble");

    final TextureManager textureManager;
    final SoundManager soundManager;
    final PenApi penApi = PenApi.getInstance();
    public ScribbleData data;
    public boolean drawing;

    boolean requiresUpload = true;
    public boolean hasBeenModified = false;
    final DynamicTexture texture;

    Vec2d previous;
    double distance;
    int duration;
    double maxPressure;

    public List<DyeColor> colors = new ArrayList<>() {{
        add(DyeColor.BLACK);
    }};

    public ColorMode colorMode = ColorMode.FIREWORK;
    public int realColor = colorMode.apply(DyeColor.BLACK);

    public static List<Tool> tools = new ArrayList<>(DEFAULT_TOOLS);
    public static int toolIndex;
    public static int radiusIndex = 1;

    private int opacityNotificationTimer;

    public enum ColorMode {
        FIREWORK,
        TEXT;

        public ColorMode next() {
            return switch (this) {
                case FIREWORK -> TEXT;
                case TEXT -> FIREWORK;
            };
        }

        int apply(DyeColor color) {
            return switch (this) {
                case FIREWORK -> color == DyeColor.YELLOW ? color.getTextureDiffuseColor() : color.getFireworkColor();
                case TEXT -> color.getTextColor();
            };
        }
    }


    public ScribbleArea(int x, int y, ScribbleData data, TextureManager textureManager, SoundManager soundManager) {
        super(x, y, data.width, data.height, Component.empty());

        texture = new DynamicTexture(() -> "Scribble", data.width, data.height, true);
        textureManager.register(SCRIBBLE_ID, texture);

        this.textureManager = textureManager;
        this.soundManager = soundManager;
        this.data = data;
    }

    @Override
    public int get(int x, int y) {
        if (data.inBounds(x, y)) {
            var d = data.get(x, y);
            if (ARGB.alpha(d) == 0) {
                return 0x00ffffff;
            }

            return d;
        }

        return 0x00ffffff;
    }

    @Override
    public void set(int x, int y, int color) {
        if (data.inBounds(x, y)) {
            data = data.set(x, y, color);
            requiresUpload = true;
            hasBeenModified = true;
        }
    }


    public static void increaseRadius(boolean loop) {
        radiusIndex++;

        if (loop) {
            radiusIndex %= RADII_COUNT;
        } else if (radiusIndex >= RADII_COUNT) {
            radiusIndex = RADII_COUNT - 1;
        }
    }

    public static void decreaseRadius(boolean loop) {
        radiusIndex--;

        if (radiusIndex < 0) {
            if (loop) {
                radiusIndex = RADII_COUNT - 1;
            } else {
                radiusIndex = 0;
            }
        }
    }

    public void updateColor() {
        var c = this.colors.stream().map(colorMode::apply).toList();
        realColor = ColorUtils.mix(c);
    }

    private boolean mouseNearBounds(double x, double y, double radius) {
        var mX = (x - getX());
        var mY = (y - getY());

        return data.nearBounds((int) mX, (int) mY, radius);
    }


    double radius() {
        var pressure = penApi.getPressureOrDefault(DEFAULT_PRESSURE);
        var tool = tools.get(toolIndex);
        return tool.radius(radiusIndex, pressure);
    }


    void mouseDraw(Vec2d pos) {
        var pressure = penApi.getPressureOrDefault(DEFAULT_PRESSURE);

        if (!drawing) {
            previous = pos;
            distance = 0;
            duration = 0;
            maxPressure = pressure;
        }

        var dist = previous.distanceTo(pos);
        distance += dist;

        var tool = tool();
        var radius = tool.radius(radiusIndex, pressure);

        if (data.nearBounds(pos, radius)) {
            soundManager.play(SimpleSoundInstance.forUI(ScriboodleSoundEvents.SCRIBBLE, (float) (Math.random() + 0.7), (float) (VOLUME_BASE + VOLUME_FACTOR * dist)));
        }

        previous = tool.drawStroke(this, previous, pos, radius, pressure, distance, realColor);
    }

    Tool tool() {
        return tools.get(toolIndex);
    }

    public void extractCustomState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHoveringChild) {
        updateTextureIfNeeded();

        double radius = 0;

        if (drawing || !isHoveringChild) {
            radius = radius();

            if (mouseNearBounds(mouseX, mouseY, radius)) {
                graphics.requestCursor(ScriboodleCursorTypes.HIDDEN);
            }
        }


        var color = 0x00000000;
        if (opacityNotificationTimer > 0) {
            color = ARGB.color((int) (tool().opacity() * 0xff), realColor);
        }

        ((GuiGraphicsExtractorDuck) graphics).scriboodle$scribble(texture, getX(), getY(), data.width, data.height, mouseX, mouseY, radius, color);
    }

    private void updateTextureIfNeeded() {
        if (this.requiresUpload) {
            var pixels = this.texture.getPixels();
            data.write(pixels);
            this.texture.upload();
            this.requiresUpload = false;
        }
    }

    Vec2d bufferVec(double x, double y) {
        return new Vec2d(x - getX(), y - getY());
    }

    @Override
    public void mouseMoved(double x, double y) {
        if (drawing) {
            mouseDraw(bufferVec(x, y));
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        mouseDraw(bufferVec(event.x(), event.y()));
        drawing = true;

        return true;
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        if (drawing && duration < 5 && distance < 1) {
            var pressure = maxPressure;
            var tool = tool();
            var radius = tool.radius(radiusIndex, pressure);

            tool.stamp(this, previous, radius, pressure, realColor);
        }

        drawing = false;
        return true;
    }

    public void nextColorMode() {
        colorMode = colorMode.next();
        updateColor();
    }

    public void dispose() {
        textureManager.release(SCRIBBLE_ID);
    }

    public void tick() {
        opacityNotificationTimer--;

        if (drawing) {
            var pressure = penApi.getPressureOrDefault(DEFAULT_PRESSURE);
            maxPressure = Math.max(maxPressure, pressure);

            duration++;
        }
    }

    public void setData(ScribbleData data) {
        this.data = data;
        requiresUpload = true;
        hasBeenModified = false;
    }

    public void resetTool(int toolIndex) {
        tools.set(toolIndex, DEFAULT_TOOLS.get(toolIndex));
    }

    public void changeToolOpacity(double opacityDelta) {
        var tool = tool();
        var opacity = tool.opacity() + opacityDelta;
        tools.set(toolIndex, tool.withOpacity(opacity));

        opacityNotificationTimer = 20;
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {

    }

    @Override
    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }
}
