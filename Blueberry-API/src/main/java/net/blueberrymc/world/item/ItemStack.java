package net.blueberrymc.world.item;

import net.blueberrymc.nbt.TagCompound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record ItemStack(@NotNull Item item, int amount, @Nullable TagCompound tag) {
    public ItemStack(@NotNull Item item, int amount) {
        this(item, amount, null);
    }

    public ItemStack(@NotNull Item item) {
        this(item, 1, null);
    }

    public ItemStack {
        Objects.requireNonNull(item, "item");
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull ItemStack withTag(@Nullable TagCompound tag) {
        return new ItemStack(item, amount, tag);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull ItemStack withAmount(int amount) {
        return new ItemStack(item, amount, tag);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull ItemStack withItem(@NotNull Item item) {
        return new ItemStack(item, amount, tag);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull ItemStack grow(int amount) {
        return new ItemStack(item, this.amount + amount, tag);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull ItemStack shrink(int amount) {
        return new ItemStack(item, this.amount - amount, tag);
    }
}
