package net.blueberrymc.impl.util;

import net.kyori.adventure.key.Key;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class KeyUtil {
    @SuppressWarnings("PatternValidation")
    @NotNull
    public static Key toAdventure(@NotNull ResourceLocation resourceLocation) {
        if ((Object) resourceLocation instanceof Key key) {
            return key;
        }
        return Key.key(resourceLocation.getNamespace(), resourceLocation.getPath());
    }

    @NotNull
    public static ResourceLocation toMinecraft(@NotNull Key key) {
        if ((Object) key instanceof ResourceLocation resourceLocation) {
            return resourceLocation;
        }
        return new ResourceLocation(key.namespace(), key.value());
    }
}
