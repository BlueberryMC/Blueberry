package net.blueberrymc.util;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public record RandomRandomSource(@NotNull Random handle) implements RandomSource {
    @Override
    public void setSeed(long seed) {
        handle.setSeed(seed);
    }

    @Override
    public int nextInt() {
        return handle.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return handle.nextInt(bound);
    }

    @Override
    public long nextLong() {
        return handle.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return handle.nextBoolean();
    }

    @Override
    public float nextFloat() {
        return handle.nextFloat();
    }

    @Override
    public double nextDouble() {
        return handle.nextDouble();
    }

    @Override
    public double nextGaussian() {
        return handle.nextGaussian();
    }
}
