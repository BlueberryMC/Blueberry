package net.blueberrymc.common.resources;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class BlueberryCommonComponents {
    public static final BlueberryText MULTIPLAYER_COMPATIBLE = new BlueberryText("blueberry", "multiplayer.compatible");
    public static final BlueberryText MULTIPLAYER_INCOMPATIBLE = new BlueberryText("blueberry", "multiplayer.incompatible");
    public static final BlueberryText MULTIPLAYER_VANILLA = new BlueberryText("blueberry", "multiplayer.vanilla");
    public static final MutableComponent EMPTY_TEXT = Component.literal("");
}
