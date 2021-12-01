package net.blueberrymc.util;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface NameGetter {
    @NotNull
    String getName();
}
