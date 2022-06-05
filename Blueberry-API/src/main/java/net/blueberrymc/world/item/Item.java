package net.blueberrymc.world.item;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.blueberrymc.util.Constants;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Item {
    private static final Logger LOGGER = LogManager.getLogger();

    public Item(@NotNull Properties properties) {
        if (Constants.IS_RUNNING_IN_IDE) {
            String s = this.getClass().getSimpleName();
            if (!s.endsWith("Item")) {
                LOGGER.error("Item classes should end with Item and {} doesn't.", s);
            }
        }
    }

    @NotNull
    public abstract Component getName(@NotNull ItemStack itemStack);

    public interface Properties {
        @Contract(" -> new")
        @NotNull
        static Builder builder() {
            return (Builder) ImplGetter.byMethod("builder").apply();
        }

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
