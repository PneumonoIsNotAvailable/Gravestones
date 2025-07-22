package net.pneumono.gravestones.content;

import net.minecraft.block.SkullBlock;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.GravestoneTime;
import net.pneumono.gravestones.gravestones.enums.TimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TechnicalGravestoneBlockEntityRenderer extends AbstractGravestoneBlockEntityRenderer<TechnicalGravestoneBlockEntity> {
    public TechnicalGravestoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void renderGravestoneText(TechnicalGravestoneBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        for (int i = 0; i < 4; ++i) {
            matrices.translate(0, 2 * (1 / TEXT_SCALE), 0);

            Text message = Text.literal(switch (i) {
                case 0 -> {
                    ProfileComponent profileComponent = entity.getGraveOwner();
                    if (profileComponent != null) {
                        yield profileComponent.name().orElse("???");
                    }
                    yield "???";
                }
                case 1 -> getGravestoneTimeLines(entity,true);
                case 2 -> getGravestoneTimeLines(entity,false);
                default -> "";
            });

            this.textRenderer.draw(
                    message,
                    (float) (-this.textRenderer.getWidth(message) / 2),
                    0.0F,
                    Colors.BLACK,
                    false,
                    matrices.peek().getPositionMatrix(),
                    vertexConsumers,
                    TextRenderer.TextLayerType.POLYGON_OFFSET,
                    0,
                    light
            );
        }
    }

    private String getGravestoneTimeLines(TechnicalGravestoneBlockEntity entity, boolean line) {
        String spawnDateTime = entity.getSpawnDateTime();
        String text = "";
        if (spawnDateTime != null) {
            try {
                SimpleDateFormat fromServer = GravestoneTime.READABLE;
                TimeFormat type = GravestonesConfig.TIME_FORMAT.getValue();

                SimpleDateFormat toClient = new SimpleDateFormat(line ? type.getDateFormat() : type.getTimeFormat());
                text = toClient.format(fromServer.parse(spawnDateTime));
            } catch (ParseException ignored) {}
        }
        return text;
    }

    @Override
    public void renderHead(TechnicalGravestoneBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (GravestonesConfig.SHOW_HEADS.getValue()) {
            super.renderHead(entity, matrices, vertexConsumers, light);
        }
    }

    @Override
    public void renderGravestoneHead(TechnicalGravestoneBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float yaw, float pitch, int light) {
        if (!GravestonesConfig.SHOW_HEADS.getValue()) return;

        SkullBlockEntityModel model = this.models.apply(SkullBlock.Type.PLAYER);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                SkullBlockEntityRenderer.getRenderLayer(SkullBlock.Type.PLAYER, entity.getGraveOwner())
        );

        model.setHeadRotation(0.0F, yaw, pitch);
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
    }
}
