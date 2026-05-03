package dev.doublekekse.scriboodle.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.doublekekse.scriboodle.Scriboodle;
import dev.doublekekse.scriboodle.component.ScribbleStyle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FileUtils;

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

    private final MinecraftServer server;
    public int next;
    public Map<Integer, PaginatedScribbleData> dirtyData = new HashMap<>();
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
        var dirty = dirtyData.get(id);
        if (dirty != null) {
            return dirty;
        }

        var cached = cachedData.getIfPresent(id);
        if (cached != null) {
            return cached;
        }

        try {
            var loaded = load(id, style);
            cachedData.put(id, loaded);
            return loaded;
        } catch (IOException e) {
            return null;
        }
    }

    public void set(int id, PaginatedScribbleData data) {
        dirtyData.put(id, data);
    }

    public void saveDirty() {
        var dataPath = server.getWorldPath(LevelResource.DATA);

        for (var entry : dirtyData.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var id = Scriboodle.id("paginated_" + key);
            var pagesPath = id.resolveAgainst(dataPath);
            var pages = value.pages();
            var cached = cachedData.getIfPresent(key);
            var cachedPages = cached != null ? cached.pages() : null;

            if (Files.exists(pagesPath) && !FileUtils.deleteQuietly(pagesPath.toFile())) {
                Scriboodle.LOGGER.error("Failed to clear directory");
                continue;
            }

            try {
                Files.createDirectories(pagesPath);
            } catch (IOException e) {
                Scriboodle.LOGGER.error("Failed to create directory");
                continue;
            }

            for (int i = 0; i < pages.size(); i++) {
                var page = pages.get(i);

                if (cachedPages != null && cachedPages.size() > i && cachedPages.get(i).equals(page)) {
                    continue;
                }

                var bi = new BufferedImage(page.width, page.height, BufferedImage.TYPE_INT_ARGB);
                page.write(bi);

                var pagePath = pagesPath.resolve("page_" + i + ".png");
                try {
                    ImageIO.write(bi, "png", pagePath.toFile());
                } catch (IOException e) {
                    Scriboodle.LOGGER.error("Failed to save page", e);
                }
            }

            cachedData.put(key, value);
        }

        dirtyData.clear();
    }

    private PaginatedScribbleData load(int key, ScribbleStyle style) throws IOException {
        var dataPath = server.getWorldPath(LevelResource.DATA);
        var id = Scriboodle.id("paginated_" + key);
        var pagesPath = id.resolveAgainst(dataPath);

        var width = style.foregroundWidth();
        var height = style.foregroundHeight();

        var list = new ArrayList<ScribbleData>();
        try (var pagesStream = Files.newDirectoryStream(pagesPath)) {
            for (var entry : pagesStream) {
                if (!entry.getFileName().toString().endsWith(".png")) {
                    continue;
                }

                var is = Files.newInputStream(entry, StandardOpenOption.READ);
                var bi = ImageIO.read(is);

                if (bi.getWidth() != width || bi.getHeight() != height) {
                    Scriboodle.LOGGER.error("Image has incorrect dimensions, skipping");
                    continue;
                }

                var raw = ScribbleData.read(bi);
                list.add(raw);
            }
        }

        Scriboodle.LOGGER.info(String.valueOf(list));
        return new PaginatedScribbleData(width, height, list);
    }

    public int create(ScribbleStyle style) {
        var data = new PaginatedScribbleData(style.foregroundWidth(), style.foregroundHeight(), new ArrayList<>());
        next++;

        set(next, data);
        return next;
    }
}
