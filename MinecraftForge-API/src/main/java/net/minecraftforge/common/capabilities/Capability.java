package net.minecraftforge.common.capabilities;

import com.google.common.base.Throwables;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;

public class Capability<T> {
    public static interface IStorage<T> {
        @Nullable
        Tag writeNBT(Capability<T> capability, T instance, Direction side);

        void readNBT(Capability<T> capability, T instance, Direction side, Tag nbt);
    }

    public String getName() { return name; }

    public IStorage<T> getStorage() { return storage; }

    public void readNBT(T instance, Direction side, Tag nbt) {
        storage.readNBT(this, instance, side, nbt);
    }

    @Nullable
    public Tag writeNBT(T instance, Direction side) {
        return storage.writeNBT(this, instance, side);
    }

    /**
     * A NEW instance of the default implementation.
     *
     * If it important to note that if you want to use the default storage
     * you may be required to use this exact implementation.
     * Refer to the owning API of the Capability in question.
     *
     * @return A NEW instance of the default implementation.
     */
    @Nullable
    public T getDefaultInstance() {
        try {
            return this.factory.call();
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public <R> LazyOptional<R> orEmpty(Capability<R> toCheck, LazyOptional<T> inst) {
        return this == toCheck ? inst.cast() : LazyOptional.empty();
    }

    // INTERNAL
    private final String name;
    private final IStorage<T> storage;
    private final Callable<? extends T> factory;

    Capability(String name, IStorage<T> storage, Callable<? extends T> factory) {
        this.name = name;
        this.storage = storage;
        this.factory = factory;
    }
}
