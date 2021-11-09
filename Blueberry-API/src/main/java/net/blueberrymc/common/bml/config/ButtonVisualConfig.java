package net.blueberrymc.common.bml.config;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public abstract class ButtonVisualConfig<V extends ButtonVisualConfig<V, T>, T> extends VisualConfig<T> {
    protected BiConsumer<V, Button> onClick = null;

    protected ButtonVisualConfig(@Nullable Component component) {
        super(component);
    }

    protected ButtonVisualConfig(@Nullable Component component, @Nullable T initialValue, @Nullable T defaultValue) {
        super(component, initialValue, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public void clicked(@NotNull Button button) {
        if (onClick != null) onClick.accept((V) this, button);
    }

    @SuppressWarnings("unchecked")
    @Contract("_ -> this")
    @NotNull
    public V onClick(@Nullable BiConsumer<V, Button> handler) {
        onClick = handler;
        return (V) this;
    }
}
