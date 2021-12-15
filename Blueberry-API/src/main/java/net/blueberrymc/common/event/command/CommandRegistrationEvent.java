package net.blueberrymc.common.event.command;

import com.mojang.brigadier.CommandDispatcher;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Fired when the command is about to be registered. <b>Unlike most events, listener of this event must be registered
 * at {@link BlueberryMod#onLoad()} phase.</b>
 */
public class CommandRegistrationEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    protected final CommandDispatcher<CommandSourceStack> dispatcher;
    protected final Commands.CommandSelection commandSelection;

    public CommandRegistrationEvent(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, @NotNull Commands.CommandSelection commandSelection) {
        this(dispatcher, commandSelection, !Blueberry.getUtil().isOnGameThread());
    }

    protected CommandRegistrationEvent(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, @NotNull Commands.CommandSelection commandSelection, boolean async) {
        super(async);
        this.dispatcher = dispatcher;
        this.commandSelection = commandSelection;
    }

    @NotNull
    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return dispatcher;
    }

    @NotNull
    public Commands.CommandSelection getCommandSelection() {
        return commandSelection;
    }

    public void register(@NotNull Consumer<CommandDispatcher<CommandSourceStack>> registerer) {
        registerer.accept(dispatcher);
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
