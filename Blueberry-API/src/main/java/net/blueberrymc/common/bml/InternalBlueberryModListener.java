package net.blueberrymc.common.bml;

import net.blueberrymc.client.event.render.gui.ScreenChangedEvent;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.EventHandler;
import net.blueberrymc.common.bml.event.Listener;
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

    public class Server implements Listener {
    }

    public class Client implements Listener {
        @EventHandler
        public void onScreenChanged(@NotNull ScreenChangedEvent e) {
            if (Blueberry.getCurrentState() != ModState.AVAILABLE) return;
            InternalBlueberryModListener.this.mod.refreshDiscordStatus(e.getScreen());
        }
    }
}
