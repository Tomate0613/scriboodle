package dev.doublekekse.scriboodle.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScribblePatch {
    Map<Integer, PatchEntry> added = new HashMap<>();

    public record PatchEntry(ScribbleData data, UUID author) {}

    public ScribblePatch add(int page, ScribbleData data, UUID author) {
        added.put(page, new PatchEntry(data, author));
        return this;
    }

    public ScribblePatch addAll(PaginatedScribbleData data, UUID author) {
        var pages = data.pages();

        for (int i = 0; i < pages.size(); i++) {
            add(i, pages.get(i), author);
        }

        return this;
    }

    public void apply(PaginatedScribbleData paginatedScribbleData) {
        for (var entry : added.entrySet()) {
            paginatedScribbleData.set(entry.getKey(), entry.getValue().data);
        }
    }
}
