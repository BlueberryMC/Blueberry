package net.blueberrymc.impl.common.event;

import com.mojang.brigadier.CommandDispatcher;
import net.blueberrymc.command.BlueberryCommand;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.event.block.PlayerBlockDropItemEvent;
import net.blueberrymc.common.event.command.CommandRegistrationEvent;
import net.blueberrymc.impl.server.entity.BlueberryServerPlayer;
import net.blueberrymc.impl.util.PositionUtil;
import net.blueberrymc.impl.world.BlueberryWorld;
import net.blueberrymc.impl.world.level.block.state.BlueberryBlockState;
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
        var world = new BlueberryWorld(level);
        var vec3i = PositionUtil.toBlueberry(pos);
        var state = new BlueberryBlockState(blockState);
        var serverPlayer = new BlueberryServerPlayer(player);
        var event = Event.callEvent(new PlayerBlockDropItemEvent(new CapturedBlock(world, vec3i, state), serverPlayer, items));
        if (!event.isCancelled()) {
            for (ItemEntity item : event.getItems()) {
                level.addFreshEntity(item);
            }
        }
    }
}
