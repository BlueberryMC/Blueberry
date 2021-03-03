package net.blueberrymc.common.event;

import com.mojang.brigadier.CommandDispatcher;
import net.blueberrymc.command.BlueberryCommand;
import net.blueberrymc.common.event.command.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class EventFactory {
    public static void callCommandRegistrationEvent(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        BlueberryCommand.register(dispatcher);
        new CommandRegistrationEvent(dispatcher).callEvent();
    }
}
