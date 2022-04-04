package net.blueberrymc.client.event.player;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.CancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when the local player (the client) tries to send a chat message. If the event is cancelled, the chat message
 * will not be sent. If the message was changed through {@link #setMessage(String)}, the new message will be sent.
 */
public class ClientLocalPlayerChatEvent extends CancellableEvent {
    @NotNull
    private String message;

    public ClientLocalPlayerChatEvent(@NotNull String message) {
        super(!Blueberry.getUtil().isOnGameThread());
        this.message = message;
    }

    /**
     * Gets the message that the player is trying to send.
     * @return the message
     */
    @NotNull
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message that the player is trying to send.
     * @param message the message
     */
    public void setMessage(@NotNull String message) {
        this.message = message;
    }
}
