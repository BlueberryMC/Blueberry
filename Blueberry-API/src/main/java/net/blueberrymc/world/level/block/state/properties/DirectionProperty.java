package net.blueberrymc.world.level.block.state.properties;

import net.blueberrymc.world.level.block.BlockFace;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class DirectionProperty extends EnumProperty<BlockFace> {
    private static final List<BlockFace> VALUES = List.of(BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);

    protected DirectionProperty(@NotNull String name, @NotNull Collection<BlockFace> values) {
        super(name, BlockFace.class, values);
    }

    @Contract("_ -> new")
    public static @NotNull DirectionProperty create(@NotNull String name) {
        return new DirectionProperty(name, VALUES);
    }

    @Contract("_, _ -> new")
    public static @NotNull DirectionProperty create(@NotNull String name, @NotNull Predicate<BlockFace> filterFunction) {
        return new DirectionProperty(name, VALUES.stream().filter(filterFunction).toList());
    }

    @Contract("_, _ -> new")
    public static @NotNull DirectionProperty create(@NotNull String name, @NotNull BlockFace @NotNull ... values) {
        return new DirectionProperty(name, Arrays.stream(values).filter(VALUES::contains).toList());
    }

    @Contract("_, _ -> new")
    public static @NotNull DirectionProperty create(@NotNull String name, @NotNull Collection<BlockFace> values) {
        return new DirectionProperty(name, values.stream().filter(VALUES::contains).toList());
    }
}
