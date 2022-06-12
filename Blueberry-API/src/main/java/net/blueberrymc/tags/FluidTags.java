package net.blueberrymc.tags;

import net.blueberrymc.registry.Registry;
import net.blueberrymc.world.level.fluid.Fluid;
import net.kyori.adventure.key.Key;

public class FluidTags {
    public static final TagKey<Fluid> WATER = TagKey.create(Registry.FLUID_REGISTRY, Key.key("minecraft:water"));
    public static final TagKey<Fluid> LAVA = TagKey.create(Registry.FLUID_REGISTRY, Key.key("minecraft:lava"));
}
