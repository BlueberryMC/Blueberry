package net.blueberrymc.world.level.block.state;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class BlockBehaviour {
    protected BlockBehaviour(@NotNull Properties properties) {
    }

    public interface Properties {
        @Contract(pure = true)
        @NotNull
        static Builder builder() {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        interface Builder {
            @NotNull
            Properties build();
        }
    }
}
