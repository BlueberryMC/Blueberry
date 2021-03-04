package net.blueberrymc.common.event.block;

import net.blueberrymc.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public abstract class BlockExpEvent extends BlockEvent {
    protected int exp;

    public BlockExpEvent(@NotNull Block block, int exp) {
        super(block);
        this.exp = exp;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }
}
