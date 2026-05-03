package dev.doublekekse.scriboodle.math;

import org.joml.Math;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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

    public double angle() {
        if(x == 0 && y == 0) {
            return 0;
        }

        var normalized = this.normalize();
        return Math.atan2(normalized.y, normalized.x);
    }

    public Iterable<Vec2d> walk(Vec2d target, double step) {
        if (step <= 0) {
            throw new IllegalArgumentException();
        }

        var delta = target.sub(this);
        var totalDist = delta.length();

        if (totalDist == 0) {
            return List.of(this);
        }

        var dir = delta.normalize();

        return () -> new Iterator<>() {
            private double traveled = 0;
            private boolean done = false;

            @Override
            public boolean hasNext() {
                return !done;
            }

            @Override
            public Vec2d next() {
                if (done) throw new NoSuchElementException();

                Vec2d point = add(new Vec2d(
                    dir.x() * traveled,
                    dir.y() * traveled
                ));

                traveled += step;

                if (traveled >= totalDist) {
                    done = true;
                    return target;
                }

                return point;
            }
        };
    }

    public Vec2d randomize(double radius) {
        var angle = Math.random() * 2 * Math.PI;
        var r = Math.sqrt(Math.random()) * radius;

        double dx = r * Math.cos(angle);
        double dy = r * Math.sin(angle);

        return new Vec2d(x + dx, y + dy);
    }

    /**
     * clockwise
     */
    public Vec2d perpendicular() {
        //noinspection SuspiciousNameCombination
        return new Vec2d(y, -x);
    }

    /**
     * counterclockwise
     */
    public Vec2d perpendicularCCW() {
        //noinspection SuspiciousNameCombination
        return new Vec2d(-y, x);
    }
}
