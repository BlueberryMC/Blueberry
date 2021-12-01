package net.blueberrymc.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.ModClassLoader;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@FunctionalInterface
public interface ClientCommandHandler {
    void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher);

    @NotNull
    static BlueberryMod getMod(@NotNull ClientCommandHandler handler) {
        if (handler.getClass().getClassLoader() instanceof ModClassLoader mcl) {
            return mcl.getMod();
        }
        return Objects.requireNonNull(Blueberry.getModManager().getModById("blueberry"));
    }
}
