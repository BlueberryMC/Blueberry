package net.blueberrymc.common.event.block;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public abstract class BlockEvent extends Event {
    protected final Block block;

    public BlockEvent(@NotNull Block block) {
        this(block, !Blueberry.getUtil().isOnGameThread());
    }

    protected BlockEvent(@NotNull Block block, boolean async) {
        super(async);
        this.block = block;
    }

    @NotNull
    public Block getBlock() {
        return block;
    }
}
