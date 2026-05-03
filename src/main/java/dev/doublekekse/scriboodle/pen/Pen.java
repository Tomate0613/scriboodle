package dev.doublekekse.scriboodle.pen;

import com.sun.jna.Structure;

import java.util.List;

public class Pen extends Structure {
    public static class ByReference extends Pen implements Structure.ByReference {}

    public int id;
    public double x, y;
    public double pressure;
    public int down;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("id", "x", "y", "pressure", "down");
    }
}
