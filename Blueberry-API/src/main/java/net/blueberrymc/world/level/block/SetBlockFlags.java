package net.blueberrymc.world.level.block;

public class SetBlockFlags {
    public static final int UPDATE_NEIGHBOUR = 1;
    public static final int SEND_BLOCK_UPDATE = 2;
    public static final int SUPPRESS_SEND_BLOCK_UPDATE_ON_CLIENT = 4;
    public static final int SET_CLIENT_CHUNK_DIRTY_FROM_PLAYER = 8; // effective only for client (ChunkRenderDispatcher)
    public static final int NO_OBSERVER = 16; // SUPPRESS_UPDATE_NEIGHBOUR_SHAPES
    public static final int SUPPRESS_DROP_RESOURCES_FROM_BLOCK_ENTITY = 32;
    public static final int NOTIFY_BLOCK_UPDATE = 64;
    public static final int SUPPRESS_LIGHT_UPDATES = 128;
}
