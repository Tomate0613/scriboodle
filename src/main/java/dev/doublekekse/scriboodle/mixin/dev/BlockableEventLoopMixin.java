package dev.doublekekse.scriboodle.mixin.dev;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockableEventLoop.class)
public class BlockableEventLoopMixin {
    @Inject(method = "doRunTask", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Lorg/slf4j/Marker;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    void logError(Runnable task, CallbackInfo ci, @Local(name = "e") Exception e) {
        //noinspection CallToPrintStackTrace
        e.printStackTrace();
    }
}
