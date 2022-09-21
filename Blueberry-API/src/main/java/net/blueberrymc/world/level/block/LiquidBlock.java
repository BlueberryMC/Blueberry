package net.blueberrymc.world.level.block;

import net.blueberrymc.world.level.block.state.properties.BlockStateProperties;
import net.blueberrymc.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

public abstract class LiquidBlock extends BlockData {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;

    protected LiquidBlock(@NotNull Properties properties) {
        super(properties);
    }
}
