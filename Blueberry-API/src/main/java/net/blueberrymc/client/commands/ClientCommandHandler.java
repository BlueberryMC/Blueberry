package net.blueberrymc.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.ModClassLoader;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Interface for registering single client command.
 */
@FunctionalInterface
public interface ClientCommandHandler {
    static <T> RequiredArgumentBuilder<CommandSource, T> argument(@NotNull String name, @NotNull ArgumentType<T> type) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        return RequiredArgumentBuilder.argument(name, type);
    }

    static LiteralArgumentBuilder<CommandSource> literal(@NotNull String name) {
        Objects.requireNonNull(name, "name cannot be null");
        return LiteralArgumentBuilder.literal(name);
    }

    /**
     * Registers a command. Client command will not work if you register a command under different/wrong name here.
     * @param dispatcher command dispatcher
     */
    void register(@NotNull CommandDispatcher<CommandSource> dispatcher);

    /**
     * Returns the mod for the client command handler
     * @param handler command handler
     * @return the mod; returns blueberry mod if the mod could not be determined
     */
    @NotNull
    static BlueberryMod getMod(@NotNull ClientCommandHandler handler) {
        if (handler.getClass().getClassLoader() instanceof ModClassLoader mcl) {
            BlueberryMod mod = mcl.getMod();
            //noinspection ConstantConditions // for some reason, the mod becomes null after ModClassLoader is closed
            if (mod != null) return mod;
        }
        return Objects.requireNonNull(Blueberry.getModLoader().getModById("blueberry"));
    }
}
