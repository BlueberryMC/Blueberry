package net.blueberrymc.common.bml;

import net.blueberrymc.util.Versioned;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface VersionedModInfo extends Versioned, ModInfo {
    @Contract("_ -> new")
    @NotNull
    static VersionedModInfo copyValues(@NotNull VersionedModInfo info) {
        return new SimpleVersionedModInfo(info.getName(), info.getModId(), info.getVersion());
    }

    @NotNull
    default CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", getName());
        tag.putString("id", getModId());
        tag.putString("version", getVersion());
        return tag;
    }
}
