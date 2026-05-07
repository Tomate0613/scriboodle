package dev.doublekekse.scriboodle.registry;

import com.mojang.serialization.Codec;
import dev.doublekekse.scriboodle.Scriboodle;
import dev.doublekekse.scriboodle.component.ScribbleStyle;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Unit;

public class ScriboodleComponents {
    public static final DataComponentType<Integer> SCRIBBLE_REFERENCE = register(
        DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).cacheEncoding().build(),
        "scribble_reference"
    );

    public static final DataComponentType<ScribbleStyle> SCRIBBLE_STYLE = register(
        DataComponentType.<ScribbleStyle>builder().persistent(ScribbleStyle.CODEC).networkSynchronized(ScribbleStyle.STREAM_CODEC).build(),
        "scribble_style"
    );

    public static final DataComponentType<Unit> SCRIBBLEABLE = register(
        DataComponentType.<Unit>builder().persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC).build(),
        "scribbleable"
    );



    private static <A, T extends DataComponentType<A>> T register(T componentType, String path) {
        return Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Scriboodle.id(path),
            componentType
        );
    }

    public static void register() {
    }
}
