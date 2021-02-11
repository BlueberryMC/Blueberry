package net.minecraftforge.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICapabilityProvider {
    @NotNull
    <T> LazyOptional<T> getCapability(@NotNull final Capability<T> cap, final @Nullable Direction side);

    default <T> LazyOptional<T> getCapability(@NotNull final Capability<T> cap) {
        return getCapability(cap, null);
    }
}
