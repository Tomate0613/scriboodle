package dev.doublekekse.scriboodle.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record PaginatedScribbleData(int width, int height, List<ScribbleData> pages) implements Cloneable {
    public static StreamCodec<FriendlyByteBuf, PaginatedScribbleData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, PaginatedScribbleData::width,
        ByteBufCodecs.INT, PaginatedScribbleData::height,
        ScribbleData.STREAM_CODEC.apply(ByteBufCodecs.list(100)), PaginatedScribbleData::pages,
        PaginatedScribbleData::new
    );

    @Override
    public PaginatedScribbleData clone() {
        return new PaginatedScribbleData(width, height, new ArrayList<>(pages.stream().map(ScribbleData::clone).toList()));
    }

    public ScribbleData firstPage() {
        return get(0);
    }

    public ScribbleData get(int index) {
        if (index >= pages.size()) {
            return new BytePalettedScribbleData(width, height);
        } else {
            return pages.get(index);
        }
    }

    public void set(int index, ScribbleData data) {
        while (pages.size() <= index) {
            pages.add(new EmptyScribbleData(width, height));
        }

        pages.set(index, data.optimize());
    }

    public boolean validate() {
        for (var page : pages) {
            if (page.width != width || page.height != height) {
                return false;
            }
        }

        return true;
    }

    public PaginatedScribbleData with(@Nullable ScribblePatch patch) {
        if (patch == null) {
            return this;
        }

        return patch.withPatches(this);
    }
}
