package net.blueberrymc.world.item;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.blueberrymc.registry.Registry;
import net.blueberrymc.util.Constants;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class Item implements ItemLike {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ItemRarity rarity;

    public Item(@NotNull Properties properties) {
        if (Constants.IS_RUNNING_IN_IDE) {
            String s = this.getClass().getSimpleName();
            if (!s.endsWith("Item")) {
                LOGGER.error("Item classes should end with Item and {} doesn't.", s);
            }
        }
        this.rarity = properties.getRarity();
    }

    @Override
    public @NotNull Item asItem() {
        return this;
    }

    @NotNull
    public abstract Component getName(@NotNull ItemStack itemStack);

    @NotNull
    public ItemRarity getRawRarity() {
        return rarity;
    }

    /**
     * Create a new item with the given minecraft item instance.
     * @param o the instance
     * @return the item
     */
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static Item ofUnsafe(@NotNull Object o) {
        Objects.requireNonNull(o, "o");
        return (Item) ImplGetter.byConstructor(Object.class).apply(o);
    }

    /**
     * Gets an item by its id, key, or resource location (like <code>minecraft:stone</code>).
     * @param key item location
     * @return the item
     * @throws IllegalArgumentException if the item is not found
     */
    @NotNull
    public static Item byKey(@NotNull Key key) {
        return Registry.ITEM.getValueOrThrow(key);
    }

    /**
     * Gets an item by its id, key, or resource location (like <code>minecraft:stone</code>).
     * @param key item location
     * @return the item
     * @throws IllegalArgumentException if the item is not found
     */
    @NotNull
    public static Item byKey(@Subst("minecraft:stone") @NotNull String key) {
        return byKey(Key.key(key));
    }

    public interface Properties {
        @Contract(" -> new")
        @NotNull
        static Builder builder() {
            return (Builder) ImplGetter.byMethod("builder").apply();
        }

        int getMaxStackSize();

        int getMaxDamage();

        @Nullable
        Item getCraftRemainder();

        @NotNull
        ItemRarity getRarity();

        boolean isFireResistant();

        interface Builder {
            /**
             * Sets the maximum stack size of the item.
             * @param count maximum stack size
             * @return this builder
             */
            @Contract(value = "_ -> this", mutates = "this")
            @NotNull
            Builder stacksTo(int count);

            /**
             * Sets the default durability of the item. Actually, this sets the durability if the durability is not set
             * before.
             * @param durability the durability of the item
             * @return this builder
             */
            @Contract(value = "_ -> this", mutates = "this")
            @NotNull
            Builder defaultDurability(int durability);

            /**
             * Sets the durability and sets the maximum stack size to 1 (un-stackable).
             * @param durability the durability of the item
             * @return this builder
             */
            @Contract(value = "_ -> this", mutates = "this")
            @NotNull
            Builder durability(int durability);

            /**
             * Sets the item which remains when a player crafts an item using this item. For example, when crafting the
             * cake with milk bucket (craftRemainder is set to bucket), the player keeps the bucket after crafting the
             * cake.
             * @param item the item
             * @return this builder
             */
            @Contract(value = "_ -> fail", mutates = "this") // TODO: change to "_ -> this" after this has been implemented
            @NotNull
            Builder craftRemainder(@Nullable Item item);

            /**
             * Sets the rarity of an item. The default rarity is {@link net.blueberrymc.world.item.ItemRarity#COMMON}.
             * @param rarity the rarity of the item
             * @return this builder
             */
            @Contract(value = "_ -> this", mutates = "this")
            @NotNull
            Builder rarity(@NotNull ItemRarity rarity);

            /**
             * Sets whether the item (as an entity form) survives the fire.
             * @param fireResistant true if the item survives the fire, false otherwise
             * @return this builder
             */
            @Contract(value = "_ -> this", mutates = "this")
            @NotNull
            default Builder fireResistant(boolean fireResistant) {
                if (fireResistant) {
                    return this.fireResistant();
                } else {
                    return this;
                }
            }

            /**
             * Sets the item (as an entity form) to survives the fire.
             * @return this builder
             */
            @Contract(value = "-> this", mutates = "this")
            @NotNull
            Builder fireResistant();

            @Contract(value = "-> new", pure = true)
            @NotNull
            Properties build();
        }
    }
}
