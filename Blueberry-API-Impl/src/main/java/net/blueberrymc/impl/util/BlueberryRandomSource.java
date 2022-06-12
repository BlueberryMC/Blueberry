package net.blueberrymc.impl.util;

import net.blueberrymc.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record BlueberryRandomSource(@NotNull RandomSource handle) implements net.minecraft.util.RandomSource {
    @Contract(" -> new")
    @Override
    public net.minecraft.util.@NotNull RandomSource fork() {
        return new LegacyRandomSource(nextLong());
    }

    @Contract(" -> new")
    @Override
    public @NotNull PositionalRandomFactory forkPositional() {
        return new LegacyRandomSource.LegacyPositionalRandomFactory(nextLong());
    }

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

    @Contract(pure = true)
    public static net.minecraft.util.@NotNull RandomSource of(@NotNull RandomSource randomSource) {
        if (randomSource instanceof MinecraftRandomSource minecraftRandomSource) {
            return minecraftRandomSource.handle();
        } else {
            return new BlueberryRandomSource(randomSource);
        }
    }
}
