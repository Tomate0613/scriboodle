package dev.doublekekse.scriboodle.tools;

import dev.doublekekse.scriboodle.ColorUtils;
import net.minecraft.util.ARGB;
import org.joml.Math;

@FunctionalInterface
public interface Pattern {
    int getColor(int previousColor, int newColor, double alpha, double opacity);

    Pattern DIRECT = (previousColor, newColor, _, opacity) -> ColorUtils.mix(previousColor, ARGB.color(0xff, newColor), opacity);
    Pattern ROUGH = (previousColor, newColor, _, opacity) -> ColorUtils.mix(previousColor, ARGB.color(0xff, newColor), Math.random() * opacity);
    Pattern SOFT = (previousColor, newColor, alpha, opacity) -> ColorUtils.mix(previousColor, ARGB.color(0xff, newColor), (alpha + (Math.random() * .2)) * .24 * opacity);
}
