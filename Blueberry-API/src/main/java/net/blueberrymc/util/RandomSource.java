package net.blueberrymc.util;

public interface RandomSource {
    void setSeed(long seed);

    int nextInt();

    int nextInt(int bound);

    default int nextIntBetweenInclusive(int min, int max) {
        return this.nextInt(max - min + 1) + min;
    }

    long nextLong();

    boolean nextBoolean();

    float nextFloat();

    double nextDouble();

    double nextGaussian();

    default double triangle(double d, double d2) {
        return d + d2 * (this.nextDouble() - this.nextDouble());
    }

    default void consumeCount(int count) {
        for (int i = 0; i < count; ++i) {
            this.nextInt();
        }
    }

    default int nextInt(int origin, int bound) {
        if (origin >= bound) {
            throw new IllegalArgumentException("bound - origin is non positive");
        } else {
            return origin + this.nextInt(bound - origin);
        }
    }
}
