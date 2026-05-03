package dev.doublekekse.scriboodle.mixin;

import com.mojang.datafixers.DataFixer;
import dev.doublekekse.scriboodle.data.ScribbleManager;
import dev.doublekekse.scriboodle.duck.MinecraftServerDuck;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;
import java.util.Optional;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements MinecraftServerDuck {
    @Unique
    ScribbleManager scribbleManager;

    @Inject(method = "<init>", at = @At("RETURN"))
    void init(Thread serverThread, LevelStorageSource.LevelStorageAccess storageSource, PackRepository packRepository, WorldStem worldStem, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<GameRules> gameRules, Proxy proxy, DataFixer fixerUpper, Services services, LevelLoadListener levelLoadListener, boolean propagatesCrashes, CallbackInfo ci) {
        scribbleManager = new ScribbleManager((MinecraftServer) (Object) this);
    }

    public ScribbleManager scriboodle$getScribbleManager() {
        return scribbleManager;
    }

    @Inject(method = "saveEverything", at = @At("RETURN"))
    void save(boolean silent, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        scribbleManager.saveDirty();
    }
}
