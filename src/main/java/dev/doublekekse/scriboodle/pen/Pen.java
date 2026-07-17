package dev.doublekekse.scriboodle.pen;

import dev.doublekekse.scriboodle.math.Vec2d;

public class Pen {
    public Vec2d position;

    /**
     * Pen pressure.
     * Unidirectional: 0 to 1.0
     */
    public float pressure;
    /**
     * Pen horizontal tilt angle.
     * Bidirectional: -90.0 to 90.0 (left-to-right)
     */
    public float xTilt;
    /**
     * Pen vertical tilt angle.
     * Bidirectional: -90.0 to 90.0 (top-to-down)
     */
    public float yTilt;
    /**
     * Pen distance to drawing surface.  Unidirectional: 0.0 to 1.0
     */
    public float distance;

    /**
     * Pen barrel rotation.
     * Bidirectional: -180 to 179.9 (clockwise, 0 is facing up, -180.0 is facing down).
     */
    public float rotation;

    /**
     * Pen finger wheel or slider (e.g., Airbrush Pen).
     * Unidirectional: 0 to 1.0
     */
    public float slider;

    /**
     * Pressure from squeezing the pen ("barrel pressure")
     */
    public float tangentialPressure;

    public boolean down;
    public boolean inProximity;
}
