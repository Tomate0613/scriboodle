package dev.doublekekse.scriboodle.data;

import dev.doublekekse.scriboodle.Scriboodle;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.network.codec.StreamCodec;

public class RawScribbleData extends ScribbleData {
    final int[][] colors;

    public RawScribbleData(int width, int height) {
        super(width, height);
        this.colors = new int[width][height];
    }

    public static final StreamCodec<ByteBuf, RawScribbleData> STREAM_CODEC = StreamCodec.of((buf, data) -> {
        buf.writeInt(data.width);
        buf.writeInt(data.height);

        for (int x = 0; x < data.width; x++) {
            for (int y = 0; y < data.height; y++) {
                buf.writeInt(data.colors[x][y]);
            }
        }
    }, (buf) -> {
        var data = new RawScribbleData(buf.readInt(), buf.readInt());

        for (int x = 0; x < data.width; x++) {
            for (int y = 0; y < data.height; y++) {
                data.colors[x][y] = buf.readInt();
            }
        }

        return data;
    });

    @Override
    public RawScribbleData clone() {
        var data = new RawScribbleData(width, height);

        for (int x = 0; x < width; x++) {
            if (height >= 0) System.arraycopy(colors[x], 0, data.colors[x], 0, height);
        }

        return data;
    }

    public static RawScribbleData cloneFrom(ScribbleData other) {
        var data = new RawScribbleData(other.width, other.height);

        for (int x = 0; x < other.width; x++) {
            for (int y = 0; y < data.height; y++) {
                data.colors[x][y] = other.get(x, y);
            }
        }

        return data;
    }

    @Override
    public ScribbleData optimize() {
        var set = new IntOpenHashSet();
        int count = 0;

        int runs = 1;
        int prev = colors[0][0];

        set.add(prev);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int c = colors[x][y];
                if (c != prev) {
                    runs++;
                    prev = c;

                    if (set.add(c)) {
                        count++;
                    }
                }
            }
        }

        int rawSize = width * height * Integer.BYTES;
        int byteSize = count * Integer.BYTES + runs * 2;
        int shortSize = count * Integer.BYTES + runs * 3;

        if (count < BytePalettedScribbleData.MAX_PALETTE_SIZE && rawSize > byteSize) {
            Scriboodle.LOGGER.info("Optimizing to byte paletted");
            return BytePalettedScribbleData.cloneFrom(this);
        } else if (count < ShortPalettedScribbleData.MAX_PALETTE_SIZE && rawSize > shortSize) {
            Scriboodle.LOGGER.info("Optimizing to short paletted");
            return ShortPalettedScribbleData.cloneFrom(this);
        }

        Scriboodle.LOGGER.info("Not optimizing");
        return this;
    }

    public boolean isEmpty() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (colors[x][y] != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int get(int x, int y) {
        return colors[x][y];
    }

    @Override
    public ScribbleData set(int x, int y, int color) {
        colors[x][y] = color;
        return this;
    }

    @Override
    public byte id() {
        return 0;
    }
}
