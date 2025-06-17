package net.pneumono.gravestones.content;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.pneumono.gravestones.block.AbstractGravestoneBlockEntity;

public abstract class AbstractGravestoneBlockEntityRenderer<T extends AbstractGravestoneBlockEntity> implements BlockEntityRenderer<T> {
    protected static final float SCALE = 1f / 7f;
    protected final TextRenderer textRenderer;

    public AbstractGravestoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.textRenderer = ctx.getTextRenderer();
    }

    @Override
    public void render(T entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
        matrices.push();

        int rotation = switch (entity.getGravestoneDirection()) {
            case DOWN, UP, NORTH -> 0;
            case WEST -> 90;
            case SOUTH -> 180;
            case EAST -> 270;
        };
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.scale(0.0625F, -0.0625F, 0.0625F);
        matrices.translate(-8, -14, -8);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
        matrices.translate(0, 0, -3.95);

        matrices.scale(SCALE, SCALE, SCALE);
        renderGravestoneText(entity, matrices, vertexConsumers, light);

        matrices.pop();
    }

    public abstract void renderGravestoneText(T blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);
}
