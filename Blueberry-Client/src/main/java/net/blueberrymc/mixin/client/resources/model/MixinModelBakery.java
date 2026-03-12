package net.blueberrymc.mixin.client.resources.model;

import net.blueberrymc.client.BlueberryClientImpl;
import net.blueberrymc.client.event.SpecialModelRegistryEvent;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(ModelBakery.class)
public abstract class MixinModelBakery {
    /*
    @Shadow
    public abstract UnbakedModel getModel(Identifier resourceLocation);

    @Shadow
    @Final
    private Map<Identifier, UnbakedModel> unbakedCache;

    @Shadow
    @Final
    private Map<Identifier, UnbakedModel> topLevelModels;

    @Unique
    private void addModelToCache(Identifier resourceLocation) {
        UnbakedModel unbakedModel = this.getModel(resourceLocation);
        this.unbakedCache.put(resourceLocation, unbakedModel);
        this.topLevelModels.put(resourceLocation, unbakedModel);
        unbakedModel.resolveParents(this::getModel);
    }

    @Unique
    public Set<Identifier> getSpecialModels() {
        return BlueberryClientImpl.specialModels;
    }

    @Inject(at = @At(value = "TAIL"), method = "<init>")
    public void addSpecialModels(BlockColors blockColors, ProfilerFiller profilerFiller, Map<Identifier, BlockModel> map, Map<Identifier, List<ModelBakery.LoadedJson>> map2, CallbackInfo ci) {
        new SpecialModelRegistryEvent().callEvent();
        for (Identifier resourceLocation : getSpecialModels()) {
            this.addModelToCache(resourceLocation);
        }
    }
    */
}
