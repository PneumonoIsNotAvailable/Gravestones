package net.pneumono.gravestones;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.pneumono.gravestones.content.GravestoneBlockEntityRenderer;
import net.pneumono.gravestones.content.GravestonesContent;

public class GravestonesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(GravestonesContent.GRAVESTONE_SKELETON_ENTITY_TYPE, SkeletonEntityRenderer::new);
        BlockEntityRendererFactories.register(GravestonesContent.GRAVESTONE_ENTITY, GravestoneBlockEntityRenderer::new);
    }
}