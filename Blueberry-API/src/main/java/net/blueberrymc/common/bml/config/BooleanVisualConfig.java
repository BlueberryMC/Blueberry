package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class BooleanVisualConfig extends VisualConfig<Boolean> {
    public BooleanVisualConfig(@Nullable Component component) {
        this(component, false);
    }

    public BooleanVisualConfig(@Nullable Component component, boolean initialValue) {
        super(component, initialValue);
    }
}
