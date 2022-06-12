package net.blueberrymc.impl.world.item;

import net.blueberrymc.common.util.ReflectionHelper;
import net.blueberrymc.impl.common.text.MinecraftComponent;
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
    @NotNull
    private final Item handle;

    public BlueberryItem(@NotNull Item handle) {
        super(Properties.builder()
                .durability(handle.getMaxDamage())
                .stacksTo(handle.getMaxStackSize())
                .rarity(ItemRarity.valueOf(handle.getRarity(net.minecraft.world.item.ItemStack.EMPTY).name()))
                .fireResistant(handle.isFireResistant())
                .build());
        this.handle = handle;
    }

    public BlueberryItem(net.blueberrymc.world.item.Item.@NotNull Properties properties) {
        super(properties);
        Item.Properties props = new Item.Properties();
        props.durability(properties.getMaxDamage());
        props.stacksTo(properties.getMaxStackSize());
        props.rarity(Rarity.valueOf(properties.getRarity().name()));
        if (properties.isFireResistant()) {
            props.fireResistant();
        }
        this.handle = new Item(props);
    }

    @NotNull
    public Item getHandle() {
        return handle;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        return MinecraftComponent.toAdventure(handle.getName(BlueberryItemStack.toMinecraft(itemStack)));
    }

    public record Properties(@NotNull Item.Properties handle) implements net.blueberrymc.world.item.Item.Properties {
        @Reflected
        @Contract(" -> new")
        @NotNull
        static BuilderImpl builder() {
            return new BuilderImpl();
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public int getMaxStackSize() {
            return (int) ReflectionHelper.getFieldWithoutException(Item.Properties.class, handle, "maxStackSize");
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public int getMaxDamage() {
            return (int) ReflectionHelper.getFieldWithoutException(Item.Properties.class, handle, "maxDamage");
        }

        @Override
        public net.blueberrymc.world.item.@Nullable Item getCraftRemainder() {
            var craftingRemainingItem = (Item) ReflectionHelper.getFieldWithoutException(Item.Properties.class, handle, "craftingRemainingItem");
            if (craftingRemainingItem == null) {
                return null;
            }
            return new BlueberryItem(craftingRemainingItem);
        }

        @Override
        public @NotNull ItemRarity getRarity() {
            var rarity = (Rarity) ReflectionHelper.getFieldWithoutException(Item.Properties.class, handle, "rarity");
            if (rarity == null) {
                return ItemRarity.COMMON;
            }
            return ItemRarity.valueOf(rarity.name());
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public boolean isFireResistant() {
            return (boolean) ReflectionHelper.getFieldWithoutException(Item.Properties.class, handle, "fireResistant");
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
