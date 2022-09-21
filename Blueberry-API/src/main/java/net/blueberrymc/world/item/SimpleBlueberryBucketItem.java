package net.blueberrymc.world.item;

import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.world.level.fluid.Fluid;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class SimpleBlueberryBucketItem extends BlueberryBucketItem {
    private final Function<ItemStack, Component> toNameFunction;

    public SimpleBlueberryBucketItem(@NotNull BlueberryMod mod, @NotNull Fluid fluid, @NotNull Properties properties, @NotNull Function<ItemStack, Component> toNameFunction) {
        super(mod, fluid, properties);
        this.toNameFunction = toNameFunction;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        return toNameFunction.apply(itemStack);
    }
}
