package net.blueberrymc.world.item;

import net.blueberrymc.world.level.fluid.Fluid;

public abstract class BucketItem extends Item {
    private final Fluid fluid;

    public BucketItem(Fluid fluid, Properties properties) {
        super(properties);
        this.fluid = fluid;
    }
}
