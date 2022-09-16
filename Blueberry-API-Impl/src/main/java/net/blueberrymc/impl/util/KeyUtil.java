package net.blueberrymc.impl.util;

import net.kyori.adventure.key.Key;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;

public class KeyUtil {
    @SuppressWarnings("PatternValidation")
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static Key toAdventure(ResourceLocation resourceLocation) {
        if (resourceLocation == null) {
            return null;
        }
        if ((Object) resourceLocation instanceof Key key) {
            return key;
        }
        return Key.key(resourceLocation.getNamespace(), resourceLocation.getPath());
    }

    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static ResourceLocation toMinecraft(Key key) {
        if (key == null) {
            return null;
        }
        if ((Object) key instanceof ResourceLocation resourceLocation) {
            return resourceLocation;
        }
        return new ResourceLocation(key.namespace(), key.value());
    }
}
