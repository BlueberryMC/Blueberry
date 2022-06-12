package net.blueberrymc.world.level.block.state.properties;

import net.blueberrymc.util.NameGetter;
import net.blueberrymc.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class EnumProperty<T extends Enum<T> & NameGetter> extends BlockState.Property<T> {
    private final Collection<T> values;
    private final Map<String, T> names = new HashMap<>();

    protected EnumProperty(@NotNull String name, @NotNull Class<T> clazz, @NotNull Collection<T> values) {
        super(name, clazz);
        this.values = Collections.unmodifiableCollection(values);

        for (T value : values) {
            String valueName = value.getName();
            if (names.containsKey(valueName)) {
                throw new IllegalArgumentException("Multiple values have the same name '" + valueName + "'");
            }

            names.put(valueName, value);
        }
    }

    @Override
    public @NotNull Collection<T> getPossibleValues() {
        return values;
    }

    @Override
    public String getName(@NotNull T value) {
        return value.getName();
    }

    @Override
    public @NotNull Optional<T> getValue(@NotNull String s) {
        return Optional.ofNullable(names.get(s));
    }

    @Contract("_, _ -> new")
    public static <T extends Enum<T> & NameGetter> @NotNull EnumProperty<T> create(@NotNull String name, @NotNull Class<T> clazz) {
        return new EnumProperty<>(name, clazz, List.of(clazz.getEnumConstants()));
    }

    @Contract("_, _, _ -> new")
    public static <T extends Enum<T> & NameGetter> @NotNull EnumProperty<T> create(@NotNull String name, @NotNull Class<T> clazz, @NotNull Predicate<T> filterFunction) {
        return new EnumProperty<>(name, clazz, Arrays.stream(clazz.getEnumConstants()).filter(filterFunction).toList());
    }

    @SafeVarargs
    @Contract("_, _, _ -> new")
    public static <T extends Enum<T> & NameGetter> @NotNull EnumProperty<T> create(@NotNull String name, @NotNull Class<T> clazz, @NotNull T @NotNull ... values) {
        return new EnumProperty<>(name, clazz, List.of(values));
    }

    @Contract("_, _, _ -> new")
    public static <T extends Enum<T> & NameGetter> @NotNull EnumProperty<T> create(@NotNull String name, @NotNull Class<T> clazz, @NotNull Collection<T> values) {
        return new EnumProperty<>(name, clazz, values);
    }
}
