package net.blueberrymc.command;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.blueberrymc.client.BlueberryClient;
import net.blueberrymc.client.resources.BlueberryText;
import net.blueberrymc.command.argument.ModIdArgument;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.BlueberryUtil;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.event.mod.ModReloadEvent;
import net.blueberrymc.server.BlueberryServer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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
        source.sendSuccess(new TextComponent("Average TPS in last 100 ticks: ").append(new TextComponent(Double.toString(last100t)).withStyle(getTPSColor(last100t))).append(" (Lowest: ").append(new TextComponent(Double.toString(l100t)).withStyle(getTPSColor(l100t))).append(")"), false);
        source.sendSuccess(new TextComponent("Average TPS in last 20 ticks: ").append(new TextComponent(Double.toString(last20t)).withStyle(getTPSColor(last20t))).append(" (Lowest: ").append(new TextComponent(Double.toString(l20t)).withStyle(getTPSColor(l20t))).append(")"), false);
        source.sendSuccess(new TextComponent("Average TPS in last 10 ticks: ").append(new TextComponent(Double.toString(last10t)).withStyle(getTPSColor(last10t))).append(" (Lowest: ").append(new TextComponent(Double.toString(l10t)).withStyle(getTPSColor(l10t))).append(")"), false);
        return 1;
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
