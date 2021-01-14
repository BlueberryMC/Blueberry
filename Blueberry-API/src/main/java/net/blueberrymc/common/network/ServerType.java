package net.blueberrymc.common.network;

import net.blueberrymc.client.resources.BlueberryCommonComponents;
import net.blueberrymc.client.resources.BlueberryText;

public enum ServerType {
    BLUEBERRY_GOOD(0, BlueberryCommonComponents.MULTIPLAYER_COMPATIBLE),
    BLUEBERRY_BAD(1, BlueberryCommonComponents.MULTIPLAYER_INCOMPATIBLE),
    VANILLA(3, BlueberryCommonComponents.MULTIPLAYER_VANILLA),
    ;

    private final int offset;
    private final BlueberryText blueberryText;

    ServerType(int offset, BlueberryText blueberryText) {
        this.offset = offset;
        this.blueberryText = blueberryText;
    }

    public int getOffset() {
        return offset;
    }

    public BlueberryText getBlueberryText() {
        return blueberryText;
    }
}
