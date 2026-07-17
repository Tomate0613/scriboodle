package dev.doublekekse.scriboodle.pen;

public class PenApi {
    private static PenApi INSTANCE;
    private int penCount;

    private PenApi() {
        if(true) {
            return;
        }
    }

    public static PenApi getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PenApi();
        }

        return INSTANCE;
    }

    public Pen[] getPens() {
//        if (penCount == 0) {
            return new Pen[0];
//        }

//        Pen template = new Pen();
//        Pen[] out = (Pen[]) template.toArray(16);
//
//        int count = NATIVE_INSTANCE.pen_get(out, out.length);


//        return Arrays.copyOf(out, count);
    }

    public void destroy() {
        INSTANCE = null;

//        if (NATIVE_INSTANCE != null) {
//            NATIVE_INSTANCE.pen_destroy();
//        }
    }

    public double getPressureOrDefault(double defaultValue) {
        var pens = getPens();

        var pressure = defaultValue;

        for (var pen : pens) {
            if (pen.down != 0) {
                pressure = pen.pressure;
            }
        }

        return pressure;
    }

    public boolean hasPen() {
        return penCount > 0;
    }
}
