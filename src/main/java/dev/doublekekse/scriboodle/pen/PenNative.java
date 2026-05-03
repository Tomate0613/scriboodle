package dev.doublekekse.scriboodle.pen;

import com.sun.jna.Library;

interface PenNative extends Library {
    int pen_init();
    int pen_get(Pen[] out, int max);
    void pen_destroy();
}
