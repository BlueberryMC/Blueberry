package net.blueberrymc.tags;

import net.blueberrymc.registry.Registry;
import net.blueberrymc.common.internal.util.ImplGetter;
import net.blueberrymc.registry.ResourceKey;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface TagKey<T> {
    /**
     * Creates a new tag key.
     * @param resourceKey the resource key
     * @param key the location
     * @return the tag key
     * @param <T> type of tag
     */
    @SuppressWarnings("unchecked")
    @Contract("_, _ -> new")
    static <T> @NotNull TagKey<T> create(@NotNull ResourceKey<? extends Registry<T>> resourceKey, @NotNull Key key) {
        return (TagKey<T>) ImplGetter.byMethod("create", ResourceKey.class, Key.class).apply(resourceKey, key);
    }

    @NotNull ResourceKey<? extends Registry<T>> registry();

    @NotNull Key location();
}
