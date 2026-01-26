package net.blueberrymc.mixin.server.packs.repository;

import net.blueberrymc.common.Blueberry;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(BuiltInPackSource.class)
public class MixinBuiltInPackSource {
    @Inject(at = @At("TAIL"), method = "loadPacks")
    public void loadPacks(Consumer<Pack> consumer, CallbackInfo ci) {
        Blueberry.getModLoader().loadPacks(consumer);
    }
}
