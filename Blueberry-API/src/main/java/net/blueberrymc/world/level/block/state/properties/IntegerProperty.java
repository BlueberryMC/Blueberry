package net.blueberrymc.world.level.block.state.properties;

import net.blueberrymc.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.IntStream;

public class IntegerProperty extends BlockState.Property<Integer> {
    private final Collection<Integer> values;
    private final int min;
    private final int max;

    protected IntegerProperty(@NotNull String name, int min, int max) {
        super(name, Integer.class);
        if (min < 0) {
            throw new IllegalArgumentException("Min value of " + name + " must be 0 or greater");
        } else if (max <= min) {
            throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + ")");
        } else {
            this.min = min;
            this.max = max;
            this.values = IntStream.rangeClosed(min, max).boxed().toList();
        }
    }

    @Contract("_, _, _ -> new")
    public static @NotNull IntegerProperty create(@NotNull String name, int min, int max) {
        return new IntegerProperty(name, min, max);
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    @Override
    public @NotNull Collection<Integer> getPossibleValues() {
        return values;
    }

    @Override
    public String getName(@NotNull Integer value) {
        return Integer.toString(value);
    }

    @Override
    public @NotNull Optional<Integer> getValue(@NotNull String s) {
        try {
            int value = Integer.parseInt(s);
            if (value >= min && value <= max) {
                return Optional.of(value);
            }
        } catch (NumberFormatException ignored) {
        }
        return Optional.empty();
    }
}
