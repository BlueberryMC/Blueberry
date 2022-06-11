package net.blueberrymc.tags;

import net.blueberrymc.common.Registry;
import net.blueberrymc.common.internal.util.ImplGetter;
import net.blueberrymc.registry.ResourceKey;
import net.blueberrymc.world.level.fluid.Fluid;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface TagKey<T> {
    @SuppressWarnings("unchecked")
    static <T> TagKey<T> create(@NotNull ResourceKey<? extends Registry<Fluid>> resourceKey, @NotNull Key key) {
        return (TagKey<T>) ImplGetter.byMethod("create", ResourceKey.class, Key.class).apply(resourceKey, key);
    }
}
