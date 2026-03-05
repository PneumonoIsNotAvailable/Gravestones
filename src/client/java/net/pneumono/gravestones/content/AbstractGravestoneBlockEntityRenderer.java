package net.pneumono.gravestones.content;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.pneumono.gravestones.block.AbstractGravestoneBlockEntity;
import com.mojang.math.Axis;
import java.util.List;
import java.util.function.Function;

//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderType;
//?} else if >=1.21.9 {
/*import net.minecraft.client.renderer.RenderType;
*///?}

//? if >=1.21.11 {
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.util.Util;
//?} else {
/*import net.minecraft.client.model.SkullModelBase;
import net.minecraft.Util;
*///?}

//? if >=1.21.9 {
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
*///?}

//? if >=1.21.5 {
import net.minecraft.world.phys.Vec3;
//?}

//? if >=1.21.4 {
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
//?} else {
/*import net.minecraft.client.renderer.blockentity.SignRenderer;
*///?}

//? if >=1.21.9 {
public abstract class AbstractGravestoneBlockEntityRenderer<T extends AbstractGravestoneBlockEntity, U extends AbstractGravestoneBlockEntityRenderer.RenderState> implements BlockEntityRenderer<T, U> {
//?} else {
/*public abstract class AbstractGravestoneBlockEntityRenderer<T extends AbstractGravestoneBlockEntity> implements BlockEntityRenderer<T> {
*///?}
    protected static final float TEXT_SCALE = 1f / 7f;
    protected static final int TEXT_LINE_HEIGHT = 7;
    protected final Font font;
    protected final Function<SkullBlock.Type, SkullModelBase> models;

    //? if >=1.21.9 {
    protected final PlayerSkinRenderCache skinCache;
    //?}

    public AbstractGravestoneBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        //? if >=1.21.9 {
        this.font = ctx.font();
        //?} else {
        /*this.font = ctx.getFont();
        *///?}

        //? if >=1.21.9 {
        this.models = Util.memoize(type -> SkullBlockRenderer.createModel(ctx.entityModelSet(), type));
        //?} else if >=1.21.4 {
        /*this.models = Util.memoize(type -> SkullBlockRenderer.createModel(ctx.getModelSet(), type));
        *///?} else {
        /*this.models = Util.memoize(type -> SkullBlockRenderer.createSkullRenderers(ctx.getModelSet()).get(type));
        *///?}

        //? if >=1.21.9 {
        this.skinCache = ctx.playerSkinRenderCache();
        //?}
    }

    @Override
    //? if >=1.21.9 {
    public void submit(U state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState cameraState) {
    //?} else {
    /*public void render(T entity, float tickProgress, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay/^? if >=1.21.5 {^/, Vec3 cameraPos/^?}^/) {
    *///?}
        poseStack.pushPose();

        poseStack.translate(0.5F, 0.0F, 0.5F);
        int rotation = switch (/*? if >=1.21.9 {*/state.direction/*?} else {*//*entity.getGravestoneDirection()*//*?}*/) {
            case DOWN, UP, NORTH -> 0;
            case WEST -> 90;
            case SOUTH -> 180;
            case EAST -> 270;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-0.5F, 0.0F, -0.5F);

        //? if >=1.21.9 {
        renderText(state, poseStack, queue);
        renderPositionedHead(state, poseStack, queue);
        //?} else {
        /*renderText(entity, poseStack, bufferSource, light);
        renderPositionedHead(entity, poseStack, bufferSource, light);
        *///?}

        poseStack.popPose();
    }

    //? if >=1.21.9 {
    public void renderText(U info, PoseStack poseStack, SubmitNodeCollector queue) {
    //?} else {
    /*public void renderText(T info, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
    *///?}
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.scale(0.0625F, -0.0625F, 0.0625F);
        poseStack.translate(-8, -14, -11.95);

        poseStack.scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);

        SignText signText = getSignText(info);
        int textColor = /*? if >=1.21.4 {*/AbstractSignRenderer.getDarkColor(signText)/*?} else {*//*SignRenderer.getDarkColor(signText)*//*?}*/;
        FormattedCharSequence[] orderedTexts = signText.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), (text) -> {
            List<FormattedCharSequence> list = this.font.split(text, 120);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
        });
        int color;
        boolean renderOutline;
        int finalLight;
        if (signText.hasGlowingText()) {
            color = signText.getColor().getTextColor();
            renderOutline = isOutlineVisible(
                    info./*? if >=1.21.9 {*/getPos/*?} else {*//*getBlockPos*//*?}*/(),
                    color
            );
            finalLight = 15728880;
        } else {
            color = textColor;
            renderOutline = false;
            finalLight = /*? if >=1.21.9 {*/info.lightCoords/*?} else {*//*light*//*?}*/;
        }

        for(int i = 0; i < 4; ++i) {
            FormattedCharSequence orderedText = orderedTexts[i];
            float x = (float)(-this.font.width(orderedText) / 2);
            int y = (i + 1) * TEXT_LINE_HEIGHT * 2;
            //? if >=1.21.9 {
            queue.submitText(
                    poseStack,
                    x, y,
                    orderedText,
                    false,
                    Font.DisplayMode.POLYGON_OFFSET,
                    finalLight,
                    color,
                    0,
                    renderOutline ? textColor : 0
            );
            //?} else {
            /*if (renderOutline) {
                this.font.drawInBatch8xOutline(
                        orderedText,
                        x, y,
                        color,
                        textColor,
                        poseStack.last().pose(),
                        bufferSource,
                        finalLight
                );
            } else {
                this.font.drawInBatch(
                        orderedText,
                        x, y,
                        color,
                        false,
                        poseStack.last().pose(),
                        bufferSource,
                        Font.DisplayMode.POLYGON_OFFSET,
                        0,
                        finalLight
                );
            }
            *///?}
        }

        poseStack.popPose();
    }

    private static boolean isOutlineVisible(BlockPos pos, int color) {
        //? if >=1.21.9 {
        return color == DyeColor.BLACK.getTextColor() || AbstractSignRenderer.isOutlineVisible(pos);
        //?} else if >=1.21.4 {
        /*return AbstractSignRenderer.isOutlineVisible(pos, color);
        *///?} else {
        /*return SignRenderer.isOutlineVisible(pos, color);
        *///?}
    }

    //? if >=1.21.9 {
    public void renderPositionedHead(U info, PoseStack poseStack, SubmitNodeCollector queue) {
    //?} else {
    /*public void renderPositionedHead(T info, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
     *///?}
        poseStack.pushPose();

        poseStack.translate(0F, -0.0625F, 0F);

        BlockPos pos = info./*? if >=1.21.9 {*/getPos/*?} else {*//*getBlockPos*//*?}*/();
        // Slightly overcomplicated, but whatever
        long seed = (
                "Pos: " + pos.toString() +
                ", Some other text, I don't know, it doesn't matter what goes here."
        ).hashCode();
        RandomSource random = new LegacyRandomSource(seed);

        float yaw = random.nextFloat() * 70F - 35F;
        float pitch = random.nextFloat() * -30F;

        poseStack.translate(0.5F, 0.0F, 0.475F);

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        //? if >=1.21.9 {
        renderHead(info, poseStack, queue, yaw, pitch);
        //?} else {
        /*renderHead(info, poseStack, bufferSource, yaw, pitch, light);
        *///?}

        poseStack.popPose();
    }

    //? if >=1.21.9 {
    public abstract void renderHead(U info, PoseStack poseStack, SubmitNodeCollector queue, float yaw, float pitch);
    //?} else {
    /*public abstract void renderHead(T info, PoseStack poseStack, MultiBufferSource bufferSource, float yaw, float pitch, int light);
    *///?}

    //? if >=1.21.9 {
    public void renderHeadModel(U info, PoseStack poseStack, SkullModelBase model, SubmitNodeCollector queue, RenderType renderLayer, float yaw, float pitch) {
        SkullModelBase.State skullState = new SkullModelBase.State();
        skullState.yRot = yaw;
        skullState.xRot = pitch;
        skullState.animationPos = 0.0F;
        model.setupAnim(skullState);
        queue.submitModel(model, skullState, poseStack, renderLayer, info.lightCoords, OverlayTexture.NO_OVERLAY, 0, info.breakProgress);
    }
    //?} else {
    /*public void renderHeadModel(PoseStack poseStack, SkullModelBase model, VertexConsumer vertexConsumer, float yaw, float pitch, int light) {
        model.setupAnim(0.0F, yaw, pitch);
        //? if >=1.21 {
        model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
        //?} else {
        /^model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        ^///?}
    }
    *///?}

    public abstract SignText getSignText(T entity);

    //? if >=1.21.9 {
    public SignText getSignText(U state) {
        return state.signText;
    }

    @Override
    public void extractRenderState(T entity, U state, float tickProgress, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(entity, state, tickProgress, cameraPos, crumblingOverlay);
        state.direction = entity.getGravestoneDirection();
        state.signText = getSignText(entity);
    }

    public static class RenderState extends BlockEntityRenderState {
        public SignText signText;
        public Direction direction;

        public BlockPos getPos() {
            return blockPos;
        }
    }
    //?}
}
