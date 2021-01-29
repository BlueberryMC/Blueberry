package net.blueberrymc.server.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModIdArgument implements ArgumentType<String> {
    private static final List<String> EXAMPLES = Arrays.asList("magmacube", "blueberry");

    @Contract(value = " -> new", pure = true)
    @NotNull
    public static ModIdArgument modIdArgument() {
        return new ModIdArgument();
    }

    @Override
    public String parse(StringReader stringReader) {
        return stringReader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Blueberry.getModLoader().mapActiveMods(BlueberryMod::getModId), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
