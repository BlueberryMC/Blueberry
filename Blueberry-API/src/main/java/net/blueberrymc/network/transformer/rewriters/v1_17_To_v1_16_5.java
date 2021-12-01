package net.blueberrymc.network.transformer.rewriters;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.blueberrymc.network.transformer.PacketWrapper;
import net.blueberrymc.network.transformer.TransformableProtocolVersions;
import net.blueberrymc.util.IntPair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.ConnectionProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;

// https://wiki.vg/index.php?title=Pre-release_protocol&direction=prev&oldid=16888
public class v1_17_To_v1_16_5 extends v1_17_1_To_v1_17 {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int[] EMPTY_BIOME_DATA = new int[1024];

    public v1_17_To_v1_16_5() {
        this(TransformableProtocolVersions.v1_17, TransformableProtocolVersions.v1_16_5);
    }

    protected v1_17_To_v1_16_5(@NotNull TransformableProtocolVersions sourcePV, @NotNull TransformableProtocolVersions targetPV) {
        super(sourcePV, targetPV);
    }

    @Override
    protected void preRegister() {
        registerParticleRewriter();
    }

    @Override
    public void registerInbound() {
        // + 0x05: Sculk Vibration Signal
        remapInbound(ConnectionProtocol.PLAY, 0x05, 0x06); // Entity Animation
        remapInbound(ConnectionProtocol.PLAY, 0x06, 0x07); // Statistics
        remapInbound(ConnectionProtocol.PLAY, 0x07, 0x08); // Ack Player Digging
        remapInbound(ConnectionProtocol.PLAY, 0x08, 0x09); // Block Break Animation
        remapInbound(ConnectionProtocol.PLAY, 0x09, 0x0A); // Block Entity Data
        remapInbound(ConnectionProtocol.PLAY, 0x0A, 0x0B); // Block Action
        remapInbound(ConnectionProtocol.PLAY, 0x0B, 0x0C); // Block Change
        remapInbound(ConnectionProtocol.PLAY, 0x0C, 0x0D); // Boss Bar
        remapInbound(ConnectionProtocol.PLAY, 0x0D, 0x0E); // Server Difficulty
        remapInbound(ConnectionProtocol.PLAY, 0x0E, 0x0F); // Chat Message
        // + 0x10: Clear Titles
        remapInbound(ConnectionProtocol.PLAY, 0x0F, 0x11); // Tab-Complete
        remapInbound(ConnectionProtocol.PLAY, 0x10, 0x12); // Declare Commands
        // - 0x11 Window Confirmation
        remapInbound(ConnectionProtocol.PLAY, 0x12, 0x13); // Close Window
        remapInbound(ConnectionProtocol.PLAY, 0x13, 0x14); // Window Items
        remapInbound(ConnectionProtocol.PLAY, 0x14, 0x15); // Window Property
        remapInbound(ConnectionProtocol.PLAY, 0x15, 0x16); // Set Slot
        remapInbound(ConnectionProtocol.PLAY, 0x16, 0x17); // Set Cooldown
        remapInbound(ConnectionProtocol.PLAY, 0x17, 0x18); // Plugin Message
        remapInbound(ConnectionProtocol.PLAY, 0x18, 0x19); // Named Sound Effect
        remapInbound(ConnectionProtocol.PLAY, 0x19, 0x1A); // Disconnect
        remapInbound(ConnectionProtocol.PLAY, 0x1A, 0x1B); // Entity Status
        remapInbound(ConnectionProtocol.PLAY, 0x1B, 0x1C); // Explosion
        remapInbound(ConnectionProtocol.PLAY, 0x1C, 0x1D); // Unload Chunk
        remapInbound(ConnectionProtocol.PLAY, 0x1D, 0x1E); // Change Game State
        remapInbound(ConnectionProtocol.PLAY, 0x1E, 0x1F); // Open Horse Window
        // + 0x20: Initialize World Border
        remapInbound(ConnectionProtocol.PLAY, 0x1F, 0x21); // Keep Alive
        remapInbound(ConnectionProtocol.PLAY, 0x20, 0x22); // Chunk Data
        remapInbound(ConnectionProtocol.PLAY, 0x21, 0x23); // Effect
        remapInbound(ConnectionProtocol.PLAY, 0x22, 0x24); // Particle
        remapInbound(ConnectionProtocol.PLAY, 0x23, 0x25); // Update Light
        remapInbound(ConnectionProtocol.PLAY, 0x24, 0x26); // Join Game
        remapInbound(ConnectionProtocol.PLAY, 0x25, 0x27); // Map Data
        remapInbound(ConnectionProtocol.PLAY, 0x26, 0x28); // Trade List
        remapInbound(ConnectionProtocol.PLAY, 0x27, 0x29); // Entity Position
        remapInbound(ConnectionProtocol.PLAY, 0x28, 0x2A); // Entity Position and Rotation
        remapInbound(ConnectionProtocol.PLAY, 0x29, 0x2B); // Entity Rotation
        // - 0x2A: Entity Movement
        remapInbound(ConnectionProtocol.PLAY, 0x2B, 0x2C); // Vehicle Move
        remapInbound(ConnectionProtocol.PLAY, 0x2C, 0x2D); // Open Book
        remapInbound(ConnectionProtocol.PLAY, 0x2D, 0x2E); // Open Window
        remapInbound(ConnectionProtocol.PLAY, 0x2E, 0x2F); // Open Sign Editor
        // + 0x30: Ping
        remapInbound(ConnectionProtocol.PLAY, 0x2F, 0x31); // Craft Recipe Response
        remapInbound(ConnectionProtocol.PLAY, 0x30, 0x32); // Player Abilities
        // - 0x31: Combat Event
        // + 0x33: End Combat Event
        // + 0x34: Enter Combat Event
        // + 0x35: Death Combat Event
        remapInbound(ConnectionProtocol.PLAY, 0x32, 0x36); // Player Info
        remapInbound(ConnectionProtocol.PLAY, 0x33, 0x37); // Face Player
        // - 0x36: Destroy Entities -> 0x3a Destroy Entity
        remapInbound(ConnectionProtocol.PLAY, 0x34, 0x38); // Player Position And Look
        remapInbound(ConnectionProtocol.PLAY, 0x35, 0x39); // Unlock Recipes
        // + 0x3A: Destroy Entity <- 0x36 Destroy Entities
        remapInbound(ConnectionProtocol.PLAY, 0x37, 0x3B); // Remove Entity Effect
        remapInbound(ConnectionProtocol.PLAY, 0x38, 0x3C); // Resource Pack Send
        remapInbound(ConnectionProtocol.PLAY, 0x39, 0x3D); // Respawn
        remapInbound(ConnectionProtocol.PLAY, 0x3A, 0x3E); // Entity Head Look
        remapInbound(ConnectionProtocol.PLAY, 0x3B, 0x3F); // Multi Block Change
        remapInbound(ConnectionProtocol.PLAY, 0x3C, 0x40); // Select Advancement Tab
        // + 0x41: Action Bar
        // + 0x42: World Border Center
        // + 0x43: World Border Lerp Size
        // + 0x44: World Border Size
        // + 0x45: World Border Warning Delay
        // + 0x46: World Border Warning Reach
        remapInbound(ConnectionProtocol.PLAY, 0x3E, 0x47); // Camera
        remapInbound(ConnectionProtocol.PLAY, 0x3F, 0x48); // Held Item Change
        remapInbound(ConnectionProtocol.PLAY, 0x40, 0x49); // Update View Position
        remapInbound(ConnectionProtocol.PLAY, 0x41, 0x4A); // Update View Distance
        remapInbound(ConnectionProtocol.PLAY, 0x42, 0x4B); // Spawn Position
        remapInbound(ConnectionProtocol.PLAY, 0x43, 0x4C); // Display Scoreboard
        remapInbound(ConnectionProtocol.PLAY, 0x44, 0x4D); // Entity Metadata
        remapInbound(ConnectionProtocol.PLAY, 0x45, 0x4E); // Attach Entity
        remapInbound(ConnectionProtocol.PLAY, 0x46, 0x4F); // Entity Velocity
        remapInbound(ConnectionProtocol.PLAY, 0x47, 0x50); // Entity Equipment
        // - 0x4F: Title
        remapInbound(ConnectionProtocol.PLAY, 0x48, 0x51); // Set Experience
        remapInbound(ConnectionProtocol.PLAY, 0x49, 0x52); // Update Health
        remapInbound(ConnectionProtocol.PLAY, 0x4A, 0x53); // Scoreboard Objective
        remapInbound(ConnectionProtocol.PLAY, 0x4B, 0x54); // Set Passengers
        remapInbound(ConnectionProtocol.PLAY, 0x4C, 0x55); // Teams
        remapInbound(ConnectionProtocol.PLAY, 0x4D, 0x56); // Update Score
        // + 0x57: Set Title SubTitle
        remapInbound(ConnectionProtocol.PLAY, 0x4E, 0x58); // Time Update
        // + 0x59: Set Title Text
        // + 0x5A: Set Title Time
        remapInbound(ConnectionProtocol.PLAY, 0x50, 0x5B); // Entity Sound Effect
        remapInbound(ConnectionProtocol.PLAY, 0x51, 0x5C); // Sound Effect
        remapInbound(ConnectionProtocol.PLAY, 0x52, 0x5D); // Stop Sound
        remapInbound(ConnectionProtocol.PLAY, 0x53, 0x5E); // Player List Header And Footer
        remapInbound(ConnectionProtocol.PLAY, 0x54, 0x5F); // NBT Query Response
        remapInbound(ConnectionProtocol.PLAY, 0x55, 0x60); // Collect Item
        remapInbound(ConnectionProtocol.PLAY, 0x56, 0x61); // Entity Teleport
        remapInbound(ConnectionProtocol.PLAY, 0x57, 0x62); // Advancements
        remapInbound(ConnectionProtocol.PLAY, 0x58, 0x63); // Entity Properties
        remapInbound(ConnectionProtocol.PLAY, 0x59, 0x64); // Entity Effect
        remapInbound(ConnectionProtocol.PLAY, 0x5A, 0x65); // Declare Recipes
        remapInbound(ConnectionProtocol.PLAY, 0x5B, 0x66); // Tags
        // ------------------------------
        rewriteInbound(ConnectionProtocol.PLAY, 0x1C, wrapper -> { // Explosion
            wrapper.passthrough(PacketWrapper.Type.FLOAT); // X
            wrapper.passthrough(PacketWrapper.Type.FLOAT); // Y
            wrapper.passthrough(PacketWrapper.Type.FLOAT); // Z
            wrapper.passthrough(PacketWrapper.Type.FLOAT); // Strength
            int length = wrapper.readInt(); // Record Count
            wrapper.writeVarInt(length);
            for (int i = 0; i < length; i++) { // Records
                wrapper.passthrough(PacketWrapper.Type.BYTE); // Record X
                wrapper.passthrough(PacketWrapper.Type.BYTE); // Record Y
                wrapper.passthrough(PacketWrapper.Type.BYTE); // Record Z
            }
            wrapper.passthrough(PacketWrapper.Type.FLOAT); // Player Motion X
            wrapper.passthrough(PacketWrapper.Type.FLOAT); // Player Motion Y
            wrapper.passthrough(PacketWrapper.Type.FLOAT); // Player Motion Z
        });
        rewriteInbound(ConnectionProtocol.PLAY, 0x22, wrapper -> { // Chunk Data
            int chunkX = wrapper.passthroughInt(); // Chunk X
            int chunkZ = wrapper.passthroughInt(); // Chunk Z
            boolean fullChunk = wrapper.readBoolean();
            BitSet bitSet = BitSet.valueOf(new long[]{wrapper.readVarInt()});
            wrapper.writeBitSet(bitSet);
            wrapper.passthrough(PacketWrapper.Type.NBT); // Heightmaps
            if (fullChunk) {
                biomeData.put(IntPair.of(chunkX, chunkZ), wrapper.readVarIntArray());
            } else {
                int[] biomes = biomeData.get(IntPair.of(chunkX, chunkZ));
                if (biomes != null) {
                    wrapper.writeVarIntArray(biomes);
                } else {
                    LOGGER.warn("Biome data not found for chunk at {}, {}", chunkX, chunkZ);
                    wrapper.writeVarIntArray(EMPTY_BIOME_DATA);
                }
            }
            wrapper.passthrough(PacketWrapper.Type.BYTE_ARRAY); // Data
            int length = wrapper.passthroughVarInt(); // Number of block entities
            for (int i = 0; i < length; i++) { // Block entities
                wrapper.passthrough(PacketWrapper.Type.NBT); // Block entity
            }
        });
        rewriteInbound(ConnectionProtocol.PLAY, 0x25, wrapper -> { // Update Light
            wrapper.passthrough(PacketWrapper.Type.VAR_INT); // Chunk X
            wrapper.passthrough(PacketWrapper.Type.VAR_INT); // Chunk Z
            wrapper.passthrough(PacketWrapper.Type.BOOLEAN); // Trust Edges
            var skyYMask = BitSet.valueOf(new long[]{wrapper.readVarInt()}); // Skylight Mask
            wrapper.writeBitSet(skyYMask); // Skylight Mask
            var blockYMask = BitSet.valueOf(new long[]{wrapper.readVarInt()}); // Block Light Mask
            wrapper.writeBitSet(blockYMask); // Block Light Mask
            wrapper.writeBitSet(BitSet.valueOf(new long[]{wrapper.readVarInt()})); // Empty Skylight Mask
            wrapper.writeBitSet(BitSet.valueOf(new long[]{wrapper.readVarInt()})); // Empty Block Light Mask
            int skyUpdates = 0;
            for (int i = 0; i < 18; i++) if (skyYMask.get(i)) skyUpdates++;
            int blockUpdates = 0;
            for (int i = 0; i < 18; i++) if (blockYMask.get(i)) blockUpdates++;
            wrapper.writeVarInt(skyUpdates); // Sky Light array count
            for (int i = 0; i < skyUpdates; i++) {
                wrapper.passthroughByteArray(2048); // Sky Light arrays
            }
            wrapper.writeVarInt(blockUpdates); // Block Light array count
            for (int i = 0; i < blockUpdates; i++) {
                wrapper.passthroughByteArray(2048); // Block Light arrays
            }
        });
        rewriteInbound(ConnectionProtocol.PLAY, 0x26, wrapper -> { // Login
            wrapper.passthrough(PacketWrapper.Type.INT); // Entity ID
            wrapper.passthrough(PacketWrapper.Type.BOOLEAN); // Is hardcore
            wrapper.passthrough(PacketWrapper.Type.UNSIGNED_BYTE); // Game Mode
            wrapper.passthrough(PacketWrapper.Type.BYTE); // Previous Game Mode
            wrapper.passthroughCollection(PacketWrapper.Type.RESOURCE_LOCATION); // World count / World names
            var dimensionCodecTag = wrapper.readNbt(); // Dimension Codec
            rewriteDimensionCodec(dimensionCodecTag);
            wrapper.writeNbt(dimensionCodecTag); // Dimension Codec
            var dimensionTag = wrapper.readNbt(); // Dimension
            rewriteDimensionType(dimensionTag);
            wrapper.writeNbt(dimensionTag); // Dimension
            wrapper.passthroughAll();
        });
        rewriteInbound(ConnectionProtocol.PLAY, 0x27, wrapper -> { // Map Data
            wrapper.passthrough(PacketWrapper.Type.VAR_INT); // Map ID
            wrapper.passthrough(PacketWrapper.Type.BYTE); // Scale
            boolean trackingPosition = wrapper.readBoolean(); // Tracking Position
            wrapper.passthrough(PacketWrapper.Type.BOOLEAN); // Locked
            wrapper.writeBoolean(trackingPosition); // Tracking Position
            if (trackingPosition) {
                wrapper.passthroughAll();
                return;
            }
            int count = wrapper.readVarInt(); // Icon Count
            for (int i = 0; i < count; i++) {
                wrapper.readVarInt(); // Type
                wrapper.readByte(); // X
                wrapper.readByte(); // Z
                wrapper.readByte(); // Direction
                if (wrapper.readBoolean()) { // Has display name
                    wrapper.readComponent(); // Display name
                }
            }
            if (wrapper.passthroughUnsignedByte() > 0) { // Columns
                wrapper.passthrough(PacketWrapper.Type.BYTE); // Rows
                wrapper.passthrough(PacketWrapper.Type.BYTE); // X
                wrapper.passthrough(PacketWrapper.Type.BYTE); // Z
                wrapper.passthrough(PacketWrapper.Type.BYTE_ARRAY); // Length / Data
            }
        });
    }

    @Override
    public void registerOutbound() {
        // - 0x07: Window Confirmation
        remapOutbound(ConnectionProtocol.PLAY, 0x08, 0x07); // Click Window Button
        remapOutbound(ConnectionProtocol.PLAY, 0x09, 0x08); // Click Window
        remapOutbound(ConnectionProtocol.PLAY, 0x0A, 0x09); // Close Window
        remapOutbound(ConnectionProtocol.PLAY, 0x0B, 0x0A); // Plugin Message
        remapOutbound(ConnectionProtocol.PLAY, 0x0C, 0x0B); // Edit Book
        remapOutbound(ConnectionProtocol.PLAY, 0x0D, 0x0C); // Query Entity NBT
        remapOutbound(ConnectionProtocol.PLAY, 0x0E, 0x0D); // Interact Entity
        remapOutbound(ConnectionProtocol.PLAY, 0x0F, 0x0E); // Generate Structure
        remapOutbound(ConnectionProtocol.PLAY, 0x10, 0x0F); // Keep Alive
        remapOutbound(ConnectionProtocol.PLAY, 0x11, 0x10); // Lock Difficulty
        remapOutbound(ConnectionProtocol.PLAY, 0x12, 0x11); // Player Position
        remapOutbound(ConnectionProtocol.PLAY, 0x13, 0x12); // Player Position And Rotation
        remapOutbound(ConnectionProtocol.PLAY, 0x14, 0x13); // Player Rotation
        remapOutbound(ConnectionProtocol.PLAY, 0x15, 0x14); // Player Movement
        remapOutbound(ConnectionProtocol.PLAY, 0x16, 0x15); // Vehicle Move
        remapOutbound(ConnectionProtocol.PLAY, 0x17, 0x16); // Steer Boat
        remapOutbound(ConnectionProtocol.PLAY, 0x18, 0x17); // Pick Item
        remapOutbound(ConnectionProtocol.PLAY, 0x19, 0x18); // Craft Recipe Request
        remapOutbound(ConnectionProtocol.PLAY, 0x1A, 0x19); // Player Abilities
        remapOutbound(ConnectionProtocol.PLAY, 0x1B, 0x1A); // Player Digging
        remapOutbound(ConnectionProtocol.PLAY, 0x1C, 0x1B); // Entity Action
        remapOutbound(ConnectionProtocol.PLAY, 0x1D, 0x1C); // Steer Vehicle
        // + 0x1D: Pong
        // ------------------------------
        rewriteOutbound(ConnectionProtocol.PLAY, 0x1D, PacketWrapper::cancel); // Pong
    }

    @Override
    protected int remapParticleId(int particleId) {
        return super.remapParticleId(particleId);
    }

    @Override
    protected void registerParticleRewriter() {
        super.registerParticleRewriter(0x24);
    }

    private void rewriteDimensionCodec(CompoundTag tag) {
        if (tag != null) {
            rewriteDimensionTypeRegistry(tag.getCompound("dimension_type"));
        }
    }

    private void rewriteDimensionTypeRegistry(CompoundTag tag) {
        if (tag != null && "minecraft:dimension_type".equals(tag.getString("type"))) {
            rewriteDimensionTypeRegistryEntry(tag.getCompound("value"));
        }
    }

    private void rewriteDimensionTypeRegistryEntry(CompoundTag tag) {
        if (tag != null) {
            rewriteDimensionType(tag.getCompound("element"));
        }
    }

    private void rewriteDimensionType(CompoundTag tag) {
        if (tag != null) {
            tag.put("min_y", IntTag.valueOf(0));
            tag.put("height", IntTag.valueOf(256));
        }
    }

    private final Object2ObjectMap<IntPair, int[]> biomeData = new Object2ObjectOpenHashMap<>();
}
