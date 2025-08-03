package net.pneumono.gravestones;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.block.AestheticGravestoneBlockEntity;
import net.pneumono.gravestones.content.AestheticGravestoneBlockEntityRenderer;
import net.pneumono.gravestones.content.AestheticGravestoneEditScreen;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.content.TechnicalGravestoneBlockEntityRenderer;
import net.pneumono.gravestones.networking.GravestoneEditorOpenS2CPayload;

public class GravestonesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(GravestonesRegistry.GRAVESTONE_SKELETON_ENTITY_TYPE, SkeletonEntityRenderer::new);
        BlockEntityRendererFactories.register(GravestonesRegistry.TECHNICAL_GRAVESTONE_ENTITY, TechnicalGravestoneBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(GravestonesRegistry.AESTHETIC_GRAVESTONE_ENTITY, AestheticGravestoneBlockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(GravestoneEditorOpenS2CPayload.ID, (client, handler, buf, sender) -> {
            ClientWorld world = client.world;
            if (world == null) {
                return;
            }

            BlockPos pos = buf.readBlockPos();
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof AestheticGravestoneBlockEntity gravestone) {
                client.execute(() -> client.setScreen(new AestheticGravestoneEditScreen(gravestone, client.shouldFilterText())));
            }
        });
    }
}