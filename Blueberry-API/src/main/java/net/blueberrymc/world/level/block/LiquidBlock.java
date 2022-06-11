package net.blueberrymc.world.level.block;

import net.blueberrymc.world.level.block.state.BlockState;

public class LiquidBlock {
    public static final BlockState.Property<Integer> LEVEL = BlockState.Property.create("level", Integer.class);
}
