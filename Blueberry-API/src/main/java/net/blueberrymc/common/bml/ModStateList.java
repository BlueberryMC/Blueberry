package net.blueberrymc.common.bml;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

public class ModStateList extends ArrayList<ModState> {
    @Override
    public String toString() {
        synchronized (this) {
            StringBuilder sb = new StringBuilder();
            this.forEach(state -> sb.append(state.getShortName()));
            return sb.toString();
        }
    }

    @NotNull
    public ModState getCurrentState() {
        synchronized (this) {
            return this.get(this.size() - 1);
        }
    }

    @Override
    public boolean add(@NotNull ModState modState) {
        synchronized (this) {
            return super.add(modState);
        }
    }

    @NotNull
    @Override
    public ModState set(int index, @NotNull ModState element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, @NotNull ModState element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends ModState> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends ModState> c) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public ModState remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(@NotNull Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(@NotNull Predicate<? super ModState> filter) {
        throw new UnsupportedOperationException();
    }
}
