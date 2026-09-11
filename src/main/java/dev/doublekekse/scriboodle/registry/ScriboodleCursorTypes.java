package dev.doublekekse.scriboodle.registry;

import com.mojang.blaze3d.platform.cursor.CursorType;
import org.lwjgl.BufferUtils;
import org.lwjgl.sdl.SDLMouse;

public class ScriboodleCursorTypes {
    public static final CursorType HIDDEN = hidden();

    private static CursorType hidden() {
        var pixels = BufferUtils.createByteBuffer(8 * 8 * 4);
        long handle = SDLMouse.SDL_CreateCursor(pixels, pixels, 8, 8, 0, 0);
        return new CursorType("hidden", handle);
    }
}
