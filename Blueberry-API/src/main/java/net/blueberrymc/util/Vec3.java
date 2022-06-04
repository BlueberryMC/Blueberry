package net.blueberrymc.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Vec3(double x, double y, double z) {
    @Contract(value = "_ -> new", pure = true)
    public @NotNull Vec3 withX(double x) {
        return new Vec3(x, y, z);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Vec3 withY(double y) {
        return new Vec3(x, y, z);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Vec3 withZ(double z) {
        return new Vec3(x, y, z);
    }

    @Contract("_, _, _ -> new")
    public @NotNull Vec3 add(int x, int y, int z) {
        return new Vec3(this.x + x, this.y + y, this.z + z);
    }

    @Contract("_ -> new")
    public @NotNull Vec3 add(@NotNull Vec3 vec3) {
        return new Vec3(this.x + vec3.x, this.y + vec3.y, this.z + vec3.z);
    }

    @Contract("_, _, _ -> new")
    public @NotNull Vec3 subtract(int x, int y, int z) {
        return new Vec3(this.x - x, this.y - y, this.z - z);
    }

    @Contract("_ -> new")
    public @NotNull Vec3 subtract(@NotNull Vec3 vec3) {
        return new Vec3(this.x - vec3.x, this.y - vec3.y, this.z - vec3.z);
    }

    @Contract("_, _, _ -> new")
    public @NotNull Vec3 multiply(int x, int y, int z) {
        return new Vec3(this.x * x, this.y * y, this.z * z);
    }

    @Contract("_ -> new")
    public @NotNull Vec3 multiply(int i) {
        return new Vec3(this.x * i, this.y * i, this.z * i);
    }

    @Contract("_, _, _ -> new")
    public @NotNull Vec3 divide(int x, int y, int z) {
        return new Vec3(this.x / x, this.y / y, this.z / z);
    }

    @Contract("_ -> new")
    public @NotNull Vec3 divide(int i) {
        return new Vec3(this.x / i, this.y / i, this.z / i);
    }

    @Contract("_ -> new")
    public @NotNull Vec3 mod(int i) {
        return new Vec3(this.x % i, this.y % i, this.z % i);
    }

    @Contract("_ -> new")
    public @NotNull Vec3 pow(int i) {
        return new Vec3(Math.pow(this.x, i), Math.pow(this.y, i), Math.pow(this.z, i));
    }

    /**
     * Converts the Vec3 into Vec3i. This causes the x, y, and z values to be rounded, and loses precision.
     * @return A new Vec3i with the rounded values.
     */
    @Contract(" -> new")
    public @NotNull Vec3i toVec3i() {
        return new Vec3i((int) x, (int) y, (int) z);
    }
}
