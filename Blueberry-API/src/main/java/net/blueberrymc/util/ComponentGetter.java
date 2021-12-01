package net.blueberrymc.util;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ComponentGetter {
    @NotNull
    Component getComponent();
}
