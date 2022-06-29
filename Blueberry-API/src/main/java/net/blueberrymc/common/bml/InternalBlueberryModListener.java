package net.blueberrymc.common.bml;

import net.blueberrymc.client.event.render.gui.ScreenChangedEvent;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.EventHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record InternalBlueberryModListener(@NotNull InternalBlueberryMod mod) {
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
