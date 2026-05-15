package dev.doublekekse.scriboodle.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.doublekekse.scriboodle.Scriboodle;
import dev.doublekekse.scriboodle.component.ScribbleStyle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FileUtils;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ScribbleManager {
    private static final Pattern PAGINATED_PATTERN = Pattern.compile("paginated_(\\d+)");
    private static final Pattern PAGE_PATTERN = Pattern.compile("page_(\\d+).png");

    private final MinecraftServer server;
    public int next;
    public Map<Integer, ScribblePatch> dirtyData = new HashMap<>();
    public Cache<Integer, PaginatedScribbleData> cachedData = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

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

    public void set(int id, int page, ScribbleData data, UUID author) {
        dirtyData.compute(id, (_, v) -> (v != null ? v : new ScribblePatch()).add(page, data, author));
    }

    public void setAll(int id, PaginatedScribbleData data, UUID author) {
        dirtyData.compute(id, (_, v) -> (v != null ? v : new ScribblePatch()).addAll(data, author));
    }

    private static void writeWithAuthor(RenderedImage ri, File output, String author) throws IOException {
        var writers = ImageIO.getImageWritersByFormatName("png");

        if (!writers.hasNext()) {
            throw new IllegalStateException("No PNG writer found");
        }

        var writer = writers.next();
        var writeParam = writer.getDefaultWriteParam();

        var typeSpecifier = ImageTypeSpecifier.createFromRenderedImage(ri);

        var metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

        var nativeFormat = metadata.getNativeMetadataFormatName();
        var root = metadata.getAsTree(nativeFormat);

        var text = new IIOMetadataNode("tEXt");

        var textEntry = new IIOMetadataNode("tEXtEntry");
        textEntry.setAttribute("keyword", "Author");
        textEntry.setAttribute("value", author);

        text.appendChild(textEntry);
        root.appendChild(text);

        metadata.setFromTree(nativeFormat, root);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(ios);

            writer.write(metadata, new IIOImage(ri, null, metadata), writeParam);
        }

        writer.dispose();
    }

    public void saveDirty() {
        var dataPath = server.getWorldPath(LevelResource.DATA);

        for (var entry : dirtyData.entrySet()) {
            var key = entry.getKey();
            var patch = entry.getValue();
            var id = Scriboodle.id("paginated_" + key);
            var pagesPath = id.resolveAgainst(dataPath);
            var addedPages = patch.added;

            if(patch.deleted) {
                var success = FileUtils.deleteQuietly(pagesPath.toFile());

                if(!success) {
                    Scriboodle.LOGGER.error("Failed to delete directory {}", pagesPath);
                }

                cachedData.invalidate(key);
                continue;
            }

            try {
                Files.createDirectories(pagesPath);
            } catch (IOException e) {
                Scriboodle.LOGGER.error("Failed to create directory {}", pagesPath, e);
                continue;
            }

            for (var page : addedPages.entrySet()) {
                var data = page.getValue().data();
                var author = page.getValue().author();

                var bi = new BufferedImage(data.width, data.height, BufferedImage.TYPE_INT_ARGB);
                data.write(bi);

                var pagePath = pagesPath.resolve("page_" + page.getKey() + ".png");
                try {
                    writeWithAuthor(bi, pagePath.toFile(), author.toString());
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

    public void delete(int key) {
        dirtyData.compute(key, (_, v) -> (v != null ? v : new ScribblePatch()).delete());
    }

    public int reserve() {
        next++;
        // need to create empty scribble patch so we load the correct next
        dirtyData.put(next, new ScribblePatch());
        return next;
    }
}
