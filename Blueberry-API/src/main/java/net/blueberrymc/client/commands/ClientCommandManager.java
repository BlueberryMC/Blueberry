package net.blueberrymc.client.commands;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A class responsible for registering client-side commands.
 */
public class ClientCommandManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Object2ObjectMap<String, ClientCommandHandler> COMMANDS = new Object2ObjectOpenHashMap<>();
    private static final CommandDispatcher<CommandSourceStack> DISPATCHER = new CommandDispatcher<>();

    /**
     * Strips the leading slash if present.
     * @param s string which might contain leading slash
     * @return stripped string
     */
    @NotNull
    public static String strip(@NotNull String s) {
        s = s.split(" ")[0];
        return s.startsWith("/") ? s.substring(1) : s;
    }

    /**
     * Checks if a client command is registered. Similar to {@link #has(String)}, but this method can have a leading
     * slash in parameter <code>s</code>. If leading slash is present, this method will automatically strip one slash.
     * @param s the command name with or without leading slash (/)
     * @return true if registered, false otherwise
     * @see #has(String)
     */
    public static boolean hasCommand(@NotNull String s) {
        return has(strip(s));
    }

    /**
     * Checks if a client command is registered.
     * @param name the command name without leading slash (/)
     * @return true if registered, false otherwise
     * @see #hasCommand(String)
     */
    public static boolean has(@NotNull String name) {
        return COMMANDS.containsKey(name);
    }

    /**
     * Finds the client command handler by name.
     * @param name the command name without leading slash (/)
     * @return registered command handler, null if not registered
     */
    @Nullable
    public static ClientCommandHandler get(@NotNull String name) {
        return COMMANDS.get(name);
    }

    /**
     * Unregisters all client commands.
     * @param mod the mod
     */
    public static void unregisterAll(@NotNull BlueberryMod mod) {
        // TODO: remove command from dispatcher?
        List<String> toRemove = new ArrayList<>();
        COMMANDS.forEach((name, handler) -> {
            if (ClientCommandHandler.getMod(handler) == mod) {
                toRemove.add(name);
            }
        });
        for (String s : toRemove) {
            COMMANDS.remove(s);
        }
    }

    /**
     * Registers client command handler. Will log a message if there is a conflict. You can check whether the command
     * exists via {@link #has(String)}.
     * @param name command name without leading slash (/).
     * @param handler command handler to register command
     */
    public static void register(@NotNull String name, @NotNull ClientCommandHandler handler) {
        if (has(name)) {
            ClientCommandHandler existingHandler = Objects.requireNonNull(get(name));
            String provided = ClientCommandHandler.getMod(handler).modId();
            String existing = ClientCommandHandler.getMod(existingHandler).modId();
            LOGGER.warn("Client command conflict (/{}): {} (provided) and {} (existing)", name, provided, existing);
            LOGGER.warn("Replacing {}'s client command (/{}) with {}'s ClientCommandHandler.", existing, name, provided);
        }
        COMMANDS.put(name, handler);
        handler.register(DISPATCHER);
    }

    /**
     * Returns command dispatcher.
     * @return command dispatcher
     */
    @NotNull
    public static CommandDispatcher<CommandSourceStack> getDispatcher() {
        return DISPATCHER;
    }

    /**
     * Returns baked command dispatcher that can be merged with vanilla command dispatcher.
     * @param player local player
     * @return command dispatcher
     */
    @NotNull
    public static CommandDispatcher<SharedSuggestionProvider> getRoot(@NotNull Player player) {
        Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> map = Maps.newHashMap();
        RootCommandNode<SharedSuggestionProvider> rootCommandNode = new RootCommandNode<>();
        map.put(DISPATCHER.getRoot(), rootCommandNode);
        fillUsableCommands(DISPATCHER.getRoot(), rootCommandNode, player.createCommandSourceStack(), map);
        return new CommandDispatcher<>(rootCommandNode);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void fillUsableCommands(@NotNull CommandNode<CommandSourceStack> commandNode, @NotNull CommandNode<SharedSuggestionProvider> commandNode2, CommandSourceStack commandSourceStack, @NotNull Map<CommandNode<CommandSourceStack>, @NotNull CommandNode<SharedSuggestionProvider>> map) {
        for (CommandNode<CommandSourceStack> commandNode3 : commandNode.getChildren()) {
            if (commandNode3.canUse(commandSourceStack)) {
                ArgumentBuilder<SharedSuggestionProvider, ?> argumentBuilder = (ArgumentBuilder) commandNode3.createBuilder();
                argumentBuilder.requires((sharedSuggestionProvider) -> true); // Client commands are always available
                if (argumentBuilder.getCommand() != null) {
                    argumentBuilder.executes((commandContext) -> 0);
                }
                if (argumentBuilder instanceof RequiredArgumentBuilder requiredArgumentBuilder) {
                    requiredArgumentBuilder.suggests(requiredArgumentBuilder.getSuggestionsProvider());
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

    /**
     * Executes a command.
     * @param commandSourceStack command source
     * @param input input string which can contain leading slash
     * @return command result
     */
    public static int performCommand(@NotNull CommandSourceStack commandSourceStack, @NotNull String input) {
        StringReader reader = new StringReader(input);
        if (reader.canRead() && reader.peek() == '/') {
            reader.skip();
        }

        try {
            return DISPATCHER.execute(reader, commandSourceStack);
        } catch (CommandRuntimeException commandRuntimeException) {
            commandSourceStack.sendFailure(commandRuntimeException.getComponent());
        } catch (CommandSyntaxException commandSyntaxException) {
            commandSourceStack.sendFailure(ComponentUtils.fromMessage(commandSyntaxException.getRawMessage()));
            if (commandSyntaxException.getInput() != null && commandSyntaxException.getCursor() >= 0) {
                int i = Math.min(commandSyntaxException.getInput().length(), commandSyntaxException.getCursor());
                MutableComponent mutableComponent = Component.literal("").withStyle(ChatFormatting.GRAY).withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, input)));
                if (i > 10) {
                    mutableComponent.append("...");
                }

                mutableComponent.append(commandSyntaxException.getInput().substring(Math.max(0, i - 10), i));
                if (i < commandSyntaxException.getInput().length()) {
                    Component component = Component.literal(commandSyntaxException.getInput().substring(i)).withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
                    mutableComponent.append(component);
                }

                mutableComponent.append(Component.translatable("command.context.here").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                commandSourceStack.sendFailure(mutableComponent);
            }
        } catch (Exception ex) {
            MutableComponent exceptionComponent = Component.literal(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Command exception: {}", input, ex);
                StackTraceElement[] stackTraceElements = ex.getStackTrace();

                for (int i = 0; i < Math.min(stackTraceElements.length, 3); ++i) {
                    exceptionComponent
                            .append("\n\n")
                            .append(stackTraceElements[i].getMethodName())
                            .append("\n ")
                            .append(stackTraceElements[i].getFileName() != null ? Objects.requireNonNull(stackTraceElements[i].getFileName()) : "?")
                            .append(":")
                            .append(String.valueOf(stackTraceElements[i].getLineNumber()));
                }
            }

            commandSourceStack.sendFailure(Component.translatable("command.failed").withStyle((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, exceptionComponent))));
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                commandSourceStack.sendFailure(Component.literal(Util.describeError(ex)));
                LOGGER.error("'" + input + "' threw an exception", ex);
            }
        }

        return 0;
    }
}
