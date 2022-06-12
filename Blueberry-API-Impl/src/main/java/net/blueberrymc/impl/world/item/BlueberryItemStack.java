package net.blueberrymc.impl.world.item;

import net.blueberrymc.impl.nbt.TagCompoundImpl;
import net.blueberrymc.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class BlueberryItemStack extends ItemStack {
    private final net.minecraft.world.item.ItemStack handle;

    public BlueberryItemStack(@NotNull net.minecraft.world.item.ItemStack handle) {
        super(new BlueberryItem(handle.getItem()), handle.getCount(), TagCompoundImpl.of(handle.getTag()));
        this.handle = handle;
    }

    @NotNull
    public net.minecraft.world.item.ItemStack getHandle() {
        return handle;
    }

    @Contract("_ -> new")
    public static @NotNull BlueberryItemStack toBlueberry(@NotNull net.minecraft.world.item.ItemStack stack) {
        return new BlueberryItemStack(stack);
    }

    public static @NotNull net.minecraft.world.item.ItemStack toMinecraft(@NotNull ItemStack stack) {
        // TODO: this will NOT work for handmade ItemStack
        return ((BlueberryItemStack) stack).handle;
    }
}
