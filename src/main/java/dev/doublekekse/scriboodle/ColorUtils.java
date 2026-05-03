package dev.doublekekse.scriboodle;

import com.scrtwpns.Mixbox;
import net.minecraft.util.ARGB;

import java.util.List;

public final class ColorUtils {
    public static int mix(List<Integer> colors) {
        float pro = 1.0f / colors.size();
        var cos = colors.stream().map(
            (color) -> Mixbox.rgbToLatent(color & 0xffffff)
        ).toList();

        var zMix = new float[Mixbox.LATENT_SIZE];

        for (int i = 0; i < zMix.length; i++) {
            float q = 0;
            for (var c : cos) {
                q += c[i] * pro;
            }

            zMix[i] = q;
        }

        return Mixbox.latentToRgb(zMix);
    }

    public static int mix(int colorA, int colorB, float alpha) {
        return Mixbox.lerp(
            colorA,
            colorB,
            Math.clamp(alpha, 0, 1)
        );

    }

    public static int mix(int colorA, int colorB, double alpha) {
        return mix(colorA, colorB, (float) alpha);
    }

    public static boolean isLight(int color) {
        var r = (color & 0xff0000) >> 16;
        var g = color & 0x00ff00 >> 8;
        var b = color & 0x0000ff;

        return r > 0xD0 || g > 0xD0 || b > 0xD0;
    }

    public static int randomColor() {
        return ARGB.color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }
}
