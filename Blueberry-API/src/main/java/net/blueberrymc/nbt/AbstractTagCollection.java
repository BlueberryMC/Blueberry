package net.blueberrymc.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@ApiStatus.NonExtendable
public interface AbstractTagCollection<T extends Tag> extends Collection<T>, Tag {
    @Contract("_, _ -> param2")
    @NotNull
    T set(int index, @NotNull T value);

    void add(int index, @NotNull T value);

    @NotNull
    T removeAt(int index);

    boolean setTag(int index, @NotNull Tag value);

    boolean addTag(int index, @NotNull Tag value);

    @NotNull
    Object toPrimitiveArray();
}
