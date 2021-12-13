package net.blueberrymc.network;

import net.blueberrymc.common.resources.BlueberryCommonComponents;
import net.blueberrymc.common.resources.BlueberryText;
import org.jetbrains.annotations.NotNull;

public enum ServerType {
    BLUEBERRY_GOOD(0, BlueberryCommonComponents.MULTIPLAYER_COMPATIBLE),
    BLUEBERRY_BAD(1, BlueberryCommonComponents.MULTIPLAYER_INCOMPATIBLE),
    VANILLA(3, BlueberryCommonComponents.MULTIPLAYER_VANILLA),
    ;

    private final int offset;
    private final BlueberryText blueberryText;

    ServerType(int offset, @NotNull BlueberryText blueberryText) {
        this.offset = offset;
        this.blueberryText = blueberryText;
    }

    public int getOffset() {
        return offset;
    }

    @NotNull
    public BlueberryText getBlueberryText() {
        return blueberryText;
    }
}
