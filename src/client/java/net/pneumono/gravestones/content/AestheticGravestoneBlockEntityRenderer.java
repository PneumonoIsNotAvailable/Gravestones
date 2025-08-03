package net.pneumono.gravestones.content;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.pneumono.gravestones.block.AestheticGravestoneBlockEntity;

import java.util.List;

public class AestheticGravestoneBlockEntityRenderer extends AbstractGravestoneBlockEntityRenderer<AestheticGravestoneBlockEntity> {
    public AestheticGravestoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    public void renderGravestoneText(AestheticGravestoneBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int worldLight) {
        SignText signText = entity.getText();

        boolean glowing = signText.isGlowing();
        int textColor = SignBlockEntityRenderer.getColor(signText);
        int color = glowing ? signText.getColor().getSignColor() : textColor;
        boolean renderOutline = glowing && SignBlockEntityRenderer.shouldRender(entity.getPos(), color);
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

    @Override
    public void renderGravestoneHead(AestheticGravestoneBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float yaw, float pitch, int light) {
        ItemStack headStack = entity.getHeadStack();
        if (headStack.isEmpty()) return;

        if (
                !(headStack.getItem() instanceof BlockItem blockItem) ||
                !(blockItem.getBlock() instanceof AbstractSkullBlock skullBlock)
        ) return;

        SkullBlock.SkullType type = skullBlock.getSkullType();

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                SkullBlockEntityRenderer.getRenderLayer(type, headStack.get(DataComponentTypes.PROFILE))
        );
        SkullBlockEntityModel model = this.models.apply(type);
        model.setHeadRotation(0.0F, yaw, pitch);
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
    }
}
