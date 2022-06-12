package net.blueberrymc.impl.tags;

import net.blueberrymc.registry.Registry;
import net.blueberrymc.impl.registry.BlueberryResourceKey;
import net.blueberrymc.impl.util.KeyUtil;
import net.blueberrymc.registry.ResourceKey;
import net.blueberrymc.tags.TagKey;
import net.blueberrymc.util.Reflected;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record BlueberryTagKey<T, M>(@NotNull net.minecraft.tags.TagKey<M> handle) implements TagKey<T> {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Contract("_, _ -> new")
    @Reflected
    public static <T, M> @NotNull BlueberryTagKey<T, M> create(@NotNull ResourceKey<? extends Registry<T>> resourceKey, @NotNull Key key) {
        var minecraftResourceKey = ((BlueberryResourceKey) resourceKey).handle();
        return new BlueberryTagKey<>(net.minecraft.tags.TagKey.create(minecraftResourceKey, KeyUtil.toMinecraft(key)));
    }

    @Contract(" -> new")
    @Override
    public @NotNull ResourceKey<? extends Registry<T>> registry() {
        return new BlueberryResourceKey<>(handle.registry());
    }

    @Override
    public @NotNull Key location() {
        return KeyUtil.toAdventure(handle.location());
    }
}
