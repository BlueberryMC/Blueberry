package net.blueberrymc.registry;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.blueberrymc.world.item.Item;
import net.blueberrymc.world.level.block.BlockData;
import net.blueberrymc.world.level.fluid.Fluid;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface Registry<T> {
    Registry<BlockData> BLOCK = ofUnsafe("BLOCK", BlockData::ofUnsafe, ImplGetter::getHandleOf);
    Registry<Item> ITEM = ofUnsafe("ITEM", Item::ofUnsafe, ImplGetter::getHandleOf);
    ResourceKey<Registry<Fluid>> FLUID_REGISTRY = ResourceKey.ofUnsafeRegistryField("FLUID_REGISTRY");

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

    /**
     * Try to get a value from this registry, or throw an exception if the value is not found.
     * @param key the key
     * @return the value
     * @throws IllegalArgumentException if the value is not found
     */
    @NotNull
    T getValueOrThrow(@NotNull Key key);

    @Nullable
    Key getKey(@NotNull T value);

    @Nullable
    T byId(int id);

    int getId(@NotNull T value);
}
