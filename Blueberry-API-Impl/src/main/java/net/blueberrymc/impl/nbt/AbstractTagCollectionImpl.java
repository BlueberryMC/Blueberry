package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.AbstractTagCollection;
import net.blueberrymc.nbt.Tag;
import net.blueberrymc.util.IAbstractCollection;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTagCollectionImpl<E extends Tag> extends TagImpl implements AbstractTagCollection<E>, IAbstractCollection<E> {
    public AbstractTagCollectionImpl(net.minecraft.nbt.@NotNull Tag handle) {
        super(handle);
    }
}
