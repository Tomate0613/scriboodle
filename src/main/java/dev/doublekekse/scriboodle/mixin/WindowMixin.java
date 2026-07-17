package dev.doublekekse.scriboodle.mixin;

import com.mojang.blaze3d.platform.Window;
import org.lwjgl.sdl.SDL_Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {
    @Inject(method = "handleEvent", at = @At("HEAD"))
    void handleEvent(SDL_Event event, CallbackInfo ci) {
        // TODO
        if (event.type() == SDL_Event.PAXIS) {
            System.out.println(event);
        }
    }
}
