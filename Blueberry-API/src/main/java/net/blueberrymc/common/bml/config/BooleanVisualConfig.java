package net.blueberrymc.common.bml.config;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class BooleanVisualConfig extends ButtonVisualConfig<BooleanVisualConfig, Boolean> {
    public BooleanVisualConfig(@Nullable Component component) {
        this(component, null, null);
    }

    public BooleanVisualConfig(@Nullable Component component, @Nullable Boolean initialValue, @Nullable Boolean defaultValue) {
        super(component, initialValue, defaultValue);
    }
}
