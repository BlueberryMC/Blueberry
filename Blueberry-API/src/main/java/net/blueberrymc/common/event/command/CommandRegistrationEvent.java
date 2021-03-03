package net.blueberrymc.common.event.command;

import com.mojang.brigadier.CommandDispatcher;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CommandRegistrationEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    @NotNull
    protected final CommandDispatcher<CommandSourceStack> dispatcher;

    public CommandRegistrationEvent(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        this(dispatcher, !Blueberry.getUtil().isOnGameThread());
    }

    protected CommandRegistrationEvent(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, boolean async) {
        super(async);
        this.dispatcher = dispatcher;
    }

    @NotNull
    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return dispatcher;
    }

    public void register(@NotNull Consumer<CommandDispatcher<CommandSourceStack>> registerer) {
        registerer.accept(dispatcher);
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
