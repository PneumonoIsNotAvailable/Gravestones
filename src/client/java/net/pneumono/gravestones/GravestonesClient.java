package net.pneumono.gravestones;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.pneumono.gravestones.content.TechnicalGravestoneBlockEntityRenderer;
import net.pneumono.gravestones.content.GravestonesRegistry;

public class GravestonesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(GravestonesRegistry.GRAVESTONE_SKELETON_ENTITY_TYPE, SkeletonEntityRenderer::new);
        BlockEntityRendererFactories.register(GravestonesRegistry.TECHNICAL_GRAVESTONE_ENTITY, TechnicalGravestoneBlockEntityRenderer::new);
    }
}