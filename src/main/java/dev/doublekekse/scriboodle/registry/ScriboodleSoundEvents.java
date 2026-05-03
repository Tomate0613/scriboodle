package dev.doublekekse.scriboodle.registry;

import dev.doublekekse.scriboodle.Scriboodle;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public class ScriboodleSoundEvents {
    public static final SoundEvent SCRIBBLE = register("scribble");

    private static SoundEvent register(final String path) {
        var id = Scriboodle.id(path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {

    }
}
