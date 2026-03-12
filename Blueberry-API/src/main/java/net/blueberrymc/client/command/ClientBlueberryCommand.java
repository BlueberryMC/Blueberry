package net.blueberrymc.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.activity.ActivityJoinRequestReply;
import net.blueberrymc.client.commands.ClientCommandHandler;
import net.blueberrymc.command.BlueberryCommand;
import net.blueberrymc.common.resources.BlueberryText;
import net.blueberrymc.common.util.DiscordRPCTaskExecutor;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import static net.blueberrymc.client.commands.ClientCommandHandler.literal;
import static net.blueberrymc.client.commands.ClientCommandHandler.argument;

/**
 * <code>/cblueberry</code> command definition and implementation
 */
public class ClientBlueberryCommand implements ClientCommandHandler {
    @Override
    public void register(@NotNull CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                literal("cblueberry")
                        .then(literal("version")
                                .executes(context -> BlueberryCommand.executeVersionCommand(context.getSource()))
                        )
                        .then(literal("permission")
                                .then(literal("check")
                                        .then(argument("node", StringArgumentType.string())
                                                .executes(context -> BlueberryCommand.executePermissionCheckCommand(context.getSource(), StringArgumentType.getString(context, "node"), null))
                                        )
                                )
                        )
                        .then(literal("discord")
                                .then(literal("accept")
                                        .then(argument("id", LongArgumentType.longArg(0, Long.MAX_VALUE))
                                                .executes(context -> executeDiscordAccept(context.getSource(), LongArgumentType.getLong(context, "id")))
                                        )
                                )
                                .then(literal("deny")
                                        .then(argument("id", LongArgumentType.longArg(0, Long.MAX_VALUE))
                                                .executes(context -> executeDiscordDeny(context.getSource(), LongArgumentType.getLong(context, "id")))
                                        )
                                )
                                .then(literal("ignore")
                                        .then(argument("id", LongArgumentType.longArg(0, Long.MAX_VALUE))
                                                .executes(context -> executeDiscordIgnore(context.getSource(), LongArgumentType.getLong(context, "id")))
                                        )
                                )
                        )
        );
    }

    private static int executeDiscordAccept(@NotNull CommandSource source, long id) {
        DiscordRPCTaskExecutor.submitTask(core -> core.activityManager().sendRequestReply(id, ActivityJoinRequestReply.YES, result -> {
            if (result == Result.OK) {
                source.sendSystemMessage(BlueberryText.text("blueberry", "discord.activity.join_request.accept.accepted"));
            } else {
                source.sendSystemMessage(BlueberryText.text("blueberry", "discord.error_with_result", result.name()).withStyle(ChatFormatting.RED));
            }
        }));
        return 0;
    }

    private static int executeDiscordDeny(@NotNull CommandSource source, long id) {
        DiscordRPCTaskExecutor.submitTask(core -> core.activityManager().sendRequestReply(id, ActivityJoinRequestReply.NO, result -> {
            if (result == Result.OK) {
                source.sendSystemMessage(BlueberryText.text("blueberry", "discord.activity.join_request.deny.denied"));
            } else {
                source.sendSystemMessage(BlueberryText.text("blueberry", "discord.error_with_result", result.name()).withStyle(ChatFormatting.RED));
            }
        }));
        return 0;
    }

    private static int executeDiscordIgnore(@NotNull CommandSource source, long id) {
        DiscordRPCTaskExecutor.submitTask(core -> core.activityManager().sendRequestReply(id, ActivityJoinRequestReply.IGNORE, result -> {
            if (result == Result.OK) {
                source.sendSystemMessage(BlueberryText.text("blueberry", "discord.activity.join_request.ignore.ignored"));
            } else {
                source.sendSystemMessage(BlueberryText.text("blueberry", "discord.error_with_result", result.name()).withStyle(ChatFormatting.RED));
            }
        }));
        return 0;
    }
}
