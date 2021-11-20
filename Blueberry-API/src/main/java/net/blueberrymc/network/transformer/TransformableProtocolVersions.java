package net.blueberrymc.network.transformer;

import net.blueberrymc.util.NameGetter;
import org.jetbrains.annotations.NotNull;

public enum TransformableProtocolVersions implements NameGetter {
    v1_18_PRE5("1.18 pre5", 0x40000035),
    v1_18_PRE4("1.18 pre4", 0x40000034),
    v1_18_PRE3("1.18 pre3", 0x40000033),
    v1_18_PRE2("1.18 pre2", 0x40000032),
    v1_18_PRE1("1.18 pre1", 0x40000031),
    SNAPSHOT_21W44A("21w44a", 0x40000030),
    SNAPSHOT_21W43A("21w43a", 0x4000002F),
    SNAPSHOT_21W42A("21w42a", 0x4000002E),
    SNAPSHOT_21W41A("21w41a", 0x4000002D),
    SNAPSHOT_21W40A("21w40a", 0x4000002C),
    SNAPSHOT_21W39A("21w39a", 0x4000002B),
    SNAPSHOT_21W38A("21w38a", 0x4000002A),
    SNAPSHOT_21W37A("21w37a", 0x40000029),
    v1_17_1("1.17.1", 756),
    v1_17("1.17", 755),
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
