package dev.doublekekse.scriboodle.math;

import org.joml.Math;

public class M {

    public static double clip(double value, double min, double max) {
        if (value < min || value > max) {
            return 0;
        }

        return value;
    }

    public static double binary(double value, double threshold) {
        return value > threshold ? 1 : 0;
    }

    public static double quantize(double value, int steps) {
        double s = Math.round(value * steps);
        return s / steps;
    }

    public static double influence(double value, double influence) {
        return value * influence + (1 - influence);
    }

    public static double hash(int x) {
        x = (x << 13) ^ x;
        return 1.0 - ((x * (x * x * 15731 + 789221) + 1376312589)
            & 0x7fffffff) / 1073741824.0;
    }

    public static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    public static double noise(double x) {
        int x0 = (int) org.joml.Math.floor(x);
        int x1 = x0 + 1;

        double t = x - x0;

        double v0 = hash(x0);
        double v1 = hash(x1);

        return Math.lerp(v0, v1, fade(t));
    }

    public static double fractalNoise(double x) {
        double result = 0;
        double amplitude = 1.0;
        double frequency = 1.0;

        for (int i = 0; i < 4; i++) {
            result += noise(x * frequency) * amplitude;
            amplitude *= 0.5;
            frequency *= 2.0;
        }

        return result;
    }

    public static double noisy(double value, double scale, double offset, double influence) {
        return value * influence(fractalNoise(value * scale + offset), influence);
    }
}
