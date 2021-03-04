package net.blueberrymc.client.commands;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ClientCommandManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ConcurrentHashMap<String, ClientCommandHandler> COMMANDS = new ConcurrentHashMap<>();
    private static final CommandDispatcher<CommandSourceStack> DISPATCHER = new CommandDispatcher<>();

    @NotNull
    public static String strip(@NotNull String s) {
        s = s.split(" ")[0];
        return s.substring(1);
    }

    public static boolean hasCommand(@NotNull String s) { return has(strip(s)); }

    public static boolean has(@NotNull String name) {
        return COMMANDS.containsKey(name);
    }

    @Nullable
    public static ClientCommandHandler get(@NotNull String name) {
        return COMMANDS.get(name);
    }

    public static void register(@NotNull String name, @NotNull ClientCommandHandler handler) {
        if (has(name)) {
            ClientCommandHandler theirs = Objects.requireNonNull(get(name));
            LOGGER.warn("Client command conflict: {} (yours) and {} (theirs)", handler.getMod().getName(), theirs.getMod().getName());
            LOGGER.warn("Replacing {}'s client command with {}'s ClientCommandHandler.", theirs.getMod().getName(), handler.getMod().getName());
        }
        COMMANDS.put(name, handler);
        handler.register(DISPATCHER);
    }

    @NotNull
    public static CommandDispatcher<CommandSourceStack> getDispatcher() {
        return DISPATCHER;
    }

    @NotNull
    public static CommandDispatcher<SharedSuggestionProvider> getRoot(@NotNull Player player) {
        Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> map = Maps.newHashMap();
        RootCommandNode<SharedSuggestionProvider> rootCommandNode = new RootCommandNode<>();
        map.put(getDispatcher().getRoot(), rootCommandNode);
        fillUsableCommands(getDispatcher().getRoot(), rootCommandNode, player.createCommandSourceStack(), map);
        return new CommandDispatcher<>(rootCommandNode);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void fillUsableCommands(@NotNull CommandNode<CommandSourceStack> commandNode, @NotNull CommandNode<SharedSuggestionProvider> commandNode2, CommandSourceStack commandSourceStack, @NotNull Map<CommandNode<CommandSourceStack>, @NotNull CommandNode<SharedSuggestionProvider>> map) {
        for(CommandNode<CommandSourceStack> commandNode3 : commandNode.getChildren()) {
            if (commandNode3.canUse(commandSourceStack)) {
                ArgumentBuilder<SharedSuggestionProvider, ?> argumentBuilder = (ArgumentBuilder) commandNode3.createBuilder();
                argumentBuilder.requires((sharedSuggestionProvider) -> true);
                if (argumentBuilder.getCommand() != null) {
                    argumentBuilder.executes((commandContext) -> 0);
                }
                if (argumentBuilder instanceof RequiredArgumentBuilder) {
                    RequiredArgumentBuilder<SharedSuggestionProvider, ?> requiredArgumentBuilder = (RequiredArgumentBuilder<SharedSuggestionProvider, ?>) argumentBuilder;
                    if (requiredArgumentBuilder.getSuggestionsProvider() != null) {
                        requiredArgumentBuilder.suggests(SuggestionProviders.safelySwap(requiredArgumentBuilder.getSuggestionsProvider()));
                    }
                }
                if (argumentBuilder.getRedirect() != null) {
                    argumentBuilder.redirect(map.get(argumentBuilder.getRedirect()));
                }
                CommandNode<SharedSuggestionProvider> commandNode4 = argumentBuilder.build();
                map.put(commandNode3, commandNode4);
                commandNode2.addChild(commandNode4);
                if (!commandNode3.getChildren().isEmpty()) {
                    fillUsableCommands(commandNode3, commandNode4, commandSourceStack, map);
                }
            }
        }

    }

    public static int performCommand(@NotNull CommandSourceStack commandSourceStack, @NotNull String s) {
        StringReader stringReader = new StringReader(s);
        if (stringReader.canRead() && stringReader.peek() == '/') {
            stringReader.skip();
        }

        commandSourceStack.getServer().getProfiler().push(s);

        try {
            try {
                return getDispatcher().execute(stringReader, commandSourceStack);
            } catch (CommandRuntimeException var13) {
                commandSourceStack.sendFailure(var13.getComponent());
                return 0;
            } catch (CommandSyntaxException var14) {
                commandSourceStack.sendFailure(ComponentUtils.fromMessage(var14.getRawMessage()));
                if (var14.getInput() != null && var14.getCursor() >= 0) {
                    int i = Math.min(var14.getInput().length(), var14.getCursor());
                    MutableComponent mutableComponent = (new TextComponent("")).withStyle(ChatFormatting.GRAY).withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, s)));
                    if (i > 10) {
                        mutableComponent.append("...");
                    }

                    mutableComponent.append(var14.getInput().substring(Math.max(0, i - 10), i));
                    if (i < var14.getInput().length()) {
                        Component component = (new TextComponent(var14.getInput().substring(i))).withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
                        mutableComponent.append(component);
                    }

                    mutableComponent.append((new TranslatableComponent("command.context.here")).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                    commandSourceStack.sendFailure(mutableComponent);
                }
            } catch (Exception var15) {
                MutableComponent mutableComponent2 = new TextComponent(var15.getMessage() == null ? var15.getClass().getName() : var15.getMessage());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Command exception: {}", s, var15);
                    StackTraceElement[] stackTraceElements = var15.getStackTrace();

                    for(int i2 = 0; i2 < Math.min(stackTraceElements.length, 3); ++i2) {
                        mutableComponent2.append("\n\n").append(stackTraceElements[i2].getMethodName()).append("\n ").append(stackTraceElements[i2].getFileName()).append(":").append(String.valueOf(stackTraceElements[i2].getLineNumber()));
                    }
                }

                commandSourceStack.sendFailure((new TranslatableComponent("command.failed")).withStyle((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mutableComponent2))));
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    commandSourceStack.sendFailure(new TextComponent(Util.describeError(var15)));
                    LOGGER.error("'" + s + "' threw an exception", var15);
                }

                return 0;
            }

            return 0;
        } finally {
            commandSourceStack.getServer().getProfiler().pop();
        }
    }
}
