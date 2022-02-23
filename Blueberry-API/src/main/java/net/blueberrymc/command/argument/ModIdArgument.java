package net.blueberrymc.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.blueberrymc.common.resources.BlueberryText;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record ModIdArgument(@NotNull Mode mode) implements ArgumentType<BlueberryMod> {
    private static final List<String> EXAMPLES = Collections.singletonList("blueberry");
    // private static final BlueberryText INVALID_MOD_ID_MESSAGE = new BlueberryText("blueberry", "command.argument.mod_id.invalid_mod");
    private static final DynamicCommandExceptionType INVALID_MOD_ID = new DynamicCommandExceptionType(o -> new BlueberryText("blueberry", "command.argument.mod_id.invalid_mod", o));

    @Contract(value = "-> new", pure = true)
    @NotNull
    public static ModIdArgument modId() {
        return modId(Mode.ALL_MODS);
    }

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static ModIdArgument modId(@NotNull Mode mode) {
        return new ModIdArgument(mode);
    }

    @NotNull
    public static <S> BlueberryMod get(@NotNull CommandContext<S> commandContext, @NotNull String s) {
        return commandContext.getArgument(s, BlueberryMod.class);
    }

    /**
     * @deprecated Use {@link #mode()} instead.
     */
    @Deprecated
    @NotNull
    public Mode getMode() {
        return mode;
    }

    @NotNull
    @Override
    public BlueberryMod parse(@NotNull StringReader stringReader) throws CommandSyntaxException {
        String modId = stringReader.readUnquotedString();
        BlueberryMod mod = Blueberry.getModLoader().getModById(modId);
        if (mod == null) throw INVALID_MOD_ID.createWithContext(stringReader, modId);
        return mod;
    }

    @NotNull
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        List<String> list = switch (mode) {
            case ONLY_ACTIVE_MODS -> Blueberry.getModLoader().mapActiveMods(BlueberryMod::getModId);
            case ALL_MODS -> Blueberry.getModLoader().mapLoadedMods(BlueberryMod::getModId);
        };
        return SharedSuggestionProvider.suggest(list, builder);
    }

    @NotNull
    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public enum Mode {
        ONLY_ACTIVE_MODS,
        ALL_MODS,
    }
}
