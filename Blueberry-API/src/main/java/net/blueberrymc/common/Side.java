package net.blueberrymc.common;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum Side {
    CLIENT,
    SERVER,
    BOTH,
    ;

    @NotNull
    public String getName() { return name().toLowerCase(Locale.ROOT); }
}
