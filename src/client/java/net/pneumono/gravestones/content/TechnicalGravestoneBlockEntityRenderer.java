package net.pneumono.gravestones.content;

import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.GravestoneTime;
import net.pneumono.gravestones.gravestones.enums.TimeFormat;
import net.pneumono.gravestones.multiversion.GraveOwner;

import java.text.ParseException;
import java.text.SimpleDateFormat;

//? if >=1.21.9 {
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
//?} else {
/*import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
*///?}

//? if >=1.21.9 {
public class TechnicalGravestoneBlockEntityRenderer extends AbstractGravestoneBlockEntityRenderer<TechnicalGravestoneBlockEntity, TechnicalGravestoneBlockEntityRenderer.TechnicalRenderState> {
//?} else {
/*public class TechnicalGravestoneBlockEntityRenderer extends AbstractGravestoneBlockEntityRenderer<TechnicalGravestoneBlockEntity> {
*///?}
    public TechnicalGravestoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public SignText getSignText(TechnicalGravestoneBlockEntity entity) {
        GraveOwner graveOwner = entity.getGraveOwner();
        Text[] messages = new Text[]{
                Text.literal(graveOwner == null ? "???" : graveOwner.getNotNullName()),
                Text.literal(getGravestoneTimeLines(entity,true)),
                Text.literal(getGravestoneTimeLines(entity,false)),
                Text.empty()
        };

        return new SignText(messages, messages, DyeColor.BLACK, false);
    }

    private String getGravestoneTimeLines(TechnicalGravestoneBlockEntity entity, boolean date) {
        String spawnDateTime = entity.getSpawnDateTime();
        String text = "";
        if (spawnDateTime != null) {
            try {
                SimpleDateFormat fromServer = GravestoneTime.READABLE;
                TimeFormat type = GravestonesConfig.TIME_FORMAT.getValue();

                SimpleDateFormat toClient = new SimpleDateFormat(date ? type.getDateFormat() : type.getTimeFormat());
                text = toClient.format(fromServer.parse(spawnDateTime));
            } catch (ParseException ignored) {}
        }
        return text;
    }

    //? if >=1.21.9 {
    @Override
    public void renderPositionedHead(TechnicalRenderState info, MatrixStack matrices, OrderedRenderCommandQueue queue) {
        if (GravestonesConfig.SHOW_HEADS.getValue()) {
            super.renderPositionedHead(info, matrices, queue);
        }
    }
    //?} else {
    /*@Override
    public void renderPositionedHead(TechnicalGravestoneBlockEntity info, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (GravestonesConfig.SHOW_HEADS.getValue()) {
            super.renderPositionedHead(info, matrices, vertexConsumers, light);
        }
    }
    *///?}

    @Override
    //? if >=1.21.9 {
    public void renderHead(TechnicalRenderState info, MatrixStack matrices, OrderedRenderCommandQueue queue, float yaw, float pitch) {
        RenderLayer layer;
        ProfileComponent profileComponent = info.graveOwner.getProfileComponent();
        if (profileComponent != null) {
            layer = this.skinCache.get(profileComponent).getRenderLayer();
        } else {
            layer = SkullBlockEntityRenderer.getCutoutRenderLayer(SkullBlock.Type.PLAYER, null);
        }

        renderHeadModel(
                info,
                matrices,
                this.models.apply(SkullBlock.Type.PLAYER),
                queue,
                layer,
                yaw,
                pitch
        );
    }
    //?} else {
    /*public void renderHead(TechnicalGravestoneBlockEntity info, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float yaw, float pitch, int light) {
        GraveOwner graveOwner = info.getGraveOwner();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                SkullBlockEntityRenderer.getRenderLayer(SkullBlock.Type.PLAYER, graveOwner == null ? null : graveOwner.getProfileComponent())
        );
        renderHeadModel(matrices, this.models.apply(SkullBlock.Type.PLAYER), vertexConsumer, yaw, pitch, light);
    }
    *///?}

    //? if >=1.21.9 {
    @Override
    public TechnicalRenderState createRenderState() {
        return new TechnicalRenderState();
    }

    @Override
    public void updateRenderState(TechnicalGravestoneBlockEntity entity, TechnicalRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(entity, state, tickProgress, cameraPos, crumblingOverlay);
        state.graveOwner = entity.getGraveOwner();
    }

    public static class TechnicalRenderState extends RenderState {
        public GraveOwner graveOwner;
    }
    //?}
}
