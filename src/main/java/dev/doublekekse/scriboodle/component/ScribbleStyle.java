package dev.doublekekse.scriboodle.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.doublekekse.scriboodle.Scriboodle;
import dev.doublekekse.scriboodle.data.PaginatedScribbleData;
import dev.doublekekse.scriboodle.data.ScribbleData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record ScribbleStyle(
    Identifier backgroundTexture,
    int backgroundWidth,
    int backgroundHeight,
    int backgroundTextureWidth,
    int backgroundTextureHeight,
    int drawMarginLeft,
    int drawMarginRight,
    int drawMarginTop,
    int foregroundWidth,
    int foregroundHeight,
    int maxPageCount
) {
    private static final Identifier BOOK_LOCATION = Identifier.withDefaultNamespace("textures/gui/book.png");

    public static final ScribbleStyle BOOK = new ScribbleStyle(
        BOOK_LOCATION,
        192,
        192,
        256,
        256,
        27,
        29,
        10,
        128,
        162,
        50
    );
    public static final ScribbleStyle PAPER = new ScribbleStyle(
        Scriboodle.id("textures/gui/background/paper.png"),
        230,
        165,
        230,
        165,
        5,
        5,
        3,
        230 - 10,
        165 - 8,
        1
    );

    public static Codec<ScribbleStyle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.fieldOf("background_texture").forGetter(ScribbleStyle::backgroundTexture),
        Codec.INT.fieldOf("background_width").forGetter(ScribbleStyle::backgroundWidth),
        Codec.INT.fieldOf("background_height").forGetter(ScribbleStyle::backgroundWidth),
        Codec.INT.fieldOf("background_texture_width").forGetter(ScribbleStyle::backgroundTextureWidth),
        Codec.INT.fieldOf("background_texture_height").forGetter(ScribbleStyle::backgroundTextureWidth),
        Codec.INT.fieldOf("draw_margin_left").forGetter(ScribbleStyle::drawMarginLeft),
        Codec.INT.fieldOf("draw_margin_right").forGetter(ScribbleStyle::drawMarginRight),
        Codec.INT.fieldOf("draw_margin_top").forGetter(ScribbleStyle::drawMarginTop),
        Codec.INT.fieldOf("foreground_width").forGetter(ScribbleStyle::foregroundWidth),
        Codec.INT.fieldOf("foreground_height").forGetter(ScribbleStyle::foregroundHeight),
        Codec.INT.fieldOf("max_page_count").forGetter(ScribbleStyle::maxPageCount)
    ).apply(instance, ScribbleStyle::new));

    public static StreamCodec<ByteBuf, ScribbleStyle> STREAM_CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, ScribbleStyle::backgroundTexture,
        ByteBufCodecs.INT, ScribbleStyle::backgroundWidth,
        ByteBufCodecs.INT, ScribbleStyle::backgroundHeight,
        ByteBufCodecs.INT, ScribbleStyle::backgroundTextureWidth,
        ByteBufCodecs.INT, ScribbleStyle::backgroundTextureHeight,
        ByteBufCodecs.INT, ScribbleStyle::drawMarginLeft,
        ByteBufCodecs.INT, ScribbleStyle::drawMarginRight,
        ByteBufCodecs.INT, ScribbleStyle::drawMarginTop,
        ByteBufCodecs.INT, ScribbleStyle::foregroundWidth,
        ByteBufCodecs.INT, ScribbleStyle::foregroundHeight,
        ByteBufCodecs.INT, ScribbleStyle::maxPageCount,
        ScribbleStyle::new
    );

    public boolean validate(PaginatedScribbleData data) {
        return data.width() == foregroundWidth && data.height() == foregroundHeight && data.pages().size() <= maxPageCount && data.validate();
    }

    public boolean validate(ScribbleData data, int page) {
        return data.width == foregroundWidth && data.height == foregroundHeight && page < maxPageCount;
    }
}
