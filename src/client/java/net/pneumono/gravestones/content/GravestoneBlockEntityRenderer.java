package net.pneumono.gravestones.content;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import net.pneumono.gravestones.content.entity.AbstractGravestoneBlockEntity;

import java.util.Objects;

public class GravestoneBlockEntityRenderer implements BlockEntityRenderer<AbstractGravestoneBlockEntity> {
    private final TextRenderer textRenderer;

    public GravestoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.textRenderer = ctx.getTextRenderer();
    }

    @Override
    public void render(AbstractGravestoneBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        int rotation = switch (entity.getGravestoneDirection()) {
            case DOWN, UP, NORTH -> 0;
            case WEST -> 90;
            case SOUTH -> 180;
            case EAST -> 270;
        };
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.scale(0.0625F, -0.0625F, 0.0625F);
        matrices.translate(-8, -15, -8);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
        matrices.translate(0, 0, -4.95);

        float scale = 1f / 7f;
        matrices.scale(scale, scale, scale);

        for (int i = 0; i < 4; ++i) {
            matrices.translate(0, 2 * (1 / scale), 0);

            String text = entity.getGravestoneTextLine(i);
            if (!Objects.equals(text, "")) {
                drawText(Text.literal(text), matrices, vertexConsumers, light);
            }
        }

        matrices.pop();
    }

    public void drawText(Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        this.textRenderer.draw(text, (float) (-this.textRenderer.getWidth(text) / 2), 0.0F, 0, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);
    }
}
