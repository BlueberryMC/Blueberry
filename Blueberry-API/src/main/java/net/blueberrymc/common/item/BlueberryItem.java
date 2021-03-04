package net.blueberrymc.common.item;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    @Override
    public abstract Component getName(@NotNull ItemStack itemStack);
}
