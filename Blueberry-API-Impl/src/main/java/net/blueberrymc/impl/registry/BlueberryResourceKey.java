package net.blueberrymc.impl.registry;

import net.blueberrymc.impl.util.KeyUtil;
import net.blueberrymc.registry.ResourceKey;
import net.blueberrymc.util.Reflected;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public record BlueberryResourceKey<T, M>(@NotNull net.minecraft.resources.ResourceKey<M> handle) implements ResourceKey<T> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Contract("_ -> new")
    @Reflected
    public static <T, M> @NotNull BlueberryResourceKey<Registry<T>, M> createRegistryKey(@NotNull Key key) {
        return new BlueberryResourceKey(net.minecraft.resources.ResourceKey.createRegistryKey(KeyUtil.toMinecraft(key)));
    }

    @SuppressWarnings("unchecked")
    @Contract("_ -> new")
    @Reflected
    public static <T, M> @NotNull BlueberryResourceKey<T, M> ofUnsafeRegistryField(@NotNull String name) {
        try {
            Field field = Registry.class.getDeclaredField(name);
            if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isStatic(field.getModifiers())) {
                throw new RuntimeException("Attempted to get ResourceKey " + name + " which is not public static");
            }
            Object o = field.get(null);
            if (!(o instanceof ResourceKey<?>)) {
                throw new RuntimeException(name + " is not an instance of ResourceKey");
            }
            return new BlueberryResourceKey<>((net.minecraft.resources.ResourceKey<M>) o);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Key registryName() {
        return KeyUtil.toAdventure(handle.registry());
    }

    @Override
    public @NotNull Key location() {
        return KeyUtil.toAdventure(handle.location());
    }
}
