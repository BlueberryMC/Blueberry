package net.blueberrymc.world.item;

import net.blueberrymc.nbt.TagCompound;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

public class ItemStackBuilder {
    public final Item item;
    public int amount;
    public int damageValue = 0;
    @Nullable public TagCompound tag;
    @Nullable public Component hoverName;
    @NotNull public final Map<Enchantment, Integer> enchantments = new ConcurrentHashMap<>();
    public int repairCost = 0;

    protected ItemStackBuilder(@NotNull Item item, int amount) {
        this.item = item;
        this.amount = amount;
    }

    @Contract(pure = true)
    @NotNull
    public static ItemStackBuilder builder(@NotNull ItemLike itemLike, int amount, @Nullable TagCompound tag) {
        return builder(itemLike.asItem(), amount).tag(tag);
    }

    @Contract(pure = true)
    @NotNull
    public static ItemStackBuilder builder(@NotNull ItemLike itemLike, int amount) {
        return new ItemStackBuilder(itemLike.asItem(), amount);
    }

    @Contract(pure = true)
    @NotNull
    public static ItemStackBuilder builder(@NotNull ItemLike itemLike) {
        return builder(itemLike.asItem(), 1);
    }

    @Contract(pure = true)
    @NotNull
    public TagCompound getOrCreateTag() {
        return this.tag != null ? this.tag : (this.tag = new CompoundTag());
    }

    @Contract(pure = true)
    @NotNull
    public ItemStackBuilder tag(@Nullable TagCompound tag) {
        this.tag = tag;
        return this;
    }

    @Contract(pure = true)
    @NotNull
    public ItemStackBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    @Contract(pure = true)
    @NotNull
    public ItemStackBuilder count(int amount) {
        return amount(amount);
    }

    @Contract(pure = true)
    @NotNull
    public ItemStackBuilder grow(int amount) {
        return count(this.amount + amount);
    }

    @Contract(pure = true)
    @NotNull
    public ItemStackBuilder shrink(int amount) {
        return grow(-amount);
    }

    /**
     * Sets the Damage Value (also known as <i>DV</i>) of an item. 0 is the default value.
     * @param damageValue the new damage value
     */
    @Contract(pure = true)
    @NotNull
    public ItemStackBuilder damageValue(int damageValue) {
        this.damageValue = damageValue;
        return this;
    }

    /**
     * Sets the name that is shown when a player hovers the item. also known as display name.
     * @param hoverName the hover name
     */
    @Contract(pure = true)
    @NotNull
    public ItemStackBuilder hoverName(@Nullable Component hoverName) {
        this.hoverName = hoverName;
        return this;
    }

    /**
     * Enchants an item.
     * @param enchantment the enchantment to add
     * @param level the enchantment level
     */
    @Contract(pure = true)
    @NotNull
    public ItemStackBuilder enchant(@NotNull Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    /**
     * Removes enchant from an item.
     * @param enchantment the enchantment to remove
     */
    @Contract(pure = true)
    @NotNull
    public ItemStackBuilder removeEnchant(@NotNull Enchantment enchantment) {
        this.enchantments.remove(enchantment);
        return this;
    }

    /**
     * Removes enchant only if the level matches.
     * @param enchantment the enchantment to remove
     * @param level the enchantment level requires to match
     */
    @Contract(pure = true)
    @NotNull
    public ItemStackBuilder removeEnchant(@NotNull Enchantment enchantment, int level) {
        this.enchantments.remove(enchantment, level);
        return this;
    }

    @Contract(pure = true)
    @NotNull
    public ItemStackBuilder repairCost(int repairCost) {
        this.repairCost = repairCost;
        return this;
    }

    @Contract(pure = true)
    @NotNull
    public ItemStack build() {
        ItemStack stack = new ItemStack(item, amount);
        if (damageValue != 0) stack.setDamageValue(damageValue);
        if (tag != null) stack.setTag(tag);
        if (hoverName != null) stack.setHoverName(hoverName);
        if (!enchantments.isEmpty()) enchantments.forEach(stack::enchant);
        if (repairCost != 0) stack.setRepairCost(repairCost);
        return stack;
    }
}
