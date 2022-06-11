package net.blueberrymc.util;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ComponentGetter {
    @NotNull
    Component getComponent();
}
