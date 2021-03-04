package net.blueberrymc.common.item;

import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class SimpleBlueberryBucketItem extends BlueberryBucketItem {
    private final Function<ItemStack, Component> toNameFunction;

    public SimpleBlueberryBucketItem(@NotNull("mod") BlueberryMod mod, @NotNull("fluid") Fluid fluid, @NotNull Properties properties, @NotNull("toNameFunction") Function<ItemStack, Component> toNameFunction) {
        super(mod, fluid, properties);
        this.toNameFunction = toNameFunction;
    }

    @Nullable
    @Override
    public Component getName(@NotNull ItemStack itemStack) {
        return toNameFunction.apply(itemStack);
    }
}
