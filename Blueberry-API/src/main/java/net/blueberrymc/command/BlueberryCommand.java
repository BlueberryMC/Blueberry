package net.blueberrymc.command;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.blueberrymc.client.BlueberryClient;
import net.blueberrymc.command.argument.ModIdArgument;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.BlueberryUtil;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.event.mod.ModReloadEvent;
import net.blueberrymc.common.permission.PermissionHolder;
import net.blueberrymc.common.permission.PermissionState;
import net.blueberrymc.common.resources.BlueberryCommonComponents;
import net.blueberrymc.common.resources.BlueberryText;
import net.blueberrymc.common.util.BlueberryVersion;
import net.blueberrymc.common.util.VersionChecker;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.server.BlueberryServer;
import net.blueberrymc.util.Constants;
import net.blueberrymc.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.LongStream;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * <code>/blueberry</code> command (server-side command)
 */
public class BlueberryCommand {
    private static final SimpleCommandExceptionType UNAVAILABLE_IN_THIS_ENVIRONMENT = new SimpleCommandExceptionType(Component.literal("This command is not available in this environment.").withStyle(ChatFormatting.RED));
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("blueberry")
                        .requires(source -> ((PermissionHolder) source).hasPermission("blueberry.command"))
                        .then(literal("mod")
                                .then(argument("mod", ModIdArgument.modId())
                                        .then(literal("status").executes(context -> executeModStatusCommand(context.getSource(), ModIdArgument.get(context, "mod"))))
                                        .then(literal("reload").executes(context -> executeModReloadCommand(context.getSource(), ModIdArgument.get(context, "mod"))))
                                )
                        )
                        .then(literal("tps")
                                .executes(context -> executeTpsCommand(context.getSource()))
                        )
                        .then(literal("version")
                                .executes(context -> executeVersionCommand(context.getSource()))
                        )
                        .then(literal("permission")
                                .then(literal("check")
                                        .then(argument("node", StringArgumentType.string())
                                                .executes(context -> executePermissionCheckCommand(context.getSource(), StringArgumentType.getString(context, "node"), null))
                                                .then(argument("player", EntityArgument.player())
                                                        .executes(context -> executePermissionCheckCommand(context.getSource(), StringArgumentType.getString(context, "node"), EntityArgument.getPlayer(context, "player")))
                                                )
                                        )
                                )
                        )
        );
    }

    private static int executeModStatusCommand(CommandSourceStack source, BlueberryMod mod) {
        source.sendSuccess(Component.literal("Mod status of '" + mod.getName() + "': " + mod.getStateList() + " (Current: " + mod.getStateList().getCurrentState().getName() + ")"), false);
        return 1;
    }

    private static int executeModReloadCommand(CommandSourceStack source, BlueberryMod mod) {
        ServerPlayer player = null;
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException ignore) {}
        if (new ModReloadEvent(player, mod).callEvent()) {
            source.sendSuccess(BlueberryText.text("blueberry", "blueberry.mod.command.mod.reload.reloading", mod.getName()), true);
            try {
                if (mod.onReload()) {
                    Minecraft.getInstance().reloadResourcePacks().thenAccept(v -> source.sendSuccess(BlueberryText.text("blueberry", "blueberry.mod.command.mod.reload.success", mod.getName()), true));
                } else {
                    source.sendSuccess(BlueberryText.text("blueberry", "blueberry.mod.command.mod.reload.success", mod.getName()), true);
                }
            } catch (RuntimeException ex) {
                source.sendFailure(BlueberryText.text("blueberry", "blueberry.mod.command.mod.reload.failure.error", mod.getName(), ex.getMessage()));
                LOGGER.warn("Failed to reload mod {}", mod.getName(), ex);
            }
        } else {
            source.sendFailure(BlueberryText.text("blueberry", "blueberry.mod.command.mod.reload.failure.cancelled", mod.getName()));
        }
        return 1;
    }

    private static int executeTpsCommand(CommandSourceStack source) throws CommandSyntaxException {
        MinecraftServer server = null;
        BlueberryUtil util = Blueberry.getUtil();
        if (util instanceof BlueberryServer) {
            server = util.asServer().getServer();
        } else if (util instanceof BlueberryClient) {
            server = util.asClient().getIntegratedServer();
        }
        if (server == null) throw UNAVAILABLE_IN_THIS_ENVIRONMENT.create();
        long[] tickTimes = server.tickTimes;
        double last100t = round(getAverageTPS(tickTimes));
        double l100t = round(getLowestTPS(tickTimes));
        double last20t = round(getAverageTPS(Arrays.stream(tickTimes).limit(20)));
        double l20t = round(getLowestTPS(Arrays.stream(tickTimes).limit(20)));
        double last10t = round(getAverageTPS(Arrays.stream(tickTimes).limit(10)));
        double l10t = round(getLowestTPS(Arrays.stream(tickTimes).limit(10)));
        source.sendSuccess(
                Component.literal("Average TPS in last 100 ticks (5 seconds): ")
                        .append(Component.literal(Double.toString(last100t)).withStyle(getTPSColor(last100t))).append(" (Lowest: ")
                        .append(Component.literal(Double.toString(l100t)).withStyle(getTPSColor(l100t)))
                        .append(")"),
                false);
        source.sendSuccess(
                Component.literal("Average TPS in last 20 ticks (1 second): ")
                        .append(Component.literal(Double.toString(last20t)).withStyle(getTPSColor(last20t))).append(" (Lowest: ")
                        .append(Component.literal(Double.toString(l20t)).withStyle(getTPSColor(l20t)))
                        .append(")"),
                false);
        source.sendSuccess(
                Component.literal("Average TPS in last 10 ticks (500 ms): ")
                        .append(Component.literal(Double.toString(last10t)).withStyle(getTPSColor(last10t))).append(" (Lowest: ")
                        .append(Component.literal(Double.toString(l10t)).withStyle(getTPSColor(l10t)))
                        .append(")"),
                false);
        return 1;
    }

    public static int executeVersionCommand(@NotNull CommandSourceStack source) {
        BlueberryVersion v = Versioning.getVersion();
        boolean cached = VersionChecker.isCached();
        MutableComponent versionDiff = BlueberryCommonComponents.EMPTY_TEXT;
        if (cached) {
            try {
                VersionChecker.Result result = VersionChecker.check(true).get();
                String key = result.getStatusKey();
                if (key.equals("diverged") || key.equals("ahead") || key.equals("behind") || key.equals("clean")) {
                    versionDiff = Component.literal("");
                    versionDiff.append(Component.literal(" (").withStyle(ChatFormatting.GRAY));
                    ChatFormatting formatting = getChatFormattingForVersionCheckerKey(key);
                    switch (key) {
                        case "diverged" -> versionDiff.append(BlueberryText.text("blueberry", "blueberry.mod.command.version.checker.diverged", result.ahead(), result.behind()).withStyle(formatting));
                        case "ahead" -> versionDiff.append(BlueberryText.text("blueberry", "blueberry.mod.command.version.checker.ahead", result.ahead()).withStyle(formatting));
                        case "behind" -> versionDiff.append(BlueberryText.text("blueberry", "blueberry.mod.command.version.checker.behind", result.behind()).withStyle(formatting));
                        case "clean" -> versionDiff.append(BlueberryText.text("blueberry", "blueberry.mod.command.version.checker.clean").withStyle(formatting));
                    }
                    versionDiff.append(Component.literal(")").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.ITALIC);
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.warn("Version checker threw exception when fetching cached result", e);
            }
        }
        source.sendSuccess(
                Component.literal(" |  ")
                        .append(Component.literal(Util.capitalize(v.getName())).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                        .append(" ")
                        .append(Component.literal((Character.isDigit(v.getVersion().charAt(0)) ? "v" : "") + v.getVersion() + "." + v.getBuildNumber()).withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(" (").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal("API " + (Character.isDigit(v.getVersion().charAt(0)) ? "v" : "") + v.getVersion()).withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal(") (").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal("Minecraft " + SharedConstants.getCurrentVersion().getId()).withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(")").withStyle(ChatFormatting.DARK_GRAY)),
                false);
        source.sendSuccess(
                Component.literal(" |  ")
                        .append(BlueberryText.text("blueberry", "blueberry.mod.command.version.built_at").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                        .append(Component.literal(v.getBuiltAt())),
                false);
        source.sendSuccess(
                Component.literal(" |  ")
                        .append(BlueberryText.text("blueberry", "blueberry.mod.command.version.commit_hash").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                        .append(Component.literal(v.getShortCommit()).withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to open GitHub"))).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/" + Constants.GITHUB_REPO + "/commit/" + v.getCommit()))))
                        .append(versionDiff),
                false);
        source.sendSuccess(
                Component.literal(" |  ")
                        .append(BlueberryText.text("blueberry", "blueberry.mod.command.version.magmacube_commit_hash").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                        .append(Component.literal(v.getShortMagmaCubeCommit())
                                .withStyle(style ->
                                        style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to open GitHub")))
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/" + Constants.GITHUB_MAGMA_CUBE_REPO + "/commit/" + v.getMagmaCubeCommit())))
                        ),
                false);
        if (!cached) {
            source.sendSuccess(Component.literal("").append(BlueberryText.text("blueberry", "blueberry.mod.command.version.checking_for_new_version").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC)), false);
            VersionChecker.check().thenAccept(result -> {
                String key = result.getStatusKey();
                Object[] args = switch (key) {
                    case "diverged" -> new Object[] { result.ahead(), result.behind() };
                    case "ahead" -> new Object[] { result.ahead() };
                    case "behind" -> new Object[] { result.behind() };
                    default -> new Object[0];
                };
                MutableComponent text = BlueberryText.text("blueberry", "blueberry.mod.command.version.checker." + key, args);
                if (key.equals("error")) {
                    source.sendSuccess(text.withStyle(ChatFormatting.RED), false);
                } else {
                    source.sendSuccess(BlueberryText.text("blueberry", "blueberry.mod.command.version.checker.result").append(text).withStyle(getChatFormattingForVersionCheckerKey(key)), false);
                }
            });
        }
        return 1;
    }

    public static int executePermissionCheckCommand(@NotNull CommandSourceStack source, @NotNull String permission, @Nullable Player player) {
        PermissionHolder holder = (PermissionHolder) Objects.requireNonNullElse(player, source);
        PermissionState state = holder.getPermissionState(permission);
        source.sendSuccess(Component.literal("")
                .append(Component.literal("Permission check for ").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(permission).withStyle(ChatFormatting.GOLD)), false);
        source.sendSuccess(Component.literal("")
                .append(Component.literal("  Result: ").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(state.name().toLowerCase()).withStyle(getChatFormattingForPermissionState(state))), false);
        return 0;
    }

    @Contract(pure = true)
    private static ChatFormatting getChatFormattingForPermissionState(PermissionState state) {
        return switch (state) {
            case TRUE -> ChatFormatting.GREEN;
            case FALSE -> ChatFormatting.RED;
            case UNDEFINED -> ChatFormatting.GRAY;
        };
    }

    private static ChatFormatting getChatFormattingForVersionCheckerKey(String key) {
        return switch (key) {
            case "diverged" -> ChatFormatting.LIGHT_PURPLE;
            case "ahead" -> ChatFormatting.AQUA;
            case "behind" -> ChatFormatting.YELLOW;
            case "clean" -> ChatFormatting.GREEN;
            default -> ChatFormatting.GRAY;
        };
    }

    @NotNull
    private static ChatFormatting getTPSColor(double tps) {
        if (tps > 20) return ChatFormatting.AQUA;
        if (tps > 19) return ChatFormatting.GREEN;
        if (tps > 17.5) return ChatFormatting.YELLOW;
        if (tps > 14) return ChatFormatting.RED;
        return ChatFormatting.DARK_RED;
    }

    private static double getAverageTPS(long[] longs) {
        return getAverageTPS(Arrays.stream(longs));
    }

    private static double getAverageTPS(LongStream stream) {
        return stream.mapToDouble(BlueberryCommand::toTPS).average().orElse(20.0);
    }

    private static double getLowestTPS(long[] longs) {
        return getLowestTPS(Arrays.stream(longs));
    }

    private static double getLowestTPS(LongStream stream) {
        AtomicDouble atomicDouble = new AtomicDouble(Double.MAX_VALUE);
        stream.mapToDouble(BlueberryCommand::toTPS).forEach(l -> {
            if (l < atomicDouble.get()) atomicDouble.set(l);
        });
        if (atomicDouble.get() == Double.MAX_VALUE) return 0L;
        return atomicDouble.get();
    }

    private static double toTPS(long l) {
        return 1000 / Math.max(50, l / 1000000D);
    }

    private static double round(double d) {
        return Math.round(d * 100) / 100D;
    }
}
