package dev.doublekekse.scriboodle.registry;

import com.mojang.blaze3d.platform.cursor.CursorType;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

public class ScriboodleCursorTypes {
    public static final CursorType HIDDEN = hidden();

    private static CursorType hidden() {
        var pixels = BufferUtils.createByteBuffer(16 * 16 * 4);

        var image = GLFWImage.malloc();
        image.set(16, 16, pixels);

        var handle = GLFW.glfwCreateCursor(image, 0, 0);
        return new CursorType("hidden", handle);
    }
}
