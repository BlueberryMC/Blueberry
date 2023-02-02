package net.blueberrymc.world.level.block;

import net.blueberrymc.util.NameGetter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public enum BlockFace implements NameGetter {
    NORTH(0, 0, -1),
    EAST(1, 0, 0),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    UP(0, 1, 0),
    DOWN(0, -1, 0),
    NORTH_EAST(NORTH, EAST),
    NORTH_WEST(NORTH, WEST),
    SOUTH_EAST(SOUTH, EAST),
    SOUTH_WEST(SOUTH, WEST),
    WEST_NORTH_WEST(WEST, NORTH_WEST),
    NORTH_NORTH_WEST(NORTH, NORTH_WEST),
    NORTH_NORTH_EAST(NORTH, NORTH_EAST),
    EAST_NORTH_EAST(EAST, NORTH_EAST),
    EAST_SOUTH_EAST(EAST, SOUTH_EAST),
    SOUTH_SOUTH_EAST(SOUTH, SOUTH_EAST),
    SOUTH_SOUTH_WEST(SOUTH, SOUTH_WEST),
    WEST_SOUTH_WEST(WEST, SOUTH_WEST),
    SELF(0, 0, 0);

    private final int modX;
    private final int modY;
    private final int modZ;

    BlockFace(final int modX, final int modY, final int modZ) {
        this.modX = modX;
        this.modY = modY;
        this.modZ = modZ;
    }

    BlockFace(final BlockFace face1, final BlockFace face2) {
        this.modX = face1.getModX() + face2.getModX();
        this.modY = face1.getModY() + face2.getModY();
        this.modZ = face1.getModZ() + face2.getModZ();
    }

    /**
     * Get the amount of X-coordinates to modify to get the represented block
     *
     * @return Amount of X-coordinates to modify
     */
    public int getModX() {
        return modX;
    }

    /**
     * Get the amount of Y-coordinates to modify to get the represented block
     *
     * @return Amount of Y-coordinates to modify
     */
    public int getModY() {
        return modY;
    }

    /**
     * Get the amount of Z-coordinates to modify to get the represented block
     *
     * @return Amount of Z-coordinates to modify
     */
    public int getModZ() {
        return modZ;
    }

    @NotNull
    public BlockFace getOppositeFace() {
        return switch (this) {
            case NORTH -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.NORTH;
            case EAST -> BlockFace.WEST;
            case WEST -> BlockFace.EAST;
            case UP -> BlockFace.DOWN;
            case DOWN -> BlockFace.UP;
            case NORTH_EAST -> BlockFace.SOUTH_WEST;
            case NORTH_WEST -> BlockFace.SOUTH_EAST;
            case SOUTH_EAST -> BlockFace.NORTH_WEST;
            case SOUTH_WEST -> BlockFace.NORTH_EAST;
            case WEST_NORTH_WEST -> BlockFace.EAST_SOUTH_EAST;
            case NORTH_NORTH_WEST -> BlockFace.SOUTH_SOUTH_EAST;
            case NORTH_NORTH_EAST -> BlockFace.SOUTH_SOUTH_WEST;
            case EAST_NORTH_EAST -> BlockFace.WEST_SOUTH_WEST;
            case EAST_SOUTH_EAST -> BlockFace.WEST_NORTH_WEST;
            case SOUTH_SOUTH_EAST -> BlockFace.NORTH_NORTH_WEST;
            case SOUTH_SOUTH_WEST -> BlockFace.NORTH_NORTH_EAST;
            case WEST_SOUTH_WEST -> BlockFace.EAST_NORTH_EAST;
            case SELF -> BlockFace.SELF;
        };
    }

    @Override
    public @NotNull String getName() {
        return name();
    }

    public static final class Plane implements Iterable<BlockFace>, Predicate<BlockFace> {
        public static final Plane HORIZONTAL = new Plane(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
        public static final Plane VERTICAL = new Plane(BlockFace.UP, BlockFace.DOWN);

        private final List<BlockFace> faces;

        private Plane(final BlockFace... faces) {
            this.faces = List.of(faces);
        }

        @NotNull
        @Override
        public Iterator<BlockFace> iterator() {
            return faces.iterator();
        }

        @Override
        public boolean test(@Nullable BlockFace blockFace) {
            return faces.contains(blockFace);
        }
    }
}
