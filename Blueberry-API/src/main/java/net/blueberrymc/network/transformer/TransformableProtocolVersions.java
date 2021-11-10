package net.blueberrymc.network.transformer;

import net.blueberrymc.util.NameGetter;
import org.jetbrains.annotations.NotNull;

public enum TransformableProtocolVersions implements NameGetter {
    SNAPSHOT_21W44A("21w44a", 0x40000030),
    SNAPSHOT_21w43A("21w43a", 0x4000002F),
    ;

    private final String name;
    private final int protocolVersion;

    TransformableProtocolVersions(@NotNull String name, int protocolVersion) {
        this.name = name;
        this.protocolVersion = protocolVersion;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    @NotNull
    @Override
    public String toString() {
        return name + "(" + protocolVersion + ")";
    }
}
