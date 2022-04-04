package net.blueberrymc.common.permission;

public enum PermissionState {
    UNDEFINED(false),
    FALSE(false),
    TRUE(true),
    ;

    private final boolean value;

    PermissionState(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }
}
