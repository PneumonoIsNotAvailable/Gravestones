package net.pneumono.gravestones.content;

import com.mojang.authlib.GameProfile;
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
import net.pneumono.gravestones.gravestones.TimeFormatType;

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
        GameProfile profile = entity.getGraveOwner();
        Text name;
        float scale = 0.15f;
        matrices.scale(scale, scale, scale);
        if (profile != null) {
            name = Text.literal(profile.getName());
            this.textRenderer.draw(name, (float) (-this.textRenderer.getWidth(name) / 2), 0.0F, 0x000000, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);
        }

        // Date
        String spawnDateTime = entity.getSpawnDateTime();
        Text date = Text.literal("");
        Text time = Text.literal("");
        try {
            if (spawnDateTime != null) {
                SimpleDateFormat fromServer = GravestoneTime.getSimpleDateFormat();
                TimeFormatType type = Gravestones.TIME_FORMAT.getValue();
                SimpleDateFormat toClientDate = new SimpleDateFormat(type.getDateFormat());
                SimpleDateFormat toClientTime = new SimpleDateFormat(type.getTimeFormat());

                date = Text.literal(toClientDate.format(fromServer.parse(spawnDateTime)));
                time = Text.literal(toClientTime.format(fromServer.parse(spawnDateTime)));
            }
        } catch (ParseException ignored) {}

        if (!Objects.equals(date.getString(), "")) {
            matrices.translate(0, 2 * (1 / scale), 0);

            this.textRenderer.draw(date, (float) (-this.textRenderer.getWidth(date) / 2), 0.0F, 0x000000, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);

            matrices.translate(0, 2 * (1 / scale), 0);
            this.textRenderer.draw(time, (float) (-this.textRenderer.getWidth(time) / 2), 0.0F, 0x000000, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);
        }

        matrices.pop();
    }
}
