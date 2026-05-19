package dev.doublekekse.scriboodle.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScribblePatch {
    /**
     * key refers to page here
     */
    Map<Integer, PatchEntry> added = new HashMap<>();
    boolean deleted = false;

    public record PatchEntry(ScribbleData data, UUID author) {
    }

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

    public ScribblePatch delete() {
        deleted = true;
        added.clear();

        return this;
    }

    public PaginatedScribbleData withPatches(PaginatedScribbleData paginatedScribbleData) {
        if (deleted) {
            paginatedScribbleData = new PaginatedScribbleData(paginatedScribbleData.width(), paginatedScribbleData.height(), new ArrayList<>());
        } else {
            paginatedScribbleData = paginatedScribbleData.clone();
        }

        for (var entry : added.entrySet()) {
            paginatedScribbleData.set(entry.getKey(), entry.getValue().data);
        }

        return paginatedScribbleData;
    }
}
