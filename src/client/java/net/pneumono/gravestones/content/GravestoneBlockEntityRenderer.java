package net.pneumono.gravestones.content;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.content.entity.AbstractGravestoneBlockEntity;
import net.pneumono.gravestones.content.entity.AestheticGravestoneBlockEntity;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.GravestoneTime;
import net.pneumono.gravestones.gravestones.TimeFormatType;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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

            Text text = Text.literal("");
            if (entity instanceof TechnicalGravestoneBlockEntity blockEntity) {
                text = Text.literal(switch (i) {
                    case 0 -> {
                        ProfileComponent profileComponent = blockEntity.getGraveOwner();
                        if (profileComponent != null) {
                            yield profileComponent.name().orElse("???");
                        }
                        yield "???";
                    }
                    case 1 -> getGravestoneTimeLines(blockEntity,true);
                    case 2 -> getGravestoneTimeLines(blockEntity,false);
                    default -> "";
                });
            } else if (entity instanceof AestheticGravestoneBlockEntity blockEntity) {
                text = blockEntity.getText().getMessage(i, false);
            }

            drawText(text, matrices, vertexConsumers, light);
        }

        matrices.pop();
    }

    public void drawText(Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        this.textRenderer.draw(text, (float) (-this.textRenderer.getWidth(text) / 2), 0.0F, 0, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);
    }

    private String getGravestoneTimeLines(TechnicalGravestoneBlockEntity entity, boolean line) {
        String spawnDateTime = entity.getSpawnDateTime();
        String text = "";
        if (spawnDateTime != null) {
            try {
                SimpleDateFormat fromServer = GravestoneTime.getSimpleDateFormat();
                TimeFormatType type = Gravestones.TIME_FORMAT.getValue();

                SimpleDateFormat toClient = new SimpleDateFormat(line ? type.getDateFormat() : type.getTimeFormat());
                text = toClient.format(fromServer.parse(spawnDateTime));
            } catch (ParseException ignored) {}
        }
        return text;
    }
}
