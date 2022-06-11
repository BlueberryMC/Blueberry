package net.blueberrymc.registry;

import net.blueberrymc.common.Registry;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class DeferredRegister<T> {
    @NotNull
    private final BlueberryRegistries<T> registry;
    @Subst("blueberry")
    @NotNull
    private final String modId;

    private DeferredRegister(@NotNull BlueberryRegistries<T> registry, @NotNull String modId) {
        this.registry = registry;
        this.modId = modId;
    }

    @Contract(value = "_, _ -> new", pure = true)
    @NotNull
    public static <T> DeferredRegister<T> create(@NotNull BlueberryRegistries<T> registry, @NotNull String modId) {
        return new DeferredRegister<>(registry, modId);
    }

    @NotNull
    public BlueberryRegistries<T> getBlueberryRegistry() {
        return registry;
    }

    @NotNull
    public String getModId() {
        return modId;
    }

    @NotNull
    public Registry<T> getRegistry() { return registry.getRegistry(); }

    private final Set<Map.Entry<Key, Supplier<? extends T>>> suppliers = Collections.synchronizedSet(new HashSet<>());

    @NotNull
    public <R extends T> RegistryObject<R> register(@Subst("item") @NotNull String name, @NotNull Supplier<R> sup) {
        Key location = Key.key(modId, name);
        RegistryObject<R> object = new RegistryObject<>(sup).setKey(location);
        suppliers.add(new AbstractMap.SimpleImmutableEntry<>(location, object));
        return object;
    }

    public void registerAll() {
        suppliers.forEach(entry -> registry.register(entry.getKey(), entry.getValue().get()));
        suppliers.clear();
    }
}
