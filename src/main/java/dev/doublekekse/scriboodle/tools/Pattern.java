package dev.doublekekse.scriboodle.tools;

import dev.doublekekse.Slider;
import dev.doublekekse.scriboodle.ColorUtils;
import dev.doublekekse.scriboodle.math.M;
import dev.doublekekse.scriboodle.math.Vec2d;
import dev.doublekekse.scriboodle.math.VoronoiNoise;
import net.minecraft.util.ARGB;
import org.joml.Math;

@FunctionalInterface
public interface Pattern {
    int getColor(int x, int y, int previousColor, int newColor, double alpha, double opacity);

    Pattern DIRECT = (_, _, previousColor, newColor, _, opacity) -> ColorUtils.mix(previousColor, ARGB.color(0xff, newColor), opacity);
    Pattern SOFT = (_, _, previousColor, newColor, alpha, opacity) -> ColorUtils.mix(previousColor, ARGB.color(0xff, newColor), (alpha + (Math.random() * .2)) * .24 * opacity);
    Pattern ROUGH = (_, _, previousColor, newColor, _, opacity) -> ColorUtils.mix(previousColor, ARGB.color(0xff, newColor), Math.random() * opacity);
    Pattern ROUGH_OTHER = (_, _, previousColor, newColor, alpha, opacity) -> ColorUtils.mix(previousColor, ARGB.color(0xff, newColor), Math.random() * opacity * alpha);
    Pattern VORONOI = (x, y, previousColor, newColor, alpha, opacity) -> {
        var voronoi = 1 - VoronoiNoise.ZERO.f2MinusF1(new Vec2d(x, y).scale(.2));
        return ColorUtils.mix(previousColor, ARGB.color(0xff, newColor), alpha * voronoi * opacity);
    };
    Pattern VORONOI_OTHER = (x, y, previousColor, newColor, alpha, opacity) -> {
        var voronoi = 1 - VoronoiNoise.ZERO.f2MinusF1(new Vec2d(x, y).scale(2));
        return ColorUtils.mix(previousColor, ARGB.color(0xff, newColor), alpha * voronoi * opacity);
    };
    Pattern BUBBLES = (x, y, previousColor, newColor, alpha, opacity) -> {
        var voronoi = VoronoiNoise.ZERO.f1(new Vec2d(x, y).randomize(1).scale(.1));
        return ColorUtils.mix(previousColor, ARGB.color(0xff, newColor), M.influence(M.quantize(voronoi, 4), .6) * opacity * alpha + (Math.random() * .01));
    };
}
