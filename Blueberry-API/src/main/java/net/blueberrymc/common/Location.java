package net.blueberrymc.common;

import com.google.common.base.Preconditions;
import com.mojang.math.Vector3d;
import net.blueberrymc.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;

public class Location {
    @Nullable
    private Reference<Level> level;
    private double x;
    private double y;
    private double z;
    private float yaw; // yRot
    private float pitch; // xRot

    public Location(@Nullable Level level, double x, double y, double z, float yaw, float pitch) {
        if (level != null) {
            this.level = new WeakReference<>(level);
        }
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location(@Nullable Level level, double x, double y, double z) {
        this(level, x, y, z, 0.0F, 0.0F);
    }

    public Location(@Nullable Level level, @NotNull BlockPos pos) {
        this(level, Preconditions.checkNotNull(pos, "blockPos cannot be null").getX(), pos.getY(), pos.getZ());
    }

    public void setLevel(@Nullable Level level) {
        if (level != null) {
            this.level = new WeakReference<>(level);
        } else {
            this.level = null;
        }
    }

    /**
     * Gets the level
     * @throws IllegalArgumentException when level is unloaded
     * @return the level contains this location or null if it is not set
     */
    @Nullable
    public Level getLevel() {
        return this.getLevel(false);
    }

    @Nullable
    @Contract("true -> !null")
    public Level getLevel(boolean notNull) {
        if (this.level == null) {
            if (notNull) {
                throw new NullPointerException();
            } else {
                return null;
            }
        }
        Level level = this.level.get();
        if (level == null) throw new IllegalArgumentException("Level unloaded");
        return level;
    }

    public boolean isLoaded() {
        return getLevel(true).isLoaded(toBlockPos());
    }

    public boolean isLevelLoaded() {
        if (this.level == null) return false;
        Level level = this.level.get();
        return level != null;
    }

    @Nullable
    public LevelChunk getChunk() {
        return getLevel(true).getChunk((int) x >> 4, (int) z >> 4);
    }

    @NotNull
    public Location setX(double x) {
        this.x = x;
        return this;
    }

    public double getX() {
        return x;
    }

    @NotNull
    public Location setY(double y) {
        this.y = y;
        return this;
    }

    public double getY() {
        return y;
    }

    @NotNull
    public Location setZ(double z) {
        this.z = z;
        return this;
    }

    public double getZ() {
        return z;
    }

    @NotNull
    public Location setYaw(float yaw) {
        this.yaw = yaw;
        return this;
    }

    public float getYaw() {
        return yaw;
    }

    @NotNull
    public Location setYRot(float yRot) {
        return setYaw(yRot);
    }

    public float getYRot() {
        return getYaw();
    }

    @NotNull
    public Location setPitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    public float getPitch() {
        return pitch;
    }

    @NotNull
    public Location setXRot(float xRot) {
        return setPitch(xRot);
    }

    public float getXRot() {
        return getPitch();
    }

    public int getBlockX() {
        return (int) Math.floor(x);
    }

    public int getBlockY() {
        return (int) Math.floor(y);
    }

    public int getBlockZ() {
        return (int) Math.floor(z);
    }

    public int getChunkX() {
        return getBlockX() >> 4;
    }

    public int getChunkY() {
        return getBlockY() >> 4;
    }

    public int getChunkZ() {
        return getBlockZ() >> 4;
    }

    @NotNull
    public Block getBlock() {
        return new Block(getLevel(true), toBlockPos());
    }

    @NotNull
    public BlockPos toBlockPos() {
        return new BlockPos(getBlockX(), getBlockY(), getBlockZ());
    }

    @NotNull
    public Vector3d toVector3d() {
        return new Vector3d(x, y, z);
    }

    @NotNull
    @Override
    public String toString() {
        return "Location{" +
                "level=" + level +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                '}';
    }

    @NotNull
    public String toBlockString() {
        return "Location{" +
                "level=" + level +
                ", x=" + getBlockX() +
                ", y=" + getBlockY() +
                ", z=" + getBlockZ() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(location.x, x) == 0 && Double.compare(location.y, y) == 0 && Double.compare(location.z, z) == 0 && Float.compare(location.yaw, yaw) == 0 && Float.compare(location.pitch, pitch) == 0 && Objects.equals(level, location.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, x, y, z, yaw, pitch);
    }

    /**
     * Normalizes the given yaw angle to a value between <code>+/-180</code>
     * degrees.
     *
     * @param yaw the yaw in degrees
     * @return the normalized yaw in degrees
     * @see Location#getYaw()
     */
    public static float normalizeYaw(float yaw) {
        yaw %= 360.0f;
        if (yaw >= 180.0f) {
            yaw -= 360.0f;
        } else if (yaw < -180.0f) {
            yaw += 360.0f;
        }
        return yaw;
    }

    /**
     * Normalizes the given pitch angle to a value between <code>+/-90</code>
     * degrees.
     *
     * @param pitch the pitch in degrees
     * @return the normalized pitch in degrees
     * @see Location#getPitch()
     */
    public static float normalizePitch(float pitch) {
        if (pitch > 90.0f) {
            pitch = 90.0f;
        } else if (pitch < -90.0f) {
            pitch = -90.0f;
        }
        return pitch;
    }
}
