package net.blueberrymc.common;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.blueberrymc.world.level.block.BlockData;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface Registry<T> {
    Registry<BlockData> BLOCK = ofUnsafe("BLOCK", BlockData::ofUnsafe, ImplGetter::getHandleOf);

    @SuppressWarnings("unchecked")
    @Contract("_, _, _ -> new")
    @ApiStatus.Internal
    static <T> @NotNull Registry<T> ofUnsafe(@NotNull String name, @NotNull Function<Object, T> valueMapper, @NotNull Function<T, Object> valueUnmapper) {
        // -> BlueberryRegistry#ofUnsafe(String, Function)
        return (Registry<T>) ImplGetter.byMethod("ofUnsafe", String.class, Function.class).apply(name, valueMapper, valueUnmapper);
    }

    @SuppressWarnings("unchecked")
    @Contract("_, _, _ -> param3")
    static <R extends T, T> R register(@NotNull Registry<T> registry, @NotNull Key location, @Nullable R object) {
        return (R) ImplGetter.byMethod("register", Registry.class, Key.class, Object.class).apply(registry, location, object);
    }

    @NotNull
    Function<Object, T> valueMapper();

    @NotNull
    Function<T, Object> valueUnmapper();

    @Nullable
    T get(@Nullable Key key);

    @NotNull
    T getValueOrThrow(@NotNull Key key);

    @Nullable
    Key getKey(@NotNull T value);

    @Nullable
    T byId(int id);

    int getId(@NotNull T value);
}
