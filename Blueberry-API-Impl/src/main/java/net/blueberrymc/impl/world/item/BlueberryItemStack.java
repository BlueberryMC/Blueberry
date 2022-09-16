package net.blueberrymc.impl.world.item;

import net.blueberrymc.impl.nbt.TagCompoundImpl;
import net.blueberrymc.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
        Item itemHandle;
        if (stack.item() instanceof BlueberryItem) {
            itemHandle = ((BlueberryItem) stack.item()).getHandle();
        } else {
            // TODO: this will NOT work for custom Item implementations
            throw new UnsupportedOperationException("Custom items are not supported yet");
        }
        Optional<CompoundTag> tag = Optional.ofNullable((TagCompoundImpl) stack.tag()).map(TagCompoundImpl::getHandle);
        net.minecraft.world.item.ItemStack mcStack = new net.minecraft.world.item.ItemStack(itemHandle, stack.amount());
        tag.ifPresent(mcStack::setTag);
        return mcStack;
    }
}
