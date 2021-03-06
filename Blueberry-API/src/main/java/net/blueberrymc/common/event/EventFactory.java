package net.blueberrymc.common.event;

import com.mojang.brigadier.CommandDispatcher;
import net.blueberrymc.command.BlueberryCommand;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.event.block.PlayerBlockDropItemEvent;
import net.blueberrymc.common.event.command.CommandRegistrationEvent;
import net.blueberrymc.world.level.block.CapturedBlock;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EventFactory {
    public static void callCommandRegistrationEvent(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, @NotNull Commands.CommandSelection commandSelection) {
        BlueberryCommand.register(dispatcher);
        new CommandRegistrationEvent(dispatcher, commandSelection).callEvent();
    }

    public static void handlePlayerBlockDropItemEvent(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState blockState, @NotNull ServerPlayer player, @NotNull List<ItemEntity> items) {
        PlayerBlockDropItemEvent event = Event.callEvent(new PlayerBlockDropItemEvent(new CapturedBlock(level, pos, blockState), player, items));
        if (!event.isCancelled()) {
            for (ItemEntity item : event.getItems()) {
                level.addFreshEntity(item);
            }
        }
    }
}
