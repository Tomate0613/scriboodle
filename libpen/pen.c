#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/extensions/XInput2.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "pen.h"

#define MAX_PENS 16

typedef struct {
  int device_id;

  int pressure_idx;
  double pressure_min;
  double pressure_max;

  double x, y;
  double pressure;
  int down;
} PenState;

static Display *g_dpy = NULL;
static int xi_opcode;

static PenState pens[MAX_PENS];
static int pen_count = 0;

static PenState *find_pen(int device_id) {
  for (int i = 0; i < pen_count; i++) {
    if (pens[i].device_id == device_id) {
      return &pens[i];
    }
  }

  return NULL;
}

static PenState *add_pen(int device_id) {
  if (pen_count >= MAX_PENS) {
    return NULL;
  }

  PenState *p = &pens[pen_count++];
  memset(p, 0, sizeof(*p));

  p->device_id = device_id;
  p->pressure_idx = -1;

  return p;
}

int pen_init() {
  // Might be necessary in the future when mojang uses seperate rendering
  // thread, will have to see
  // XInitThreads();

  Display *dpy = XOpenDisplay(NULL);

  if (!dpy) {
    fprintf(stderr, "Failed to open display\n");
    return 0;
  }

  g_dpy = dpy;

  int event, error;
  if (!XQueryExtension(dpy, "XInputExtension", &xi_opcode, &event, &error)) {
    fprintf(stderr, "XInput2 not available\n");
    pen_destroy();
    return 0;
  }

  int major = 2, minor = 0;
  if (XIQueryVersion(dpy, &major, &minor) != Success) {
    fprintf(stderr, "XInput2 not available\n");
    pen_destroy();
    return 0;
  }

  int ndevices;
  XIDeviceInfo *devices = XIQueryDevice(dpy, XIAllDevices, &ndevices);

  if(!devices) {
    pen_destroy();
    return 0;
  }

  for (int i = 0; i < ndevices; i++) {
    XIDeviceInfo *dev = &devices[i];

    PenState *pen = NULL;

    for (int c = 0; c < dev->num_classes; c++) {
      XIAnyClassInfo *cls = dev->classes[c];

      if (cls->type == XIValuatorClass) {
        XIValuatorClassInfo *v = (XIValuatorClassInfo *)cls;

        if(!v->label) {
          continue;
        }

        char *name = XGetAtomName(dpy, v->label);
        if (!name) {
          continue;
        }

        if (strstr(name, "Abs Pressure")) {
          if (!pen) {
            pen = add_pen(dev->deviceid);
          }

          if (pen && v->min != v->max) {
            pen->pressure_idx = v->number;
            pen->pressure_min = v->min;
            pen->pressure_max = v->max;
          }
        }

        XFree(name);
      }
    }

    // if (pen) {
    //   fprintf(stderr, "id=%d (pressure=%d)\n", pen->device_id,
    //           pen->pressure_idx);
    // } else {
    //   fprintf(stderr, "Skipping %s (id=%d)\n", dev->name,
    //   dev->deviceid);
    // }
  }

  XIFreeDeviceInfo(devices);

  if (pen_count == 0) {
    return 0;
  }

  XIEventMask *masks = calloc(pen_count, sizeof(XIEventMask));
  unsigned char *mask_bits = calloc(pen_count, XIMaskLen(XI_LASTEVENT));

  for (int i = 0; i < pen_count; i++) {
    unsigned char *bits = mask_bits + i * XIMaskLen(XI_LASTEVENT);

    masks[i].deviceid = pens[i].device_id;
    masks[i].mask_len = XIMaskLen(XI_LASTEVENT);
    masks[i].mask = bits;

    XISetMask(bits, XI_Motion);
    XISetMask(bits, XI_ButtonPress);
    XISetMask(bits, XI_ButtonRelease);
  }

  XISelectEvents(dpy, DefaultRootWindow(dpy), masks, pen_count);
  XFlush(dpy);

  free(mask_bits);
  free(masks);

  return pen_count;
}

static void handle_motion(XIDeviceEvent *ev) {
  PenState *pen = find_pen(ev->deviceid);
  if (!pen) {
    return;
  }

  pen->x = ev->event_x;
  pen->y = ev->event_y;

  double *values = ev->valuators.values;
  XIValuatorState *vs = &ev->valuators;

  int idx = 0;

  for (int i = 0; i < vs->mask_len * 8; i++) {
    if (XIMaskIsSet(vs->mask, i)) {
      double v = values[idx++];

      if (i == pen->pressure_idx) {
        pen->pressure =
            (v - pen->pressure_min) / (pen->pressure_max - pen->pressure_min);
      }
    }
  }
}

static void handle_button(XIDeviceEvent *ev, int press) {
  PenState *pen = find_pen(ev->deviceid);
  if (!pen) {
    return;
  }

  if (ev->detail == 1) {
    pen->down = press;
  }
}

int pen_get(Pen *out, int max) {
  if (pen_count == 0 || !g_dpy) {
    return 0;
  }

  while (XPending(g_dpy)) {
    XEvent ev;
    XNextEvent(g_dpy, &ev);

    if (ev.type == GenericEvent && ev.xcookie.extension == xi_opcode &&
        XGetEventData(g_dpy, &ev.xcookie)) {
      XGenericEventCookie *cookie = &ev.xcookie;

      // TODO: Raw motion?
      if (cookie->evtype == XI_Motion) {
        handle_motion((XIDeviceEvent *)cookie->data);
      } else if (cookie->evtype == XI_ButtonPress) {
        handle_button((XIDeviceEvent *)cookie->data, 1);
      } else if (cookie->evtype == XI_ButtonRelease) {
        handle_button((XIDeviceEvent *)cookie->data, 0);
      }

      XFreeEventData(g_dpy, &ev.xcookie);
    }
  }

  int n = pen_count < max ? pen_count : max;

  for (int i = 0; i < n; i++) {
    out[i].id = pens[i].device_id;
    out[i].x = pens[i].x;
    out[i].y = pens[i].y;
    out[i].pressure = pens[i].pressure;
    out[i].down = pens[i].down;
  }

  return n;
}

void pen_destroy() {
  if (g_dpy) {
    XCloseDisplay(g_dpy);
    g_dpy = NULL;
  }

  pen_count = 0;
  memset(pens, 0, sizeof(pens));
}
