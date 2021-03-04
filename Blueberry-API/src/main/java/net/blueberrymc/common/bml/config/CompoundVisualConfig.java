package net.blueberrymc.common.bml.config;

import com.google.common.base.Preconditions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class CompoundVisualConfig extends VisualConfig<List<VisualConfig<?>>> implements Iterable<VisualConfig<?>> {
    private final List<VisualConfig<?>> children = new ArrayList<>();
    private CompoundVisualConfig parent;
    public Component title;

    public CompoundVisualConfig(@Nullable Component component) {
        this(component, null);
    }

    public CompoundVisualConfig(@Nullable Component component, @Nullable CompoundVisualConfig parent) {
        super(component);
        this.parent = parent;
    }

    @NotNull
    public CompoundVisualConfig withTitle(@Nullable Component title) {
        this.title = title;
        return this;
    }

    @NotNull
    public CompoundVisualConfig withTitle(@Nullable String title) {
        this.title = title == null ? null : new TextComponent(title);
        return this;
    }

    @Nullable
    public Component getTitle() {
        return title;
    }

    @Nullable
    public CompoundVisualConfig getParent() {
        return parent;
    }

    @Nullable
    public RootCompoundVisualConfig getRoot() {
        if (this instanceof RootCompoundVisualConfig) return (RootCompoundVisualConfig) this;
        CompoundVisualConfig parent = this;
        while ((parent = parent.getParent()) != null) {
            if (parent instanceof RootCompoundVisualConfig) return (RootCompoundVisualConfig) parent;
        }
        return null;
    }

    @Nullable
    public Component getParentTitle() {
        return parent == null ? null : parent.getTitle();
    }

    @NotNull
    @Override
    public List<VisualConfig<?>> get() {
        return children;
    }

    @Override
    public void set(@Nullable List<VisualConfig<?>> value) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public List<VisualConfig<?>> getDefaultValue() {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return children.size();
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    @NotNull
    public CompoundVisualConfig add(@NotNull VisualConfig<?> config) {
        Preconditions.checkNotNull(config, "Cannot add null config");
        if (config instanceof CompoundVisualConfig) {
            ((CompoundVisualConfig) config).parent = this;
        }
        children.add(config);
        return this;
    }

    public void clear() {
        children.clear();
    }

    @NotNull
    public VisualConfig<?> removeAt(int index) throws IndexOutOfBoundsException {
        return children.remove(index);
    }

    @Override
    public void forEach(@NotNull Consumer<? super VisualConfig<?>> action) {
        Preconditions.checkNotNull(action, "Consumer cannot be null");
        children.forEach(action);
    }

    @NotNull
    @Override
    public Iterator<VisualConfig<?>> iterator() {
        return this.children.iterator();
    }
}
