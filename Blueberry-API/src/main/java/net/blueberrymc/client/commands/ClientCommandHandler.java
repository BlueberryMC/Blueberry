package net.blueberrymc.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.ModClassLoader;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Interface for registering single client command.
 */
@FunctionalInterface
public interface ClientCommandHandler {
    /**
     * Registers a command. Client command will not work if you register a command under different/wrong name here.
     * @param dispatcher command dispatcher
     */
    void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher);

    /**
     * Returns the mod for the client command handler
     * @param handler command handler
     * @return the mod; returns blueberry mod if the mod could not be determined
     */
    @NotNull
    static BlueberryMod getMod(@NotNull ClientCommandHandler handler) {
        if (handler.getClass().getClassLoader() instanceof ModClassLoader mcl) {
            return mcl.getMod();
        }
        return Objects.requireNonNull(Blueberry.getModManager().getModById("blueberry"));
    }
}
