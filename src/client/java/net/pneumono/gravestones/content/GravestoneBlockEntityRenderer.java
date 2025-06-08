package net.pneumono.gravestones.content;

import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.block.AbstractGravestoneBlockEntity;
import net.pneumono.gravestones.block.AestheticGravestoneBlockEntity;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.GravestoneTime;
import net.pneumono.gravestones.gravestones.enums.TimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class GravestoneBlockEntityRenderer implements BlockEntityRenderer<AbstractGravestoneBlockEntity> {
    private static final float SCALE = 1f / 7f;
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
        matrices.translate(-8, -14, -8);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
        matrices.translate(0, 0, -3.95);

        matrices.scale(SCALE, SCALE, SCALE);

        if (entity instanceof TechnicalGravestoneBlockEntity blockEntity) {
            renderTechnicalGravestone(blockEntity, matrices, vertexConsumers, light);
        } else if (entity instanceof AestheticGravestoneBlockEntity blockEntity) {
            renderAestheticGravestone(blockEntity, matrices, vertexConsumers, light);
        }

        matrices.pop();
    }

    public void renderTechnicalGravestone(TechnicalGravestoneBlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        for (int i = 0; i < 4; ++i) {
            matrices.translate(0, 2 * (1 / SCALE), 0);

            Text message = Text.literal(switch (i) {
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

            this.textRenderer.draw(
                    message,
                    (float) (-this.textRenderer.getWidth(message) / 2),
                    0.0F,
                    0,
                    false,
                    matrices.peek().getPositionMatrix(),
                    vertexConsumers,
                    TextRenderer.TextLayerType.POLYGON_OFFSET,
                    0,
                    light
            );
        }
    }

    public void renderAestheticGravestone(AestheticGravestoneBlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int worldLight) {
        SignText signText = blockEntity.getText();

        boolean glowing = signText.isGlowing();
        int textColor = SignBlockEntityRenderer.getColor(signText);
        int color = glowing ? signText.getColor().getSignColor() : textColor;
        boolean renderOutline = glowing && SignBlockEntityRenderer.shouldRender(blockEntity.getPos(), color);
        int light = glowing ? 15728880 : worldLight;

        for (int i = 0; i < 4; ++i) {
            matrices.translate(0, 2 * (1 / SCALE), 0);

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
}
