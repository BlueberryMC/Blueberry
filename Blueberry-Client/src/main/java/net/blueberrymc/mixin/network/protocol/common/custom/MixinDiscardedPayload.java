package net.blueberrymc.mixin.network.protocol.common.custom;

import net.blueberrymc.network.BlueberryCustomPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DiscardedPayload.class, remap = true)
@Implements(@Interface(iface = BlueberryCustomPayload.class, prefix = "blueberry$"))
public abstract class MixinDiscardedPayload implements BlueberryCustomPayload, DiscardedPayloadExtension {
    @Unique
    private byte[] payload;

    @Shadow
    public abstract Identifier shadow$id();

    public Identifier blueberry$id() {
        return shadow$id();
    }

    public byte[] blueberry$payload() {
        return payload;
    }

    @Override
    public void blueberry2$setPayload(byte[] bytes) {
        payload = bytes;
    }

    @Override
    public byte[] blueberry2$getPayload() {
        return payload;
    }

    @Unique
    private static byte[] readRemainingBytes(FriendlyByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    @Inject(at = @At("HEAD"), method = "lambda$codec$0")
    private static void codecEncode(DiscardedPayload discardedPayload, FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
        friendlyByteBuf.writeBytes(((DiscardedPayloadExtension) (Object) discardedPayload).blueberry2$getPayload());
    }

    @Inject(at = @At("RETURN"), method = "lambda$codec$1")
    private static void codecDecode(int i, Identifier resourceLocation, FriendlyByteBuf friendlyByteBuf, CallbackInfoReturnable<DiscardedPayload> cir) {
        ((DiscardedPayloadExtension) (Object) cir.getReturnValue()).blueberry2$setPayload(readRemainingBytes(friendlyByteBuf));
    }
}
