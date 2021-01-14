package net.blueberrymc.common.bml;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ModState {
    LOADED('L'),
    PRE_INIT('P'),
    INIT('I'),
    POST_INIT('J'),
    AVAILABLE('A'),
    ERRORED('E'),
    UNLOADED('U'),
    ;

    private final char shortName;

    ModState(char shortName) {
        this.shortName = shortName;
    }

    public char getShortName() {
        return shortName;
    }

    private String cachedName = null;

    @NotNull
    public String getName() {
        if (cachedName == null) {
            cachedName = StringUtils.capitalize(name().toLowerCase(Locale.ROOT));
        }
        return cachedName;
    }

    @NotNull
    public String getShortNameAsString() {
        return Character.toString(shortName);
    }
}
