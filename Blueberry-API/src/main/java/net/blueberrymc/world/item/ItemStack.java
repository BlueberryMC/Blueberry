package net.blueberrymc.world.item;

import net.blueberrymc.nbt.TagCompound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ItemStack {
    public static final ItemStack EMPTY = new ItemStack(Items.AIR, 0, null);
    @NotNull
    private final Item item;
    private final int amount;
    @Nullable
    private final TagCompound tag;

    public ItemStack(@NotNull Item item, int amount, @Nullable TagCompound tag) {
        Objects.requireNonNull(item, "item");
        this.item = item;
        this.amount = amount;
        this.tag = tag;
    }

    public ItemStack(@NotNull Item item, int amount) {
        this(item, amount, null);
    }

    public ItemStack(@NotNull Item item) {
        this(item, 1, null);
    }

    @NotNull
    public Item item() {
        return item;
    }

    public int amount() {
        return amount;
    }

    @Nullable
    public TagCompound tag() {
        return tag;
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull ItemStack tag(@Nullable TagCompound tag) {
        return new ItemStack(item, amount, tag);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull ItemStack amount(int amount) {
        return new ItemStack(item, amount, tag);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull ItemStack item(@NotNull Item item) {
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

    @Contract(pure = true)
    public boolean isEmpty() {
        return this == EMPTY || this.item.equals(Items.AIR) || this.amount == 0;
    }
}
