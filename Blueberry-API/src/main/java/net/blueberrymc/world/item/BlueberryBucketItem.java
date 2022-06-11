package net.blueberrymc.world.item;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.BlueberryMod;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public abstract class BlueberryBucketItem extends BucketItem {
    private final BlueberryMod mod;

    public BlueberryBucketItem(@NotNull("mod") BlueberryMod mod, @NotNull("fluid") Fluid fluid, @NotNull("properties") Properties properties) {
        super(fluid, properties);
        Preconditions.checkNotNull(mod, "mod cannot be null");
        this.mod = mod;
    }

    @NotNull
    public final BlueberryMod getMod() {
        return mod;
    }

    @NotNull
    @Override
    public abstract Component getName(@NotNull ItemStack itemStack);
}
