package net.pneumono.gravestones.content;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SignText;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.GravestoneTime;
import net.pneumono.gravestones.gravestones.enums.TimeFormat;
import net.pneumono.gravestones.multiversion.GraveOwner;
import com.mojang.blaze3d.vertex.PoseStack;
import java.text.ParseException;
import java.text.SimpleDateFormat;

//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderType;
//?} else if >=1.21.9 {
/*import net.minecraft.client.renderer.RenderType;
*///?}

//? if >=1.21.9 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
//?} else {
/*import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
*///?}

//? if >=1.21.9 {
public class TechnicalGravestoneBlockEntityRenderer extends AbstractGravestoneBlockEntityRenderer<TechnicalGravestoneBlockEntity, TechnicalGravestoneBlockEntityRenderer.TechnicalRenderState> {
//?} else {
/*public class TechnicalGravestoneBlockEntityRenderer extends AbstractGravestoneBlockEntityRenderer<TechnicalGravestoneBlockEntity> {
*///?}
    public TechnicalGravestoneBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public SignText getSignText(TechnicalGravestoneBlockEntity entity) {
        GraveOwner graveOwner = entity.getGraveOwner();
        Component[] messages = new Component[]{
                Component.literal(graveOwner == null ? "???" : graveOwner.getNotNullName()),
                Component.literal(getGravestoneTimeLines(entity,true)),
                Component.literal(getGravestoneTimeLines(entity,false)),
                Component.empty()
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
    public void renderPositionedHead(TechnicalRenderState info, PoseStack poseStack, SubmitNodeCollector queue) {
        if (GravestonesConfig.SHOW_HEADS.getValue()) {
            super.renderPositionedHead(info, poseStack, queue);
        }
    }
    //?} else {
    /*@Override
    public void renderPositionedHead(TechnicalGravestoneBlockEntity info, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        if (GravestonesConfig.SHOW_HEADS.getValue()) {
            super.renderPositionedHead(info, poseStack, bufferSource, light);
        }
    }
    *///?}

    @Override
    //? if >=1.21.9 {
    public void renderHead(TechnicalRenderState info, PoseStack poseStack, SubmitNodeCollector queue, float yaw, float pitch) {
        RenderType layer;
        ResolvableProfile profileComponent = info.graveOwner == null ? null : info.graveOwner.getProfile();
        if (profileComponent != null) {
            layer = this.skinCache.getOrDefault(profileComponent).renderType();
        } else {
            layer = SkullBlockRenderer.getSkullRenderType(SkullBlock.Types.PLAYER, null);
        }

        renderHeadModel(
                info,
                poseStack,
                this.models.apply(SkullBlock.Types.PLAYER),
                queue,
                layer,
                yaw,
                pitch
        );
    }
    //?} else {
    /*public void renderHead(TechnicalGravestoneBlockEntity info, PoseStack poseStack, MultiBufferSource bufferSource, float yaw, float pitch, int light) {
        GraveOwner graveOwner = info.getGraveOwner();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(
                SkullBlockRenderer.getRenderType(SkullBlock.Types.PLAYER, graveOwner == null ? null : graveOwner.getProfile())
        );
        renderHeadModel(poseStack, this.models.apply(SkullBlock.Types.PLAYER), vertexConsumer, yaw, pitch, light);
    }
    *///?}

    //? if >=1.21.9 {
    @Override
    public TechnicalRenderState createRenderState() {
        return new TechnicalRenderState();
    }

    @Override
    public void extractRenderState(TechnicalGravestoneBlockEntity entity, TechnicalRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(entity, state, tickProgress, cameraPos, crumblingOverlay);
        state.graveOwner = entity.getGraveOwner();
    }

    public static class TechnicalRenderState extends RenderState {
        public GraveOwner graveOwner;
    }
    //?}
}
