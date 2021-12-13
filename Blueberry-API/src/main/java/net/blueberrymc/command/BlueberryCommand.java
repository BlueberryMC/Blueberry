package net.blueberrymc.command;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.blueberrymc.client.BlueberryClient;
import net.blueberrymc.common.resources.BlueberryText;
import net.blueberrymc.command.argument.ModIdArgument;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.BlueberryUtil;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.event.mod.ModReloadEvent;
import net.blueberrymc.common.util.BlueberryVersion;
import net.blueberrymc.common.util.VersionChecker;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.server.BlueberryServer;
import net.blueberrymc.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.stream.LongStream;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BlueberryCommand {
    private static final SimpleCommandExceptionType UNAVAILABLE_IN_THIS_ENVIRONMENT = new SimpleCommandExceptionType(new TextComponent("This command is not available in this environment.").withStyle(ChatFormatting.RED));
    private static final Logger LOGGER = LogManager.getLogger();
    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("blueberry")
                        .requires(source -> source.hasPermission(4))
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
        );
    }

    private static int executeModStatusCommand(CommandSourceStack source, BlueberryMod mod) {
        source.sendSuccess(new TextComponent("Mod status of '" + mod.getName() + "': " + mod.getStateList() + " (Current: " + mod.getStateList().getCurrentState().getName() + ")"), false);
        return 1;
    }

    private static int executeModReloadCommand(CommandSourceStack source, BlueberryMod mod) {
        ServerPlayer player = null;
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException ignore) {}
        if (new ModReloadEvent(player, mod).callEvent()) {
            source.sendSuccess(new BlueberryText("blueberry", "blueberry.mod.command.mod.reload.reloading", mod.getName()), true);
            try {
                if (mod.onReload()) {
                    Minecraft.getInstance().reloadResourcePacks().thenAccept(v -> source.sendSuccess(new BlueberryText("blueberry", "blueberry.mod.command.mod.reload.success", mod.getName()), true));
                } else {
                    source.sendSuccess(new BlueberryText("blueberry", "blueberry.mod.command.mod.reload.success", mod.getName()), true);
                }
            } catch (RuntimeException ex) {
                source.sendFailure(new BlueberryText("blueberry", "blueberry.mod.command.mod.reload.failure.error", mod.getName(), ex.getMessage()));
                LOGGER.warn("Failed to reload mod {}", mod.getName(), ex);
            }
        } else {
            source.sendFailure(new BlueberryText("blueberry", "blueberry.mod.command.mod.reload.failure.cancelled", mod.getName()));
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
        source.sendSuccess(new TextComponent("Average TPS in last 100 ticks (5 seconds): ").append(new TextComponent(Double.toString(last100t)).withStyle(getTPSColor(last100t))).append(" (Lowest: ").append(new TextComponent(Double.toString(l100t)).withStyle(getTPSColor(l100t))).append(")"), false);
        source.sendSuccess(new TextComponent("Average TPS in last 20 ticks (1 second): ").append(new TextComponent(Double.toString(last20t)).withStyle(getTPSColor(last20t))).append(" (Lowest: ").append(new TextComponent(Double.toString(l20t)).withStyle(getTPSColor(l20t))).append(")"), false);
        source.sendSuccess(new TextComponent("Average TPS in last 10 ticks (500 ms): ").append(new TextComponent(Double.toString(last10t)).withStyle(getTPSColor(last10t))).append(" (Lowest: ").append(new TextComponent(Double.toString(l10t)).withStyle(getTPSColor(l10t))).append(")"), false);
        return 1;
    }

    public static int executeVersionCommand(@NotNull CommandSourceStack source) {
        BlueberryVersion v = Versioning.getVersion();
        boolean cached = VersionChecker.isCached();
        MutableComponent versionDiff = (MutableComponent) TextComponent.EMPTY;
        if (cached) {
            try {
                VersionChecker.Result result = VersionChecker.check(true).get();
                String key = result.getStatusKey();
                if (key.equals("diverged") || key.equals("ahead") || key.equals("behind") || key.equals("clean")) {
                    versionDiff = new TextComponent("");
                    versionDiff.append(new TextComponent(" (").withStyle(ChatFormatting.GRAY));
                    ChatFormatting formatting = getChatFormattingForVersionCheckerKey(key);
                    switch (key) {
                        case "diverged" -> versionDiff.append(new BlueberryText("blueberry", "blueberry.mod.command.version.checker.diverged", result.ahead(), result.behind()).withStyle(formatting));
                        case "ahead" -> versionDiff.append(new BlueberryText("blueberry", "blueberry.mod.command.version.checker.ahead", result.ahead()).withStyle(formatting));
                        case "behind" -> versionDiff.append(new BlueberryText("blueberry", "blueberry.mod.command.version.checker.behind", result.behind()).withStyle(formatting));
                        case "clean" -> versionDiff.append(new BlueberryText("blueberry", "blueberry.mod.command.version.checker.clean").withStyle(formatting));
                    }
                    versionDiff.append(new TextComponent(")").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.ITALIC);
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.warn("Version checker threw exception when fetching cached result", e);
            }
        }
        source.sendSuccess(new TextComponent(" |  ").append(new TextComponent(Util.capitalize(v.getName())).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)).append(" ").append(new TextComponent((Character.isDigit(v.getVersion().charAt(0)) ? "v" : "") + v.getVersion() + "." + v.getBuildNumber()).withStyle(ChatFormatting.LIGHT_PURPLE)).append(new TextComponent(" (").withStyle(ChatFormatting.DARK_GRAY)).append(new TextComponent("API " + (Character.isDigit(v.getVersion().charAt(0)) ? "v" : "") + v.getVersion()).withStyle(ChatFormatting.DARK_GREEN)).append(new TextComponent(") (").withStyle(ChatFormatting.DARK_GRAY)).append(new TextComponent("Minecraft " + SharedConstants.getCurrentVersion().getId()).withStyle(ChatFormatting.GRAY)).append(new TextComponent(")").withStyle(ChatFormatting.DARK_GRAY)), false);
        source.sendSuccess(new TextComponent(" |  ").append(new BlueberryText("blueberry", "blueberry.mod.command.version.built_at").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)).append(new TextComponent(v.getBuiltAt())), false);
        source.sendSuccess(new TextComponent(" |  ").append(new BlueberryText("blueberry", "blueberry.mod.command.version.commit_hash").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)).append(new TextComponent(v.getShortCommit()).withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to copy"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, v.getCommit())))).append(versionDiff), false);
        source.sendSuccess(new TextComponent(" |  ").append(new BlueberryText("blueberry", "blueberry.mod.command.version.magmacube_commit_hash").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)).append(new TextComponent(v.getShortMagmaCubeCommit()).withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to copy"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, v.getMagmaCubeCommit())))), false);
        if (!cached) {
            source.sendSuccess(new TextComponent("").append(new BlueberryText("blueberry", "blueberry.mod.command.version.checking_for_new_version").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC)), false);
            VersionChecker.check().thenAccept(result -> {
                String key = result.getStatusKey();
                Object[] args = switch (key) {
                    case "diverged" -> new Object[] { result.ahead(), result.behind() };
                    case "ahead" -> new Object[] { result.ahead() };
                    case "behind" -> new Object[] { result.behind() };
                    default -> new Object[0];
                };
                BlueberryText text = new BlueberryText("blueberry", "blueberry.mod.command.version.checker." + key, args);
                if (key.equals("error")) {
                    source.sendSuccess(text.withStyle(ChatFormatting.RED), false);
                } else {
                    source.sendSuccess(new BlueberryText("blueberry", "blueberry.mod.command.version.checker.result").append(text).withStyle(getChatFormattingForVersionCheckerKey(key)), false);
                }
            });
        }
        return 1;
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
