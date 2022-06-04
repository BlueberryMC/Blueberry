package net.blueberrymc.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Vec3i(int x, int y, int z) {
    /**
     * Constructs a new {@link Vec3i} with the given values. This causes the precision loss of the given value.
     * @param x x
     * @param y y
     * @param z z
     */
    public Vec3i(double x, double y, double z) {
        this((int) x, (int) y, (int) z);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Vec3i withX(int x) {
        return new Vec3i(x, y, z);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Vec3i withY(int y) {
        return new Vec3i(x, y, z);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Vec3i withZ(int z) {
        return new Vec3i(x, y, z);
    }

    @Contract("_, _, _ -> new")
    public @NotNull Vec3i add(int x, int y, int z) {
        return new Vec3i(this.x + x, this.y + y, this.z + z);
    }

    @Contract("_ -> new")
    public @NotNull Vec3i add(@NotNull Vec3i vec3i) {
        return new Vec3i(this.x + vec3i.x, this.y + vec3i.y, this.z + vec3i.z);
    }

    @Contract("_, _, _ -> new")
    public @NotNull Vec3i subtract(int x, int y, int z) {
        return new Vec3i(this.x - x, this.y - y, this.z - z);
    }

    @Contract("_ -> new")
    public @NotNull Vec3i subtract(@NotNull Vec3i vec3i) {
        return new Vec3i(this.x - vec3i.x, this.y - vec3i.y, this.z - vec3i.z);
    }

    @Contract("_, _, _ -> new")
    public @NotNull Vec3i multiply(int x, int y, int z) {
        return new Vec3i(this.x * x, this.y * y, this.z * z);
    }

    @Contract("_ -> new")
    public @NotNull Vec3i multiply(int i) {
        return new Vec3i(this.x * i, this.y * i, this.z * i);
    }

    @Contract("_, _, _ -> new")
    public @NotNull Vec3i divide(int x, int y, int z) {
        return new Vec3i(this.x / x, this.y / y, this.z / z);
    }

    @Contract("_ -> new")
    public @NotNull Vec3i divide(int i) {
        return new Vec3i(this.x / i, this.y / i, this.z / i);
    }

    @Contract("_, _, _ -> new")
    public @NotNull Vec3i mod(int x, int y, int z) {
        return new Vec3i(this.x % x, this.y % y, this.z % z);
    }

    @Contract("_ -> new")
    public @NotNull Vec3i pow(int i) {
        return new Vec3i(Math.pow(this.x, i), Math.pow(this.y, i), Math.pow(this.z, i));
    }

    /**
     * Converts the Vec3i into Vec3.
     * @return A new Vec3 with the values.
     */
    @Contract(" -> new")
    public @NotNull Vec3 toVec3() {
        return new Vec3(x, y, z);
    }

    /**
     * Converts the Vec3i into Vec3 with aligned to the middle of the block.
     * @return A new Vec3 with the values.
     */
    @Contract(" -> new")
    public @NotNull Vec3 toBlockVec3() {
        return new Vec3(x + 0.5, y + 0.5, z + 0.5);
    }
}
