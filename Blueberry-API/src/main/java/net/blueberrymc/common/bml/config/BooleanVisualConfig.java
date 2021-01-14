package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;

public class BooleanVisualConfig extends VisualConfig<Boolean> {
    public BooleanVisualConfig(Component component) {
        this(component, false);
    }

    public BooleanVisualConfig(Component component, boolean initialValue) {
        super(component, initialValue);
    }
}
