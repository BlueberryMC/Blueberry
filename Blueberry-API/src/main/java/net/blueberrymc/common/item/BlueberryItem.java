package net.blueberrymc.common.item;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class BlueberryItem extends Item {
    private final BlueberryMod mod;

    public BlueberryItem(BlueberryMod mod, Properties properties) {
        super(properties);
        Preconditions.checkNotNull(mod, "mod cannot be null");
        this.mod = mod;
    }

    @NotNull
    public final BlueberryMod getMod() {
        return mod;
    }

    @Override
    public abstract Component getName(ItemStack itemStack);
}
