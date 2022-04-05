package net.blueberrymc.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.blueberrymc.client.commands.ClientCommandHandler;
import net.blueberrymc.command.BlueberryCommand;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * <code>/cblueberry</code> command implementation
 */
public class ClientBlueberryCommand implements ClientCommandHandler {
    @Override
    public void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
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
        );
    }
}
