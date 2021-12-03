package net.blueberrymc.client.command;

import com.mojang.brigadier.CommandDispatcher;
import net.blueberrymc.client.commands.ClientCommandHandler;
import net.blueberrymc.command.BlueberryCommand;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.commands.Commands.literal;

public class ClientBlueberryCommand implements ClientCommandHandler {
    @Override
    public void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("cblueberry")
                        .then(literal("version")
                                .executes(context -> BlueberryCommand.executeVersionCommand(context.getSource()))
                        )
        );
    }
}
