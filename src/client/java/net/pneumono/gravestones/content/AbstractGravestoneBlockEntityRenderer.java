package net.pneumono.gravestones.content;

import net.minecraft.block.SkullBlock;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.Random;
import net.pneumono.gravestones.block.AbstractGravestoneBlockEntity;

import java.util.UUID;

public abstract class AbstractGravestoneBlockEntityRenderer<T extends AbstractGravestoneBlockEntity> implements BlockEntityRenderer<T> {
    protected static final float TEXT_SCALE = 1f / 7f;
    protected final TextRenderer textRenderer;
    protected final SkullBlockEntityModel model;

    public AbstractGravestoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.textRenderer = ctx.getTextRenderer();
        LoadedEntityModels loadedEntityModels = ctx.getLoadedEntityModels();
        this.model = SkullBlockEntityRenderer.getModels(loadedEntityModels, SkullBlock.Type.PLAYER);
    }

    @Override
    public void render(T entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
        matrices.push();

        matrices.translate(0.5F, 0.0F, 0.5F);
        int rotation = switch (entity.getGravestoneDirection()) {
            case DOWN, UP, NORTH -> 0;
            case WEST -> 90;
            case SOUTH -> 180;
            case EAST -> 270;
        };
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
        matrices.translate(-0.5F, 0.0F, -0.5F);

        renderText(entity, matrices, vertexConsumers, light);
        renderHead(entity, matrices, vertexConsumers, light);

        matrices.pop();
    }

    public void renderText(T entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.scale(0.0625F, -0.0625F, 0.0625F);
        matrices.translate(-8, -14, -11.95);

        matrices.scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);
        renderGravestoneText(entity, matrices, vertexConsumers, light);

        matrices.pop();
    }

    public abstract void renderGravestoneText(T blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    public void renderHead(T entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        ProfileComponent profile = entity.getHeadProfile();
        if (profile == null) return;

        matrices.push();

        matrices.translate(0F, -0.0625F, 0F);

        BlockPos pos = entity.getPos();
        // Slightly overcomplicated, but whatever
        long seed = (
                "Pos: " + pos.toString() +
                ", UUID: " + profile.uuid().map(UUID::toString).orElse("???")
        ).hashCode();
        Random random = new CheckedRandom(seed);

        float yaw = random.nextFloat() * 70F - 35F;
        float pitch = random.nextFloat() * -30F;

        matrices.translate(0.5F, 0.0F, 0.475F);

        matrices.scale(-1.0F, -1.0F, 1.0F);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(SkullBlockEntityRenderer.getRenderLayer(SkullBlock.Type.PLAYER, profile));
        this.model.setHeadRotation(0.0F, yaw, pitch);
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }
}
