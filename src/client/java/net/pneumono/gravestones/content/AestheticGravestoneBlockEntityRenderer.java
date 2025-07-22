package net.pneumono.gravestones.content;

import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.AbstractSignBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.pneumono.gravestones.block.AestheticGravestoneBlockEntity;

import java.util.List;

public class AestheticGravestoneBlockEntityRenderer extends AbstractGravestoneBlockEntityRenderer<AestheticGravestoneBlockEntity> {
    public AestheticGravestoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    public void renderGravestoneText(AestheticGravestoneBlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int worldLight) {
        SignText signText = blockEntity.getText();

        boolean glowing = signText.isGlowing();
        int textColor = AbstractSignBlockEntityRenderer.getTextColor(signText);
        int color = glowing ? signText.getColor().getSignColor() : textColor;
        boolean renderOutline = glowing && AbstractSignBlockEntityRenderer.shouldRenderTextOutline(blockEntity.getPos(), color);
        int light = glowing ? 15728880 : worldLight;

        for (int i = 0; i < 4; ++i) {
            matrices.translate(0, 2 * (1 / TEXT_SCALE), 0);

            OrderedText[] messages = signText.getOrderedMessages(MinecraftClient.getInstance().shouldFilterText(), text -> {
                List<OrderedText> list = this.textRenderer.wrapLines(text, AestheticGravestoneEditScreen.TEXT_WIDTH);
                return list.isEmpty() ? OrderedText.EMPTY : list.getFirst();
            });
            OrderedText message = messages[i];

            if (renderOutline) {
                this.textRenderer.drawWithOutline(
                        message,
                        (float) (-this.textRenderer.getWidth(message) / 2),
                        0.0F,
                        color,
                        textColor,
                        matrices.peek().getPositionMatrix(),
                        vertexConsumers,
                        light
                );
            } else {
                this.textRenderer.draw(
                        message,
                        (float) (-this.textRenderer.getWidth(message) / 2),
                        0.0F,
                        color,
                        false,
                        matrices.peek().getPositionMatrix(),
                        vertexConsumers,
                        TextRenderer.TextLayerType.POLYGON_OFFSET,
                        0,
                        light
                );
            }
        }
    }
}
