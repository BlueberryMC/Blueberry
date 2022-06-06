package net.blueberrymc.impl.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.util.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class BinaryTagHolderUtil {
    @NotNull public static final Codec<CompoundTag, String, CommandSyntaxException, RuntimeException> CODEC = Codec.codec(TagParser::parseTag, CompoundTag::toString);

    @Contract("_ -> new")
    public static @NotNull BinaryTagHolder toAdventure(@NotNull CompoundTag tag) {
        return BinaryTagHolder.encode(tag, CODEC);
    }

    @Contract("_ -> new")
    public static @NotNull CompoundTag toMinecraft(@NotNull BinaryTagHolder tag) {
        try {
            return tag.get(CODEC);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
