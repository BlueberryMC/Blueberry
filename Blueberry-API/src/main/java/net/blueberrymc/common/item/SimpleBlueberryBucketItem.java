package net.blueberrymc.common.item;

import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class SimpleBlueberryBucketItem extends BlueberryBucketItem {
    private final Function<ItemStack, Component> toNameFunction;

    public SimpleBlueberryBucketItem(@NotNull("mod") BlueberryMod mod, @NotNull("fluid") Fluid fluid, Properties properties, @NotNull("toNameFunction") Function<ItemStack, Component> toNameFunction) {
        super(mod, fluid, properties);
        this.toNameFunction = toNameFunction;
    }

    @Override
    public Component getName(ItemStack itemStack) {
        return toNameFunction.apply(itemStack);
    }
}
