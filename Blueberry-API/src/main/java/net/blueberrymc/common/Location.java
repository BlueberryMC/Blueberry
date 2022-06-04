package net.blueberrymc.common;

import com.google.common.base.Preconditions;
import net.blueberrymc.util.Vec3;
import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.Chunk;
import net.blueberrymc.world.World;
import net.blueberrymc.world.level.block.Block;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * Represents the specific location.
 */
public class Location {
    @Nullable
    private Reference<World> world;
    private double x;
    private double y;
    private double z;
    private float yaw; // yRot
    private float pitch; // xRot

    /**
     * Constructs a new location.
     * @param world the level
     * @param x x pos
     * @param y y pos
     * @param z z pos
     * @param yaw yaw (yRot)
     * @param pitch pitch (xRot)
     */
    public Location(@Nullable World world, double x, double y, double z, float yaw, float pitch) {
        if (world != null) {
            this.world = new WeakReference<>(world);
        }
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Constructs a new location with 0 yaw and 0 pitch.
     * @param world the level
     * @param x x pos
     * @param y y pos
     * @param z z pos
     */
    public Location(@Nullable World world, double x, double y, double z) {
        this(world, x, y, z, 0.0F, 0.0F);
    }

    /**
     * Constructs a new location with provided BlockPos, 0 yaw, and 0 pitch.
     * @param world the level
     * @param pos block position
     */
    public Location(@Nullable World world, @NotNull Vec3 pos) {
        this(world, Preconditions.checkNotNull(pos, "blockPos cannot be null").x(), pos.y(), pos.z());
    }

    /**
     * Constructs a new location with provided BlockPos, 0 yaw, and 0 pitch.
     * @param world the level
     * @param pos block position
     */
    public Location(@Nullable World world, @NotNull Vec3i pos) {
        this(world, Preconditions.checkNotNull(pos, "blockPos cannot be null").x(), pos.y(), pos.z());
    }

    /**
     * Constructs a new location with provided x, y, z, null world, 0 yaw, and 0 pitch.
     * @param x x pos
     * @param y y pos
     * @param z z pos
     */
    public Location(double x, double y, double z) {
        this(null, x, y, z);
    }

    /**
     * Constructs a new location with provided x, y, z, yaw, pitch, and null level.
     * @param x x pos
     * @param y y pos
     * @param z z pos
     * @param yaw yaw (yRot)
     * @param pitch pitch (xRot)
     */
    public Location(double x, double y, double z, float yaw, float pitch) {
        this(null, x, y, z, yaw, pitch);
    }

    /**
     * Sets a new world.
     * @param world the world
     */
    public void setWorld(@Nullable World world) {
        if (world != null) {
            this.world = new WeakReference<>(world);
        } else {
            this.world = null;
        }
    }

    /**
     * Returns the level.
     * @throws IllegalArgumentException if level is unloaded
     * @return the level contains this location or null if it is not set
     */
    @Nullable
    public World getWorld() {
        return this.getLevel(false);
    }

    /**
     * Returns the level.
     * @param notNull whether the method should return non-null value
     * @return the level
     */
    @Nullable
    @Contract("true -> !null")
    public World getLevel(boolean notNull) {
        if (this.world == null) {
            if (notNull) {
                throw new NullPointerException();
            } else {
                return null;
            }
        }
        World world = this.world.get();
        if (world == null) throw new IllegalArgumentException("World unloaded");
        return world;
    }

    /**
     * Checks if the location at the location in the level is loaded.
     * @throws NullPointerException if level is null or unloaded
     * @return true if loaded, false otherwise
     */
    public boolean isLoaded() {
        return getLevel(true).isLoaded(toVec3i());
    }

    /**
     * Checks if the level is loaded.
     * @return true if loaded, false otherwise
     */
    public boolean isLevelLoaded() {
        if (this.world == null) return false;
        World world = this.world.get();
        return world != null;
    }

    /**
     * Returns the chunk if loaded.
     * @throws NullPointerException if level is null or unloaded
     * @return chunk if loaded, null otherwise
     */
    @Nullable
    public Chunk getChunkIfLoaded() {
        return getLevel(true).getChunkIfLoaded((int) x >> 4, (int) z >> 4);
    }

    /**
     * Returns the chunk at the location. This method causes the chunk to load or generate if not loaded.
     * @return the chunk
     */
    @NotNull
    public Chunk getChunk() {
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

    /**
     * Set the yaw (yRot) of this location.
     * @param yaw yaw (yRot)
     * @return this location
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Location setYaw(float yaw) {
        this.yaw = yaw;
        return this;
    }

    /**
     * Returns the yaw (yRot).
     * @return yaw (yRot)
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Alias for {@link #setYaw(float)}.
     * @param yRot yRot (yaw)
     * @return this location
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Location setYRot(float yRot) {
        return setYaw(yRot);
    }

    /**
     * Alias for {@link #getYaw()}.
     * @return yRot (yaw)
     */
    public float getYRot() {
        return getYaw();
    }

    /**
     * Set the pitch (xRot) of this location.
     * @param pitch pitch (xRot)
     * @return this location
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Location setPitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    /**
     * Returns the pitch (xRot)
     * @return pitch (xRot)
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Alias for {@link #setPitch(float)}.
     * @param xRot xRot (pitch)
     * @return this location
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Location setXRot(float xRot) {
        return setPitch(xRot);
    }

    /**
     * Alias for {@link #getPitch()}.
     * @return xRot (pitch)
     */
    public float getXRot() {
        return getPitch();
    }

    /**
     * Returns the block X (x pos with <code>Math.floor</code>)
     * @return block X
     */
    public int getBlockX() {
        return (int) Math.floor(x);
    }

    /**
     * Returns the block Y (y pos with <code>Math.floor</code>)
     * @return block Y
     */
    public int getBlockY() {
        return (int) Math.floor(y);
    }

    /**
     * Returns the block Z (z pos with <code>Math.floor</code>)
     * @return block Z
     */
    public int getBlockZ() {
        return (int) Math.floor(z);
    }

    /**
     * Returns the chunk X (block X pos >> 4)
     * @return chunk X pos
     */
    public int getChunkX() {
        return getBlockX() >> 4;
    }

    /**
     * Returns the chunk Y (block Y pos >> 4)
     * @return chunk Y pos
     */
    public int getChunkY() {
        return getBlockY() >> 4;
    }

    /**
     * Returns the chunk Z (block Z pos >> 4)
     * @return chunk Z pos
     */
    public int getChunkZ() {
        return getBlockZ() >> 4;
    }

    /**
     * Returns the (wrapped) block for the location.
     * @return the block
     */
    @NotNull
    public Block getBlock() {
        return new Block(getLevel(true), toVec3i());
    }

    /**
     * Converts the location into {@link Vec3}.
     * @return the block pos
     */
    @NotNull
    public Vec3 toVec3() {
        return new Vec3(x, y, z);
    }

    /**
     * Converts the location into {@link Vec3i}.
     * @return the block pos
     */
    @NotNull
    public Vec3i toVec3i() {
        return new Vec3i(getBlockX(), getBlockY(), getBlockZ());
    }

    @NotNull
    @Override
    public String toString() {
        return "Location{" +
                "level=" + world +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                '}';
    }

    /**
     * Similar to {@link #toString()} but this method returns block pos instead and doesn't have yaw and pitch.
     * @return block string
     */
    @NotNull
    public String toBlockString() {
        return "Location{" +
                "level=" + world +
                ", x=" + getBlockX() +
                ", y=" + getBlockY() +
                ", z=" + getBlockZ() +
                '}';
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(location.x, x) == 0 && Double.compare(location.y, y) == 0 && Double.compare(location.z, z) == 0 && Float.compare(location.yaw, yaw) == 0 && Float.compare(location.pitch, pitch) == 0 && Objects.equals(world, location.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z, yaw, pitch);
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
