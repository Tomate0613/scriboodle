package dev.doublekekse.scriboodle.math;

public class VoronoiNoise {
    public static VoronoiNoise ZERO = new VoronoiNoise(0);
    private final long seed;

    public VoronoiNoise(long seed) {
        this.seed = seed;
    }

    private long hash(int x, int y) {
        long h = seed;
        h ^= x * 0x9E3779B97F4A7C15L;
        h ^= y * 0xC2B2AE3D27D4EB4FL;
        h *= 0x165667B19E3779F9L;
        return h;
    }

    private double hashToUnit(long h) {
        return (h & 0xFFFFFFFFL) / (double) 0x100000000L;
    }

    private Vec2d featurePoint(int gx, int gy) {
        long h = hash(gx, gy);

        double rx = hashToUnit(h);
        double ry = hashToUnit(h >> 32);

        return new Vec2d(gx + rx, gy + ry);
    }

    public double f1(Vec2d p) {
        int cx = (int) Math.floor(p.x());
        int cy = (int) Math.floor(p.y());

        double minDist = Double.MAX_VALUE;

        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                Vec2d fp = featurePoint(cx + x, cy + y);
                double d = p.distanceTo(fp);

                if (d < minDist) {
                    minDist = d;
                }
            }
        }

        return minDist;
    }

    public double f2MinusF1(Vec2d p) {
        int cx = (int) Math.floor(p.x());
        int cy = (int) Math.floor(p.y());

        double min1 = Double.MAX_VALUE;
        double min2 = Double.MAX_VALUE;

        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                Vec2d fp = featurePoint(cx + x, cy + y);
                double d = p.distanceTo(fp);

                if (d < min1) {
                    min2 = min1;
                    min1 = d;
                } else if (d < min2) {
                    min2 = d;
                }
            }
        }

        return min2 - min1;
    }
}
