package dev.doublekekse.scriboodle.tools;

import org.joml.Math;

import java.util.Optional;

@FunctionalInterface
public interface Shape {
    void drawShape(ShapeInterface access, double x, double y, double dX, double dY, double radius);

    @FunctionalInterface
    interface ShapePredicate {
        Optional<Double> test(double x, double y, double dX, double dY, double radius);

        ShapePredicate SQUARE = (x, y, _, _, radius) -> (x > -radius && x < radius) && (y > -radius && y < radius) ? Optional.of(1.0) : Optional.empty();

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
            return ((access, x, y, dX, dY, radius) -> {

                int r = (int) Math.ceil(radius * 2);

                for (int ix = -r; ix <= r; ix++) {
                    for (int iy = -r; iy <= r; iy++) {
                        var res = test(ix, iy, dX, dY, radius);
                        if (res.isPresent()) {
                            access.set((int) x + ix, (int) y + iy, res.get());
                        }
                    }
                }
            });
        }
    }

    Shape CIRCLE = (access, x, y, _, _, radius) -> {
        for (var dx = -radius; dx < radius; dx++) {
            for (var dy = -radius; dy < radius; dy++) {
                if (dx * dx + dy * dy < radius * radius) {
                    var d = org.joml.Math.sqrt((dx * dx) + (dy * dy));
                    var t = d / radius;
                    var alpha = org.joml.Math.clamp((1f - t) + .2, 0, 1);

                    access.set((int) (x + dx), (int) (y + dy), alpha);
                }
            }
        }
    };

    Shape SQUARE = (access, x, y, dX, dY, radius) -> {
        for (var dx = -radius; dx < radius; dx++) {
            for (var dy = -radius; dy < radius; dy++) {
                access.set((int) (x + dx), (int) (y + dy), 1);
            }
        }
    };

    Shape SQUARE_ROTATED = ShapePredicate.SQUARE.rotated().toShape();

    interface ShapeInterface {
        void set(int x, int y, double alpha);
    }

}
