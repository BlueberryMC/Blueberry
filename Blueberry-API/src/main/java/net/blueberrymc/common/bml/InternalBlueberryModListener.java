package net.blueberrymc.common.bml;

import net.blueberrymc.client.event.render.gui.ScreenChangedEvent;
import net.blueberrymc.command.argument.ArgumentTypes;
import net.blueberrymc.command.argument.ModIdArgument;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.EventHandler;
import net.blueberrymc.common.event.lifecycle.RegistryBootstrappedEvent;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record InternalBlueberryModListener(@NotNull InternalBlueberryMod mod) {
    @EventHandler
    public static void onRegistryBootstrap(@NotNull RegistryBootstrappedEvent e) {
        registerArgumentTypes();
    }

    private static void registerArgumentTypes() {
        ArgumentTypes.register("blueberry:modid", ModIdArgument.class, SingletonArgumentInfo.contextFree(ModIdArgument::modId));
    }

    @Contract(value = " -> new", pure = true)
    @NotNull
    public Server createServer() {
        return new Server();
    }

    @Contract(value = " -> new", pure = true)
    @NotNull
    public Client createClient() {
        return new Client();
    }

    public class Server {
    }

    public class Client {
        @EventHandler
        public void onScreenChanged(@NotNull ScreenChangedEvent e) {
            if (Blueberry.getCurrentState() != ModState.AVAILABLE) return;
            InternalClientBlueberryMod.refreshDiscordStatus(e.getScreen());
        }
    }
}
