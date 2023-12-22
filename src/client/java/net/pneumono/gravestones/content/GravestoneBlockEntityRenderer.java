package net.pneumono.gravestones.content;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.content.entity.GravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.GravestoneTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class GravestoneBlockEntityRenderer implements BlockEntityRenderer<GravestoneBlockEntity> {
    private final TextRenderer textRenderer;

    public GravestoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.textRenderer = ctx.getTextRenderer();
    }

    @Override
    public void render(GravestoneBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        matrices.scale(0.0625F, -0.0625F, 0.0625F);
        matrices.translate(-8, -13, -12.95);

        // Name
        Text text = Text.literal(entity.getGraveOwner().getName());
        float scale = 10F / this.textRenderer.getWidth(text);
        matrices.scale(scale, scale, scale);

        this.textRenderer.draw(text, (float)(-this.textRenderer.getWidth(text) / 2), 0.0F, 0x000000, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);
        matrices.scale(1/scale, 1/scale, 1/scale);

        // Date
        try {
            SimpleDateFormat fromServer = GravestoneTime.getSimpleDateFormat();
            SimpleDateFormat toClient = new SimpleDateFormat(Gravestones.TIME_FORMAT.getValue());
            text = Text.literal(toClient.format(fromServer.parse(entity.getSpawnDate())));
        } catch (ParseException e) {
            text = Text.literal("");
        }

        if (!Objects.equals(text.getString(), "")) {
            matrices.translate(0, 3, 0);
            scale = 10F / this.textRenderer.getWidth(text);
            matrices.scale(scale, scale, scale);

            this.textRenderer.draw(text, (float) (-this.textRenderer.getWidth(text) / 2), 0.0F, 0x000000, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);
        }

        matrices.pop();
    }
}
