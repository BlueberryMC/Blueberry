package net.blueberrymc.impl.world.item;

import net.blueberrymc.util.Reflected;
import net.blueberrymc.world.item.ItemRarity;
import net.blueberrymc.world.item.ItemStack;
import net.kyori.adventure.text.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlueberryItem extends net.blueberrymc.world.item.Item {
    public BlueberryItem(net.blueberrymc.world.item.Item.@NotNull Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        return null;
    }

    public record Properties(@NotNull Item.Properties handle) implements net.blueberrymc.world.item.Item.Properties {
        @Reflected
        @Contract(" -> new")
        @NotNull
        static BuilderImpl builder() {
            return new BuilderImpl();
        }

        public static class BuilderImpl implements net.blueberrymc.world.item.Item.Properties.Builder {
            private final Item.Properties handle = new Item.Properties();

            @Override
            public @NotNull Builder stacksTo(int count) {
                handle.stacksTo(count);
                return this;
            }

            @Override
            public @NotNull Builder defaultDurability(int durability) {
                handle.defaultDurability(durability);
                return this;
            }

            @Override
            public @NotNull Builder durability(int durability) {
                handle.durability(durability);
                return this;
            }

            @Override
            public @NotNull Builder craftRemainder(net.blueberrymc.world.item.@Nullable Item item) {
                throw new UnsupportedOperationException("Not implemented yet.");
            }

            @Override
            public @NotNull Builder rarity(@NotNull ItemRarity rarity) {
                handle.rarity(Rarity.valueOf(rarity.name()));
                return this;
            }

            @Override
            public @NotNull Builder fireResistant() {
                handle.fireResistant();
                return this;
            }

            @NotNull
            public net.blueberrymc.world.item.Item.Properties build() {
                return new Properties(handle);
            }
        }
    }
}
