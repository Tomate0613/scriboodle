package dev.doublekekse.scriboodle.tools;

import org.joml.Math;

public interface Dynamics {
    default double modifyRadius(double radius, double pressure) {
        return radius;
    }
    default double modifyOpacity(double opacity, double pressure, double distance) {
        return opacity;
    }

    Dynamics DIRECT = new Dynamics() {
    };
    Dynamics PRESSURE_RADIUS = new Dynamics() {
        @Override
        public double modifyRadius(double radius, double pressure) {
            return Math.lerp(1, 2 * radius, pressure);
        }
    };

    Dynamics PRESSURE_OPACITY = new Dynamics() {
        @Override
        public double modifyOpacity(double opacity, double pressure, double distance) {
            return opacity * (pressure + .1f);
        }
    };

    Dynamics DISTANCE_A = new Dynamics() {
        @Override
        public double modifyOpacity(double opacity, double pressure, double distance) {
            double d = 200.0;

            double t = Math.clamp(distance / d, 0.0, 1.0);

            // smoother, more solid center
            double falloff = 1.0 - (t * t);

            // pressure has weaker influence
            double body = 0.7 + 0.3 * pressure;

            return Math.clamp(opacity * falloff * body, 0.1, 1.0);
        }
    };

    Dynamics DISTANCE_B = new Dynamics() {
        @Override
        public double modifyOpacity(double opacity, double pressure, double distance) {
            double d = 200.0;

            double falloff = Math.exp(-distance / (d * 0.6));

            double pigment = java.lang.Math.pow(pressure, 1.5);

            return Math.clamp(opacity * falloff * pigment, 0.02, 1.0);
        }
    };
}
