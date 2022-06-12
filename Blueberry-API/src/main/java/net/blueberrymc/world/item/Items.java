package net.blueberrymc.world.item;

import net.blueberrymc.registry.Registry;
import net.kyori.adventure.key.Key;

public class Items {
    public static final Item AIR = Registry.ITEM.getValueOrThrow(Key.key("minecraft:air"));
    public static final Item STONE = Registry.ITEM.getValueOrThrow(Key.key("minecraft:stone"));
}
