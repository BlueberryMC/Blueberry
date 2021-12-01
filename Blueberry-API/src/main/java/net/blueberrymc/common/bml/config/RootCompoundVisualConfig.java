package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RootCompoundVisualConfig extends CompoundVisualConfig {
    /**
     * @deprecated Use {@link #onSave(Consumer)} instead.
     */
    @Deprecated
    public Consumer<CompoundVisualConfig> onSave = null;

    public RootCompoundVisualConfig(@Nullable Component component) {
        super(component, null); // this is root, there is no parent
    }

    @NotNull
    public RootCompoundVisualConfig onSave(@Nullable Consumer<CompoundVisualConfig> onSave) {
        this.onSave = onSave;
        return this;
    }

    // you can also override it
    public void onChanged() {
        if (onSave != null) onSave.accept(this);
    }
}
