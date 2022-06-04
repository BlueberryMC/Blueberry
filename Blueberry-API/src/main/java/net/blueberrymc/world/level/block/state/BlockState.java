package net.blueberrymc.world.level.block.state;

import net.blueberrymc.world.level.block.BlockData;
import org.jetbrains.annotations.NotNull;

public interface BlockState {
    boolean isAir();

    @NotNull
    BlockData getBlockData();
}
