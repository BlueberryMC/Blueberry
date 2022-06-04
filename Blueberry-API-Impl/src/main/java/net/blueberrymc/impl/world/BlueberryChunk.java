package net.blueberrymc.impl.world;

import net.blueberrymc.world.Chunk;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;

public record BlueberryChunk(@NotNull LevelChunk handle) implements Chunk {
}
