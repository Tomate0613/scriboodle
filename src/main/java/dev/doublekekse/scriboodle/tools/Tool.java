package dev.doublekekse.scriboodle.tools;

import dev.doublekekse.scriboodle.math.Vec2d;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record Tool(
    Item displayItem,

    Pattern pattern,
    Shape shape,
    Dynamics dynamics,
    double spacing,
    double opacity,
    double[] radii
) {
    public double radius(int radiusIndex, double pressure) {
        return dynamics.modifyRadius(radii[radiusIndex], pressure);
    }

    public void draw(CanvasAccess access, Vec2d center, Vec2d dir, double radius, double pressure, double distance, int color) {
        shape.drawShape((x, y, alpha) -> {
            var prev = access.get(x, y);
            var newColor = pattern.getColor(prev, color, alpha, dynamics.modifyOpacity(opacity, pressure, distance));
            access.set(x, y, newColor);
        }, center, dir, radius);
    }

    public void stamp(CanvasAccess access, Vec2d pos, double radius, double pressure, int color) {
        draw(access, pos, Vec2d.RIGHT, radius, pressure, 0, color);
    }

    public Vec2d drawStroke(CanvasAccess access, Vec2d from, Vec2d to, double radius, double pressure, double distance, int color) {
        var d = to.sub(from);
        var dist = d.length();

        var targetDist = (spacing * radius);

        var dir = d.normalize();

        var max = 200;
        while (dist > targetDist && --max > 0) {
            draw(access, from, dir, radius, pressure, distance, color);

            from = from.add(dir.scale(targetDist));

            dist -= targetDist;
        }

        return from;
    }

    public ItemStack itemStack() {
        return new ItemStack(displayItem);
    }

    public static final double[] RADII_SMALL = new double[]{1, 1.5, 2.5, 3, 7};
    public static final double[] RADII_BIG = new double[]{4, 5, 6, 7, 14};

    public static final Tool FEATHER = new Tool(Items.FEATHER, Pattern.DIRECT, Shape.CIRCLE, Dynamics.DIRECT, .35, 1, RADII_SMALL);
    public static final Tool WATER = new Tool(Items.WATER_BUCKET, Pattern.SOFT, Shape.CIRCLE, Dynamics.DIRECT, .2, .7, RADII_BIG);
    public static final Tool WATER_PEN = new Tool(Items.WATER_BUCKET, Pattern.SOFT, Shape.CIRCLE, Dynamics.PRESSURE_OPACITY, .2, 1, RADII_BIG);

    public Tool with(Pattern pattern) {
        return new Tool(displayItem, pattern, shape, dynamics, spacing, opacity, radii);
    }

    public Tool with(Item displayItem) {
        return new Tool(displayItem, pattern, shape, dynamics, spacing, opacity, radii);
    }

    public Tool with(Shape shape) {
        return new Tool(displayItem, pattern, shape, dynamics, spacing, opacity, radii);
    }

    public Tool with(Dynamics dynamics) {
        return new Tool(displayItem, pattern, shape, dynamics, spacing, opacity, radii);
    }

    public Tool with(double[] radii) {
        return new Tool(displayItem, pattern, shape, dynamics, spacing, opacity, radii);
    }

    public Tool withSpacing(double spacing) {
        return new Tool(displayItem, pattern, shape, dynamics, spacing, opacity, radii);
    }

    public Tool withOpacity(double opacity) {
        return new Tool(displayItem, pattern, shape, dynamics, spacing, Math.clamp(opacity, 0, 1), radii);
    }
}
