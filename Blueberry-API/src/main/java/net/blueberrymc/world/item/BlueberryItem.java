package net.blueberrymc.world.item;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.BlueberryMod;
import org.jetbrains.annotations.NotNull;

public abstract class BlueberryItem extends Item {
    private final BlueberryMod mod;

    public BlueberryItem(@NotNull("mod") BlueberryMod mod, @NotNull("properties") Properties properties) {
        super(properties);
        Preconditions.checkNotNull(mod, "mod cannot be null");
        this.mod = mod;
    }

    @NotNull
    public final BlueberryMod getMod() {
        return mod;
    }
}
