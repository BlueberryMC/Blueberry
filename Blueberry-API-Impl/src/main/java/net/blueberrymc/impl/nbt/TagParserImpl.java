package net.blueberrymc.impl.nbt;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.blueberrymc.nbt.TagCompound;
import net.blueberrymc.nbt.TagParser;
import net.blueberrymc.util.Reflected;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TagParserImpl {
    @Contract(pure = true)
    @Reflected
    public static @NotNull TagCompound parse(String nbt) throws TagParser.TagParserException {
        try {
            return new TagCompoundImpl(net.minecraft.nbt.TagParser.parseTag(nbt));
        } catch (CommandSyntaxException e) {
            throw new TagParser.TagParserException(e);
        }
    }
}
