package net.minecraftforge.fml.client.registry;

import net.blueberrymc.registry.BlueberryRegistries;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ClientRegistry {
    public static synchronized <T extends BlockEntity> void bindTileEntityRenderer(
            @NotNull BlockEntityType<T> blockEntityType,
            @NotNull Function<? super BlockEntityRenderDispatcher, ? extends BlockEntityRenderer<? super T>> rendererFactory
    ) {
        BlueberryRegistries.bindTileEntityRenderer(blockEntityType, rendererFactory);
    }
}
