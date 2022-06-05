package net.blueberrymc.impl.common.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class MinecraftTextComponent implements TextComponent {
    @Override
    public @NotNull String content() {
        return null;
    }

    @Override
    public @NotNull TextComponent content(@NotNull String content) {
        return null;
    }

    @Override
    public @NotNull Builder toBuilder() {
        return null;
    }

    @Override
    public @Unmodifiable @NotNull List<Component> children() {
        return null;
    }

    @Override
    public @NotNull TextComponent children(@NotNull List<? extends ComponentLike> children) {
        return null;
    }

    @Override
    public @NotNull Style style() {
        return null;
    }

    @Override
    public @NotNull TextComponent style(@NotNull Style style) {
        return null;
    }
}
