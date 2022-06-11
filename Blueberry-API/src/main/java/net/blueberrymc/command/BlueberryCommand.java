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
import net.blueberrymc.common.text.BlueberryText;
import net.blueberrymc.common.util.BlueberryVersion;
import net.blueberrymc.common.util.VersionChecker;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.server.BlueberryServer;
import net.blueberrymc.util.Constants;
import net.blueberrymc.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
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
    private static final SimpleCommandExceptionType UNAVAILABLE_IN_THIS_ENVIRONMENT = new SimpleCommandExceptionType(Component.text("This command is not available in this environment.").withStyle(ChatFormatting.RED));
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
        source.sendSuccess(Component.text("Mod status of '" + mod.getName() + "': " + mod.getStateList() + " (Current: " + mod.getStateList().getCurrentState().getName() + ")"), false);
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
                Component.text("Average TPS in last 100 ticks (5 seconds): ")
                        .append(Component.text(Double.toString(last100t), getTPSColor(last100t)))
                        .append(Component.text(" (Lowest: "))
                        .append(Component.text(Double.toString(l100t), getTPSColor(l100t)))
                        .append(Component.text(")")),
                false);
        source.sendSuccess(
                Component.text("Average TPS in last 20 ticks (1 second): ")
                        .append(Component.text(Double.toString(last20t), getTPSColor(last20t)))
                        .append(Component.text(" (Lowest: "))
                        .append(Component.text(Double.toString(l20t), getTPSColor(l20t)))
                        .append(Component.text(")")),
                false);
        source.sendSuccess(
                Component.text("Average TPS in last 10 ticks (500 ms): ")
                        .append(Component.text(Double.toString(last10t), getTPSColor(last10t)))
                        .append(Component.text(" (Lowest: "))
                        .append(Component.text(Double.toString(l10t), getTPSColor(l10t)))
                        .append(Component.text(")")),
                false);
        return 1;
    }

    public static int executeVersionCommand(@NotNull CommandSourceStack source) {
        BlueberryVersion v = Versioning.getVersion();
        boolean cached = VersionChecker.isCached();
        Component versionDiff = Component.empty();
        if (cached) {
            try {
                VersionChecker.Result result = VersionChecker.check(true).get();
                String key = result.getStatusKey();
                if (key.equals("diverged") || key.equals("ahead") || key.equals("behind") || key.equals("clean")) {
                    versionDiff = Component.text("");
                    versionDiff = versionDiff.append(Component.text(" (", NamedTextColor.GRAY));
                    TextColor color = getColorForVersionCheckerKey(key);
                    switch (key) {
                        case "diverged" -> versionDiff = versionDiff.append(BlueberryText.text("blueberry", "blueberry.mod.command.version.checker.diverged", result.ahead(), result.behind()).withColor(color));
                        case "ahead" -> versionDiff = versionDiff.append(BlueberryText.text("blueberry", "blueberry.mod.command.version.checker.ahead", result.ahead()).withColor(color));
                        case "behind" -> versionDiff = versionDiff.append(BlueberryText.text("blueberry", "blueberry.mod.command.version.checker.behind", result.behind()).withColor(color));
                        case "clean" -> versionDiff = versionDiff.append(BlueberryText.text("blueberry", "blueberry.mod.command.version.checker.clean").withColor(color));
                    }
                    versionDiff = versionDiff.append(Component.text(")", NamedTextColor.GRAY)).decorate(TextDecoration.ITALIC);
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.warn("Version checker threw exception when fetching cached result", e);
            }
        }
        source.sendSuccess(
                Component.text(" |  ")
                        .append(Component.text(Util.capitalize(v.getName()), NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                        .append(Component.space())
                        .append(Component.text((Character.isDigit(v.getVersion().charAt(0)) ? "v" : "") + v.getVersion() + "." + v.getBuildNumber(), NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text(" (", NamedTextColor.DARK_GRAY))
                        .append(Component.text("API " + (Character.isDigit(v.getVersion().charAt(0)) ? "v" : "") + v.getVersion(), NamedTextColor.DARK_GREEN))
                        .append(Component.text(") (", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Minecraft " + Blueberry.getVersion().getGameVersion(), NamedTextColor.GRAY))
                        .append(Component.text(")", NamedTextColor.DARK_GRAY)),
                false);
        source.sendSuccess(
                Component.text(" |  ")
                        .append(BlueberryText.text("blueberry", "blueberry.mod.command.version.built_at").withColor(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                        .append(Component.text(v.getBuiltAt())),
                false);
        source.sendSuccess(
                Component.text(" |  ")
                        .append(BlueberryText.text("blueberry", "blueberry.mod.command.version.commit_hash").withColor(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                        .append(Component.text(v.getShortCommit())
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to open GitHub")))
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/" + Constants.GITHUB_REPO + "/commit/" + v.getCommit())))
                        .append(versionDiff),
                false);
        source.sendSuccess(
                Component.text(" |  ")
                        .append(BlueberryText.text("blueberry", "blueberry.mod.command.version.magmacube_commit_hash").withColor(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                        .append(Component.text(v.getShortMagmaCubeCommit())
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to open GitHub")))
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/" + Constants.GITHUB_MAGMA_CUBE_REPO + "/commit/" + v.getMagmaCubeCommit()))
                        ),
                false);
        if (!cached) {
            source.sendSuccess(Component.empty().append(BlueberryText.text("blueberry", "blueberry.mod.command.version.checking_for_new_version").withColor(NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC)), false);
            VersionChecker.check().thenAccept(result -> {
                String key = result.getStatusKey();
                Object[] args = switch (key) {
                    case "diverged" -> new Object[] { result.ahead(), result.behind() };
                    case "ahead" -> new Object[] { result.ahead() };
                    case "behind" -> new Object[] { result.behind() };
                    default -> new Object[0];
                };
                Component text = BlueberryText.text("blueberry", "blueberry.mod.command.version.checker." + key, args).asComponent();
                if (key.equals("error")) {
                    source.sendSuccess(text.color(NamedTextColor.RED), false);
                } else {
                    source.sendSuccess(BlueberryText.text("blueberry", "blueberry.mod.command.version.checker.result").asComponent().append(text).color(getColorForVersionCheckerKey(key)), false);
                }
            });
        }
        return 1;
    }

    public static int executePermissionCheckCommand(@NotNull CommandSourceStack source, @NotNull String permission, @Nullable Player player) {
        PermissionHolder holder = (PermissionHolder) Objects.requireNonNullElse(player, source);
        PermissionState state = holder.getPermissionState(permission);
        source.sendSuccess(Component.empty()
                .append(Component.text("Permission check for ", NamedTextColor.AQUA))
                .append(Component.text(permission, NamedTextColor.GOLD)), false);
        source.sendSuccess(Component.empty()
                .append(Component.text("  Result: ", NamedTextColor.AQUA))
                .append(Component.text(state.name().toLowerCase(), getColorForPermissionState(state))), false);
        return 0;
    }

    @Contract(pure = true)
    private static TextColor getColorForPermissionState(PermissionState state) {
        return switch (state) {
            case TRUE -> NamedTextColor.GREEN;
            case FALSE -> NamedTextColor.RED;
            case UNDEFINED -> NamedTextColor.GRAY;
        };
    }

    private static TextColor getColorForVersionCheckerKey(String key) {
        return switch (key) {
            case "diverged" -> NamedTextColor.LIGHT_PURPLE;
            case "ahead" -> NamedTextColor.AQUA;
            case "behind" -> NamedTextColor.YELLOW;
            case "clean" -> NamedTextColor.GREEN;
            default -> NamedTextColor.GRAY;
        };
    }

    @NotNull
    private static TextColor getTPSColor(double tps) {
        if (tps > 20) return NamedTextColor.AQUA;
        if (tps > 19) return NamedTextColor.GREEN;
        if (tps > 17.5) return NamedTextColor.YELLOW;
        if (tps > 14) return NamedTextColor.RED;
        return NamedTextColor.DARK_RED;
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
