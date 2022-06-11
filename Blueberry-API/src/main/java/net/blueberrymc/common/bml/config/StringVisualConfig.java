package net.blueberrymc.common.bml.config;

import net.kyori.adventure.text.Component;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class StringVisualConfig extends VisualConfig<String> {
    private final int min;
    private final int max;
    @Nullable private Pattern pattern = null;

    public StringVisualConfig(@Nullable Component component) {
        this(component, null, null);
    }

    public StringVisualConfig(@Nullable Component component, @Nullable String initialValue, @Nullable String defaultValue) {
        this(component, initialValue, defaultValue, 0, 0);
    }

    public StringVisualConfig(@Nullable Component component, @Nullable String initialValue, @Nullable String defaultValue, int min, int max) {
        super(component, initialValue, defaultValue);
        if (min < 0) throw new IllegalArgumentException("min < 0");
        if (max < 0) throw new IllegalArgumentException("max < 0");
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Nullable
    public Pattern getPattern() {
        return pattern;
    }

    @NotNull
    public StringVisualConfig pattern(@Nullable Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    @NotNull
    public StringVisualConfig pattern(@Language("RegExp") @Nullable String pattern) {
        return pattern(pattern != null ? Pattern.compile(pattern) : null);
    }

    @Contract("null -> false")
    public boolean isValid(@Nullable String s) {
        if (s == null) return false;
        if (s.length() < min || (max > 0 && s.length() > max)) return false;
        return this.pattern == null || this.pattern.matcher(s).matches();
    }
}
