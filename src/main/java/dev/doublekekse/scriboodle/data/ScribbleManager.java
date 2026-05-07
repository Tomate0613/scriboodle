package dev.doublekekse.scriboodle.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.doublekekse.scriboodle.Scriboodle;
import dev.doublekekse.scriboodle.component.ScribbleStyle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ScribbleManager {
    private static final Pattern PAGINATED_PATTERN = Pattern.compile("paginated_(\\d+)");
    private static final Pattern PAGE_PATTERN = Pattern.compile("page_(\\d+).png");

    private final MinecraftServer server;
    public int next;
    public Map<Integer, ScribblePatch> dirtyData = new HashMap<>();
    public Cache<Integer, PaginatedScribbleData> cachedData = CacheBuilder
        .newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build();

    public ScribbleManager(MinecraftServer server) {
        var dataPath = server.getWorldPath(LevelResource.DATA).resolve(Scriboodle.MOD_ID);
        this.server = server;

        if (!Files.exists(dataPath)) {
            return;
        }

        try (var paginatedPath = Files.newDirectoryStream(dataPath)) {
            for (var entry : paginatedPath) {
                var filename = entry.getFileName().toString();
                var matcher = PAGINATED_PATTERN.matcher(filename);

                if (matcher.find()) {
                    int number = Integer.parseInt(matcher.group(1));
                    next = Math.max(next, number);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PaginatedScribbleData get(int id, ScribbleStyle style) {
        var patch = dirtyData.get(id);

        var cached = cachedData.getIfPresent(id);
        if (cached != null) {
            return cached.with(patch);
        }

        try {
            var loaded = load(id, style);
            cachedData.put(id, loaded);
            return loaded.with(patch);
        } catch (IOException e) {
            return null;
        }
    }

    public void set(int id, int page, ScribbleData data) {
        dirtyData.compute(id, (_, v) -> (v != null ? v : new ScribblePatch()).add(page, data));
    }

    public void setAll(int id, PaginatedScribbleData data) {
        dirtyData.compute(id, (_, v) -> (v != null ? v : new ScribblePatch()).addAll(data));
    }

    public void saveDirty() {
        var dataPath = server.getWorldPath(LevelResource.DATA);

        for (var entry : dirtyData.entrySet()) {
            var key = entry.getKey();
            var patch = entry.getValue();
            var id = Scriboodle.id("paginated_" + key);
            var pagesPath = id.resolveAgainst(dataPath);
            var addedPages = patch.added;

            try {
                Files.createDirectories(pagesPath);
            } catch (IOException e) {
                Scriboodle.LOGGER.error("Failed to create directory");
                continue;
            }

            for (var page : addedPages.entrySet()) {
                var data = page.getValue();

                var bi = new BufferedImage(data.width, data.height, BufferedImage.TYPE_INT_ARGB);
                data.write(bi);

                var pagePath = pagesPath.resolve("page_" + page.getKey() + ".png");
                try {
                    ImageIO.write(bi, "png", pagePath.toFile());
                } catch (IOException e) {
                    Scriboodle.LOGGER.error("Failed to save page", e);
                }
            }

            var oldCached = cachedData.getIfPresent(key);
            if (oldCached != null) {
                cachedData.put(key, oldCached.with(patch));
            }
        }

        dirtyData.clear();
    }

    private PaginatedScribbleData load(int key, ScribbleStyle style) throws IOException {
        var dataPath = server.getWorldPath(LevelResource.DATA);
        var id = Scriboodle.id("paginated_" + key);
        var pagesPath = id.resolveAgainst(dataPath);

        var width = style.foregroundWidth();
        var height = style.foregroundHeight();

        var data = new PaginatedScribbleData(width, height, new ArrayList<>());

        if (!Files.exists(pagesPath)) {
            return data;
        }

        try (var pagesStream = Files.newDirectoryStream(pagesPath)) {
            for (var entry : pagesStream) {
                var filename = entry.getFileName().toString();
                var matcher = PAGE_PATTERN.matcher(filename);
                if (!matcher.find()) {
                    continue;
                }

                var is = Files.newInputStream(entry, StandardOpenOption.READ);
                var bi = ImageIO.read(is);

                if (bi.getWidth() != width || bi.getHeight() != height) {
                    Scriboodle.LOGGER.error("Image has incorrect dimensions, skipping");
                    continue;
                }

                var raw = ScribbleData.read(bi);
                data.set(Integer.parseInt(matcher.group(1)), raw);
            }
        }

        Scriboodle.LOGGER.info("Loaded {} pages", data.pages().size());
        return data;
    }

    public int reserve() {
        next++;
        // need to create empty scribble patch so we load the correct next
        dirtyData.put(next, new ScribblePatch());
        return next;
    }
}
