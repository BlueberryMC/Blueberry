package net.blueberrymc.common.bml.event;

public enum EventPriority {
    /**
     * Event call is of very low importance and should be run first, to allow other plugins to further customise the outcome.
     */
    LOWEST(-3),    // ^ First
    VERY_LOW(-2),  // |
    LOW(-1),       // |
    NORMAL(0),     // |
    HIGH(1),       // |
    VERY_HIGH(2),  // |
    HIGHEST(3),    // |
    /** The event executor should be run last, No modifications to the event should be made under this priority. */
    MONITOR(4),    // v Last
    ;

    private final int slot;

    EventPriority(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }
}
