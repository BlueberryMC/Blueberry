package net.blueberrymc.world.item;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    @Override
    public abstract Component getName(@NotNull ItemStack itemStack);
}
