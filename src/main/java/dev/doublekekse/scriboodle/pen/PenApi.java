package dev.doublekekse.scriboodle.pen;

import com.sun.jna.Native;
import dev.doublekekse.scriboodle.Scriboodle;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class PenApi {
    private static final String SOURCE_FILENAME = "libpen.so";
    private static final String TARGET_FILENAME = Scriboodle.MOD_ID + "-" + SOURCE_FILENAME;

    private static PenApi INSTANCE;
    private static PenNative NATIVE_INSTANCE;
    private int penCount;

    private PenApi() {
        if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_X11) {
            return;
        }

        try {
            NATIVE_INSTANCE = Native.load(extract().toAbsolutePath().toString(), PenNative.class);
            penCount = NATIVE_INSTANCE.pen_init();
        } catch (Exception e) {
            Scriboodle.LOGGER.error("Failed to load libpen", e);
        }
    }

    private static Path extract() throws IOException {
        var target = FabricLoader.getInstance().getGameDir().resolve(TARGET_FILENAME);
        var src = FabricLoader.getInstance().getModContainer(Scriboodle.MOD_ID).orElseThrow().findPath("natives").orElseThrow().resolve(SOURCE_FILENAME);

        Files.deleteIfExists(target);
        Files.createDirectories(target.getParent());
        Files.copy(src, target);

        return target;
    }

    public static PenApi getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PenApi();
        }

        return INSTANCE;
    }

    public Pen[] getPens() {
        if (penCount == 0) {
            return new Pen[0];
        }

        Pen template = new Pen();
        Pen[] out = (Pen[]) template.toArray(16);

        int count = NATIVE_INSTANCE.pen_get(out, out.length);

        return Arrays.copyOf(out, count);
    }

    public double getPressureOrDefault(double defaultValue) {
        var pens = getPens();

        var pressure = defaultValue;

        for (var pen : pens) {
            if (pen.down != 0) {
                pressure = pen.pressure;
            }
        }

        return pressure;
    }

    public boolean hasPen() {
        return penCount > 0;
    }
}
