#pragma once
#include <X11/Xlib.h>

typedef struct {
  int id;
  double x, y;
  double pressure;
  int down;
} Pen;

int pen_init();

int pen_get(Pen *out, int max);

// No clue if this even works, I am currently just relying on the xserver to clean suff up
void pen_destroy();
