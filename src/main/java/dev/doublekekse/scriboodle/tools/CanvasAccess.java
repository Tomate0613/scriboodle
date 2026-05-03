package dev.doublekekse.scriboodle.tools;

import dev.doublekekse.scriboodle.data.ScribbleData;
import net.minecraft.util.ARGB;

public interface CanvasAccess {
    int get(int x, int y);

    void set(int x, int y, int color);


    class Simple implements CanvasAccess {
        public ScribbleData data;
        int backgroundColor;

        public Simple(ScribbleData data, int backgroundColor) {
            this.data = data;
            this.backgroundColor = ARGB.color(0, backgroundColor);
        }

        @Override
        public int get(int x, int y) {
            if (data.inBounds(x, y)) {
                var d = data.get(x, y);
                if (ARGB.alpha(d) == 0) {
                    return backgroundColor;
                }

                return d;
            }

            return backgroundColor;
        }

        @Override
        public void set(int x, int y, int color) {
            if (data.inBounds(x, y)) {
                data = data.set(x, y, color);
            }
        }
    }
}
