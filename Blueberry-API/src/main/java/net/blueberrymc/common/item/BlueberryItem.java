package net.blueberrymc.common.item;

import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class BlueberryItem extends Item {
    private final BlueberryMod mod;

    public BlueberryItem(BlueberryMod mod, Properties properties) {
        super(properties);
        this.mod = mod;
    }

    public BlueberryMod getMod() {
        return mod;
    }

    @Override
    public abstract Component getName(ItemStack itemStack);
}
