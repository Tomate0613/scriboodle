package dev.doublekekse.scriboodle.data;

import com.mojang.blaze3d.platform.NativeImage;
import dev.doublekekse.scriboodle.math.Vec2d;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.awt.image.BufferedImage;

public abstract class ScribbleData implements Cloneable {
    public final int width;
    public final int height;
    public final int area;

    public static final StreamCodec<ByteBuf, ScribbleData> STREAM_CODEC = ByteBufCodecs.BYTE.dispatch(ScribbleData::id, ScribbleData::streamCodec);

    public ScribbleData(int width, int height) {
        this.width = width;
        this.height = height;
        this.area = width * height;
    }

    @Override
    public abstract ScribbleData clone();

    public abstract boolean isEmpty();

    public abstract int get(int x, int y);

    public abstract ScribbleData set(int x, int y, int color);

    public abstract byte id();

    public abstract ScribbleData optimize();

    public boolean inBounds(int x, int y) {
        return x >= 0 &&
            y >= 0 &&
            x < width &&
            y < height;
    }

    public boolean inBounds(Vec2d pos) {
        return inBounds((int) pos.x(), (int) pos.y());
    }

    public boolean nearBounds(int x, int y, double radius) {
        return x >= 0 - radius &&
            y >= 0 - radius &&
            x < width + radius &&
            y < height + radius;
    }

    public boolean nearBounds(Vec2d pos, double radius) {
        return nearBounds((int) pos.x(), (int) pos.y(), radius);
    }

    public void write(NativeImage image) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setPixel(x, y, get(x, y));
            }
        }
    }

    public void write(BufferedImage image) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, get(x, y));
            }
        }
    }

    public static ScribbleData read(BufferedImage image) {
        ScribbleData raw = new RawScribbleData(image.getWidth(), image.getHeight());

        for (int x = 0; x < raw.width; x++) {
            for (int y = 0; y < raw.height; y++) {
                raw = raw.set(x, y, image.getRGB(x, y));
            }
        }

        return raw.optimize();
    }

    private static StreamCodec<? super ByteBuf, ? extends ScribbleData> streamCodec(byte id) {
        if (id == 1) {
            return BytePalettedScribbleData.STREAM_CODEC;
        }

        if (id == 2) {
            return ShortPalettedScribbleData.STREAM_CODEC;
        }

        if (id == 3) {
            return EmptyScribbleData.STREAM_CODEC;
        }

        return RawScribbleData.STREAM_CODEC;
    }
}
