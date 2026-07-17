package dev.doublekekse.scriboodle.mixin;

import com.mojang.blaze3d.platform.Window;
import dev.doublekekse.scriboodle.math.Vec2d;
import dev.doublekekse.scriboodle.pen.PenListener;
import dev.doublekekse.scriboodle.pen.Pen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLPen;
import org.lwjgl.sdl.SDL_Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {
    @Unique
    private final Pen pen = new Pen();

    @Inject(method = "handleEvent", at = @At("HEAD"))
    void handleEvent(SDL_Event event, CallbackInfo ci) {
        if (event.type() == SDLEvents.SDL_EVENT_PEN_PROXIMITY_IN) {
            pen.inProximity = true;
        }

        if (event.type() == SDLEvents.SDL_EVENT_PEN_PROXIMITY_OUT) {
            pen.inProximity = false;
        }

        if (event.type() == SDLEvents.SDL_EVENT_PEN_DOWN) {
            pen.down = true;
        }

        if (event.type() == SDLEvents.SDL_EVENT_PEN_UP) {
            pen.down = false;
        }

        if (event.type() == SDLEvents.SDL_EVENT_PEN_MOTION) {
            var p = event.pmotion();

            var scaledX = MouseHandler.getScaledXPos((Window) (Object) this, p.x());
            var scaledY = MouseHandler.getScaledYPos((Window) (Object) this, p.y());

            pen.position = new Vec2d(scaledX, scaledY);
        }

        if (event.type() == SDLEvents.SDL_EVENT_PEN_AXIS) {
            var e = event.paxis();

            switch (e.axis()) {
                case SDLPen.SDL_PEN_AXIS_PRESSURE -> pen.pressure = e.value();
                case SDLPen.SDL_PEN_AXIS_XTILT -> pen.xTilt = e.value();
                case SDLPen.SDL_PEN_AXIS_YTILT -> pen.yTilt = e.value();
                case SDLPen.SDL_PEN_AXIS_DISTANCE -> pen.distance = e.value();
                case SDLPen.SDL_PEN_AXIS_ROTATION -> pen.rotation = e.value();
                case SDLPen.SDL_PEN_AXIS_SLIDER -> pen.slider = e.value();
                case SDLPen.SDL_PEN_AXIS_TANGENTIAL_PRESSURE -> pen.tangentialPressure = e.value();
            }
        }

        var mc = Minecraft.getInstance();
        var screen = mc.gui.screen();


        if (!(screen instanceof PenListener listener)) {
            return;
        }

        if (event.type() == SDLEvents.SDL_EVENT_PEN_MOTION) {
            listener.onPenMoved(pen);
        }

        if (event.type() == SDLEvents.SDL_EVENT_PEN_DOWN) {
            listener.onPenDown(pen);
        }

        if (event.type() == SDLEvents.SDL_EVENT_PEN_UP) {
            listener.onPenUp(pen);
        }
    }
}
