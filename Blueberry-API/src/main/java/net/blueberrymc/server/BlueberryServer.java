package net.blueberrymc.server;

import net.blueberrymc.common.BlueberryUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlueberryServer implements BlueberryUtil {
    @Override
    public @Nullable ResourceManager getResourceManager() {
        return null;
    }

    private MinecraftServer server = null;
    public void setServer(@NotNull MinecraftServer minecraftServer) {
        if (this.server != null) throw new IllegalArgumentException("Cannot redefine MinecraftServer");
        this.server = minecraftServer;
    }

    /**
     * Returns the current minecraft server. May be null if not initialized yet.
     * @return minecraft server
     */
    @Nullable
    public MinecraftServer getServer() {
        return server;
    }

    public void stopServer() {
        if (this.server instanceof DedicatedServer) {
            ((DedicatedServer) this.server).stopServer();
        }
    }

    @Override
    public void reloadResourcePacks() {}
}
