package net.blueberrymc.world.item;

import net.blueberrymc.common.bml.BlueberryMod;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class SimpleBlueberryItem extends BlueberryItem {
    private final Function<ItemStack, Component> toNameFunction;

    public SimpleBlueberryItem(@NotNull BlueberryMod mod, @NotNull Properties properties, @NotNull Function<@NotNull ItemStack, @NotNull Component> toNameFunction) {
        super(mod, properties);
        this.toNameFunction = toNameFunction;
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack itemStack) {
        return toNameFunction.apply(itemStack);
    }
}
