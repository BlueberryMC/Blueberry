package net.blueberrymc.common;

import java.util.Locale;

public enum Side {
    CLIENT,
    SERVER,
    BOTH,
    ;

    public String getName() { return name().toLowerCase(Locale.ROOT); }
}
