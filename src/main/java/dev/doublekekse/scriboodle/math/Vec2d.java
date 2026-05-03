package dev.doublekekse.scriboodle.math;

import org.joml.Math;

public record Vec2d(double x, double y) {
    public static final Vec2d RIGHT = new Vec2d(1, 0);

    public Vec2d add(Vec2d value) {
        return new Vec2d(x + value.x, y + value.y);
    }

    public Vec2d sub(Vec2d value) {
        return new Vec2d(x - value.x, y - value.y);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public double distanceTo(Vec2d other) {
        var dX = other.x - x;
        var dY = other.y - y;

        return Math.sqrt(dX * dX + dY * dY);
    }

    public Vec2d normalize() {
        double invLength = Math.invsqrt(x * x + y * y);
        return new Vec2d(x * invLength, y * invLength);
    }

    public Vec2d scale(double scalar) {
        return new Vec2d(x * scalar, y * scalar);
    }
}
