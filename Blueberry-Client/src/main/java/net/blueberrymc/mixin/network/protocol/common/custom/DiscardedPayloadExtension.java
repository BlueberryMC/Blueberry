package net.blueberrymc.mixin.network.protocol.common.custom;

interface DiscardedPayloadExtension {
    void blueberry2$setPayload(byte[] bytes);
    byte[] blueberry2$getPayload();
}
