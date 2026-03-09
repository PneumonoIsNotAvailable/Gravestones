package net.pneumono.gravestones;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.pneumono.gravestones.block.AestheticGravestoneBlockEntity;
import net.pneumono.gravestones.content.AestheticGravestoneBlockEntityRenderer;
import net.pneumono.gravestones.content.AestheticGravestoneEditScreen;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.content.TechnicalGravestoneBlockEntityRenderer;
import net.pneumono.gravestones.networking.GravestoneEditorOpenS2CPayload;

//? if >=1.21.9 {
import net.minecraft.client.renderer.entity.EntityRenderers;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
*///?}

//? if >=1.20.5 {
import net.minecraft.client.Minecraft;
//?}

public class GravestonesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        //? if >=1.21.9 {
        EntityRenderers.register(GravestonesRegistry.GRAVESTONE_SKELETON_ENTITY_TYPE, SkeletonRenderer::new);
        //?} else {
        /*EntityRendererRegistry.register(GravestonesRegistry.GRAVESTONE_SKELETON_ENTITY_TYPE, SkeletonRenderer::new);
        *///?}
        BlockEntityRenderers.register(GravestonesRegistry.TECHNICAL_GRAVESTONE_ENTITY, TechnicalGravestoneBlockEntityRenderer::new);
        BlockEntityRenderers.register(GravestonesRegistry.AESTHETIC_GRAVESTONE_ENTITY, AestheticGravestoneBlockEntityRenderer::new);

        //? if >=1.20.5 {
        ClientPlayNetworking.registerGlobalReceiver(GravestoneEditorOpenS2CPayload.PAYLOAD_ID, (payload, context) -> {
            Minecraft client = context.client();
            ClientLevel level = client.level;
            if (level == null) {
                return;
            }

            BlockPos pos = payload.pos();
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof AestheticGravestoneBlockEntity gravestone) {
                client.setScreen(new AestheticGravestoneEditScreen(gravestone, client.isTextFilteringEnabled()));
            }
        });
        //?} else {
        /*ClientPlayNetworking.registerGlobalReceiver(GravestoneEditorOpenS2CPayload.ID, (client, handler, buf, sender) -> {
            ClientLevel level = client.level;
            if (level == null) {
                return;
            }

            GravestoneEditorOpenS2CPayload payload = GravestoneEditorOpenS2CPayload.read(buf);
            BlockPos pos = payload.pos();
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof AestheticGravestoneBlockEntity gravestone) {
                client.execute(() -> client.setScreen(new AestheticGravestoneEditScreen(gravestone, client.isTextFilteringEnabled())));
            }
        });
        *///?}
    }
}