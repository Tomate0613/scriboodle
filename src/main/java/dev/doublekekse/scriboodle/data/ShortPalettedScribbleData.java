package dev.doublekekse.scriboodle.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Tuple;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ShortPalettedScribbleData extends ScribbleData {
    public static final int MAX_PALETTE_SIZE = 0xffff;

    public int[] palette;
    public short[] data;

    public static final StreamCodec<ByteBuf, ShortPalettedScribbleData> STREAM_CODEC = StreamCodec.of(
        (buf, value) -> {
            buf.writeInt(value.width);
            buf.writeInt(value.height);

            VarInt.write(buf, value.palette.length);
            for (var p : value.palette) {
                buf.writeInt(p);
            }

            VarInt.write(buf, value.data.length);
            for (var d : value.data) {
                buf.writeShort(d);
            }
        },
        buf -> {
            var width = buf.readInt();
            var height = buf.readInt();

            var paletteLength = VarInt.read(buf);
            var palette = new int[paletteLength];
            for (int i = 0; i < paletteLength; i++) {
                palette[i] = buf.readInt();
            }

            var dataLength = VarInt.read(buf);
            var data = new short[dataLength];
            for (int i = 0; i < dataLength; i++) {
                data[i] = buf.readShort();
            }

            var scribbleData = new ShortPalettedScribbleData(width, height);
            scribbleData.palette = palette;
            scribbleData.data = data;

            return scribbleData;
        }
    );

    public ShortPalettedScribbleData(int width, int height) {
        super(width, height);

        palette = new int[0];
        data = new short[0];
    }

    ShortPalettedScribbleData(int width, int height, List<Integer> palette, List<Short> data) {
        super(width, height);

        this.palette = palette.stream().mapToInt(m -> m).toArray();
        this.data = new short[data.size()];
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = data.get(i);
        }
    }

    @Override
    public ScribbleData clone() {
        var palettedData = new ShortPalettedScribbleData(width, height);

        palettedData.data = new short[data.length];
        palettedData.palette = new int[palette.length];

        System.arraycopy(data, 0, palettedData.data, 0, data.length);
        System.arraycopy(palette, 0, palettedData.palette, 0, palette.length);

        return palettedData;
    }

    @Override
    public boolean isEmpty() {
        return palette.length == 1 && palette[0] == 0;
    }

    @Override
    public int get(int x, int y) {
        int idx = idx(x, y);

        for (int i = 0; i < data.length; i += 2) {
            idx -= (data[i] & MAX_PALETTE_SIZE) + 1;

            if (idx < 0) {
                return palette[(data[i + 1]) & MAX_PALETTE_SIZE];
            }
        }

        if(data.length == 0) {
            return 0;
        }

        throw new IllegalStateException();
    }

    @Override
    public void write(BufferedImage image) {
        var idx = 0;
        for (int i = 0; i < data.length; i += 2) {
            var delta = (data[i] & MAX_PALETTE_SIZE) + 1;

            for(int j = 0; j < delta; j++) {
                var pixelIndex = idx + j;
                var pos = pos(pixelIndex);
                var color = palette[(data[i + 1]) & MAX_PALETTE_SIZE];

                image.setRGB(pos.getA(), pos.getB(), color);
            }

            idx += delta;
        }
    }

    @Override
    public ScribbleData set(int x, int y, int color) {
        var raw = RawScribbleData.cloneFrom(this);
        return raw.set(x, y, color);
    }

    @Override
    public byte id() {
        return 2;
    }

    @Override
    public ScribbleData optimize() {
        return this;
    }

    private int idx(int x, int y) {
        return x * height + y;
    }

    private Tuple<Integer, Integer> pos(int idx) {
        return new Tuple<>(idx / height, idx % height);
    }

    static ShortPalettedScribbleData cloneFrom(RawScribbleData raw) {
        int count = -1;
        int prevColor = raw.colors[0][0];
        var skipNext = true;

        var data = new ArrayList<Short>();
        var palette = new ArrayList<Integer>();

        for (int x = 0; x < raw.width; x++) {
            for (int y = 0; y < raw.height; y++) {
                int color = raw.colors[x][y];

                skipNext = count == MAX_PALETTE_SIZE;
                if (prevColor == color && !skipNext) {
                    count++;
                    continue;
                }


                data.add((short) count);
                data.add(paletteIndex(palette, prevColor));
                count = 0;

                prevColor = color;
            }
        }

        data.add((short) count);
        data.add(paletteIndex(palette, prevColor));

        return new ShortPalettedScribbleData(raw.width, raw.height, palette, data);
    }

    private static short paletteIndex(List<Integer> palette, int color) {
        int paletteIndex = palette.indexOf(color);
        if (paletteIndex == -1) {
            if (palette.size() >= MAX_PALETTE_SIZE) {
                throw new IllegalStateException();
            }
            palette.add(color);
            return (short) (palette.size() - 1);
        }

        return (short) paletteIndex;
    }
}
