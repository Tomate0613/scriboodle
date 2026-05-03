package dev.doublekekse.scriboodle.tools;

import dev.doublekekse.scriboodle.math.Vec2d;
import dev.doublekekse.scriboodle.math.VoronoiNoise;
import org.joml.Math;

import java.util.Optional;
import java.util.Random;

@FunctionalInterface
public interface Shape {
    Random random = new Random();

    void drawShape(ShapeInterface access, Vec2d pos, Vec2d dir, double radius);

    @FunctionalInterface
    interface ShapePredicate {
        Optional<Double> test(double x, double y, double dX, double dY, double radius);

        ShapePredicate SQUARE = (x, y, _, _, radius) -> (x > -radius && x < radius) && (y > -radius && y < radius) ? Optional.of(1.0) : Optional.empty();
        ShapePredicate BRUSH = (x, y, _, _, radius) -> {
            var isInRectangle = (x > -radius * 1.5 && x < radius * 1.5) && (y > -radius && y < radius);
            if (!isInRectangle) {
                return Optional.empty();
            }

            var op = fractalNoise(y + Math.random() * .2);
            return Optional.of(influence(op, .5 * influence(Math.abs(1 - Math.cos((x / 1.5) / Math.PI)), 1)));
        };

        default ShapePredicate rotated() {
            return (x, y, dX, dY, radius) -> {
                double pX = -dY;
                double pY = dX;

                double localX = x * dX + y * dY;
                double localY = x * pX + y * pY;

                return test(localX, localY, 1, 0, radius);
            };
        }

        default Shape toShape() {
            return ((access, pos, dir, radius) -> {

                int r = (int) Math.ceil(radius * 2);

                for (int ix = -r; ix <= r; ix++) {
                    for (int iy = -r; iy <= r; iy++) {
                        var res = test(ix, iy, dir.x(), dir.y(), radius);
                        if (res.isPresent()) {
                            access.set((int) pos.x() + ix, (int) pos.y() + iy, res.get());
                        }
                    }
                }
            });
        }
    }

    Shape CIRCLE = (access, pos, _, radius) -> {
        for (var dx = -radius; dx < radius; dx++) {
            for (var dy = -radius; dy < radius; dy++) {
                if (dx * dx + dy * dy < radius * radius) {
                    var d = org.joml.Math.sqrt((dx * dx) + (dy * dy));
                    var t = d / radius;
                    var alpha = org.joml.Math.clamp((1f - t) + .2, 0, 1);

                    access.set((int) (pos.x() + dx), (int) (pos.y() + dy), alpha);
                }
            }
        }
    };

    Shape SQUARE = (access, pos, _, radius) -> {
        for (var dx = -radius; dx < radius; dx++) {
            for (var dy = -radius; dy < radius; dy++) {
                access.set((int) (pos.x() + dx), (int) (pos.y() + dy), 1);
            }
        }
    };

    Shape SQUARE_ROTATED = ShapePredicate.SQUARE.rotated().toShape();
    Shape BRUSH_ROTATED = ShapePredicate.BRUSH.rotated().toShape();

    Shape SPLOT = ((access, pos, dir, radius) -> {
//        var offset = Math.random() * 1000;
        var offset = Math.random() * 5;

        for (var dx = -radius; dx < radius; dx++) {
            for (var dy = -radius; dy < radius; dy++) {
                var angle = new Vec2d(dx, dy).angle();
                if (dx * dx + dy * dy < radius * radius * influence(Math.sin((noisy(((angle) * 3) + 9, 1, offset, 0.2)) + dir.angle()), .3)) {

                    var d = org.joml.Math.sqrt((dx * dx) + (dy * dy));
                    var t = d / radius;
                    var alpha = org.joml.Math.clamp((1f - t) + .2, 0, 1);

                    access.set((int) (pos.x() + dx), (int) (pos.y() + dy), alpha);
                }
            }
        }
    });


    private static double holeDistance(Vec2d[] holes, Vec2d pos, double radius) {
        var dist = Double.MAX_VALUE;

        for (var hole : holes) {
            dist = Math.min(dist, pos.distanceTo(hole.scale(radius)));
        }

        return dist;
    }

    private static Vec2d[] generateHoles(int count) {
        var holes = new Vec2d[count];

        for (int i = 0; i < count; i++) {
            holes[i] = new Vec2d(Math.random() * .5, Math.random() * .5);
        }

        return holes;
    }

    Shape SPLOT_OTHER = ((access, pos, dir, radius) -> {
        var offset = Math.random() * 5;
        Vec2d[] holes = new Vec2d[]{new Vec2d(-.2, -.3), new Vec2d(.01, .3), new Vec2d(.2, -.2)};

        for (var dx = -radius; dx < radius; dx++) {
            for (var dy = -radius; dy < radius; dy++) {
                var delta = new Vec2d(dx, dy);
                var angle = delta.angle();
                if (dx * dx + dy * dy < radius * radius * influence(Math.sin((noisy(((angle) * 3) + 9, 1, offset, 0.1)) + dir.angle()), .3)) {
                    var hD = holeDistance(holes, delta, radius);

                    if (hD < .2 * radius * influence(Math.sin(dx + dy), .2)) {
                        continue;
                    }

                    var d = org.joml.Math.sqrt((dx * dx) + (dy * dy));
                    var t = d / radius;
                    var alpha = org.joml.Math.clamp((1f - t) + .2, 0, 1);

                    access.set((int) (pos.x() + dx), (int) (pos.y() + dy), alpha);
                }
            }
        }
    });

    Shape BEANS = (access, pos, dir, radius) -> {
        var beanCount = 1;
        var beanLength = 0.5;
        var beanWidth = 0.1;

        for (int i = 0; i < beanCount; i++) {
            var center = pos.randomize(radius);

            var beanOffset = dir.randomize(.5).normalize().scale(radius * beanLength);
            var beanStart = center.sub(beanOffset);
            var beanEnd = center.add(beanOffset);

            var totalDistance = beanStart.distanceTo(beanEnd);

            for (var beanPos : beanStart.walk(beanEnd, .4)) {
                var progress = beanPos.distanceTo(beanStart) / totalDistance;
                var width = Math.sqrt(Math.sin(progress * Math.PI)) * radius * beanWidth;
                var perp = dir.perpendicular().scale(width);
                var left = beanPos.add(perp);
                var right = beanPos.sub(perp);

                for (var beanPixel : left.walk(right, .5)) {
                    access.set(beanPixel, 1);
                }
            }
        }
    };

    Shape VORONOI = (access, pos, dir, radius) -> {
        var noise = new VoronoiNoise(random.nextLong(Long.MAX_VALUE));

        var vScale1 = 1;
        var vScale2 = 1.5;

        for (var p : pos.sub(radius, radius).walkSquare(pos.add(radius, radius), 1)) {
            var distSquared = p.distanceToSquared(pos);
            if (distSquared > radius * radius) {
                continue;
            }
            var v1 = 1 - noise.f2MinusF1(p.scale(vScale1 / radius));
            var v2 = 1 - noise.f2MinusF1(p.add(1000, 1000).scale(vScale2 / radius));

            var vv1 = v1 * v1 * v1 * v1 * v1 * v1;
            var vv2 = v2 * v2 * v2 * v2 * v2 * v2;

            var alpha = (binary(vv1, .5) + clip(vv2, .1, 1)) * 2.5 * (1 - (distSquared / (radius * radius)));
            if (alpha > .5) {
                access.set(p, alpha);
            }
        }
    };

    interface ShapeInterface {
        void set(int x, int y, double alpha);

        default void set(Vec2d pos, double alpha) {
            set((int) pos.x(), (int) pos.y(), alpha);
        }
    }

    private static double clip(double value, double min, double max) {
        if (value < min || value > max) {
            return 0;
        }

        return value;
    }

    private static double binary(double value, double threshold) {
        return value > threshold ? 1 : 0;
    }

    private static double influence(double value, double influence) {
        return value * influence + (1 - influence);
    }

    private static double hash(int x) {
        x = (x << 13) ^ x;
        return 1.0 - ((x * (x * x * 15731 + 789221) + 1376312589)
            & 0x7fffffff) / 1073741824.0;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double noise(double x) {
        int x0 = (int) Math.floor(x);
        int x1 = x0 + 1;

        double t = x - x0;

        double v0 = hash(x0);
        double v1 = hash(x1);

        return Math.lerp(v0, v1, fade(t));
    }

    private static double fractalNoise(double x) {
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

    private static double noisy(double value, double scale, double offset, double influence) {
        return value * influence(fractalNoise(value * scale + offset), influence);
    }
}
