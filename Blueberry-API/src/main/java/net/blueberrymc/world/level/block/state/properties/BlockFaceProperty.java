package net.blueberrymc.world.level.block.state.properties;

import net.blueberrymc.world.level.block.BlockFace;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class BlockFaceProperty extends EnumProperty<BlockFace> {
    protected BlockFaceProperty(@NotNull String name, @NotNull Collection<BlockFace> values) {
        super(name, BlockFace.class, values);
    }

    @Contract("_ -> new")
    public static @NotNull BlockFaceProperty create(@NotNull String name) {
        return new BlockFaceProperty(name, List.of(BlockFace.values()));
    }

    @Contract("_, _ -> new")
    public static @NotNull BlockFaceProperty create(@NotNull String name, @NotNull Predicate<BlockFace> filterFunction) {
        return new BlockFaceProperty(name, Arrays.stream(BlockFace.values()).filter(filterFunction).toList());
    }

    @Contract("_, _ -> new")
    public static @NotNull BlockFaceProperty create(@NotNull String name, @NotNull BlockFace @NotNull ... values) {
        return new BlockFaceProperty(name, List.of(values));
    }

    @Contract("_, _ -> new")
    public static @NotNull BlockFaceProperty create(@NotNull String name, @NotNull Collection<BlockFace> values) {
        return new BlockFaceProperty(name, values);
    }
}
