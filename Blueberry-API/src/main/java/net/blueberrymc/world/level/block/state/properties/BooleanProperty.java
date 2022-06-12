package net.blueberrymc.world.level.block.state.properties;

import net.blueberrymc.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BooleanProperty extends BlockState.Property<Boolean> {
    private static final Collection<Boolean> VALUES = List.of(true, false);

    protected BooleanProperty(@NotNull String name) {
        super(name, Boolean.class);
    }

    @Contract("_ -> new")
    public static @NotNull BooleanProperty create(@NotNull String name) {
        return new BooleanProperty(name);
    }

    @Override
    public @NotNull Collection<Boolean> getPossibleValues() {
        return VALUES;
    }

    @Override
    public String getName(@NotNull Boolean value) {
        return Boolean.toString(value);
    }

    @Override
    public @NotNull Optional<Boolean> getValue(@NotNull String s) {
        if (s.equals("true")) {
            return Optional.of(true);
        }
        if (s.equals("false")) {
            return Optional.of(false);
        }
        return Optional.empty();
    }
}
