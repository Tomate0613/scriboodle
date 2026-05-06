package dev.doublekekse.scriboodle.data;

import java.util.HashMap;
import java.util.Map;

public class ScribblePatch {
    Map<Integer, ScribbleData> added = new HashMap<>();

    public ScribblePatch add(int page, ScribbleData data) {
        added.put(page, data);
        return this;
    }

    public ScribblePatch addAll(PaginatedScribbleData data) {
        var pages = data.pages();

        for (int i = 0; i < pages.size(); i++) {
            add(i, pages.get(i));
        }

        return this;
    }

    public void apply(PaginatedScribbleData paginatedScribbleData) {
        for (var entry : added.entrySet()) {
            paginatedScribbleData.set(entry.getKey(), entry.getValue());
        }
    }
}
