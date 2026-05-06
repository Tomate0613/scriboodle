package dev.doublekekse.scriboodle.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class EmptyScribbleData extends ScribbleData {
    public static StreamCodec<ByteBuf, EmptyScribbleData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, a -> a.width,
        ByteBufCodecs.INT, a -> a.height,
        EmptyScribbleData::new
    );

    public EmptyScribbleData(int width, int height) {
        super(width, height);
    }

    @Override
    public ScribbleData clone() {
        // There is no data
        return this;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int get(int x, int y) {
        return 0;
    }

    @Override
    public ScribbleData set(int x, int y, int color) {
        return new RawScribbleData(width, height).set(x, y, color);
    }

    @Override
    public byte id() {
        return 3;
    }

    @Override
    public ScribbleData optimize() {
        return this;
    }
}
