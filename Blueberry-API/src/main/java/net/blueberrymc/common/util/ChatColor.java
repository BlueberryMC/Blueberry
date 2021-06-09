package net.blueberrymc.common.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum ChatColor {
    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    DARK_PURPLE('5'),
    GOLD('6'),
    GRAY('7'),
    DARK_GRAY('8'),
    BLUE('9'),
    GREEN('a'),
    AQUA('b'),
    RED('c'),
    LIGHT_PURPLE('d'),
    YELLOW('e'),
    WHITE('f'),
    ;

    public static final char SECTION = '\u00a7';

    private final String toString;

    ChatColor(char code) {
        this.toString = Character.toString(SECTION) + code;
    }

    public static @NotNull String translate(@NotNull String s) {
        return s.replaceAll("&", String.valueOf(SECTION));
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public String toString() {
        return toString;
    }
}
