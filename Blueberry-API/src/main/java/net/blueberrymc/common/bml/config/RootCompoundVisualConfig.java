package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class RootCompoundVisualConfig extends CompoundVisualConfig {
    public Consumer<CompoundVisualConfig> onSave = null;

    public RootCompoundVisualConfig(Component component) {
        super(component, null); // this is root, there is no parent
    }

    public void onChanged() {
        if (onSave != null) onSave.accept(this);
    }
}
