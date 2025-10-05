package net.pneumono.gravestones.content;

import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.Random;
import net.pneumono.gravestones.block.AbstractGravestoneBlockEntity;

import java.util.List;
import java.util.function.Function;

//? if >=1.21.9 {
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
//?} else {
/*import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
*///?}

//? if >=1.21.5 {
import net.minecraft.util.math.Vec3d;
//?}

//? if >=1.21.4 {
import net.minecraft.client.render.block.entity.AbstractSignBlockEntityRenderer;
//?} else {
/*import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
*///?}

//? if >=1.21.9 {
public abstract class AbstractGravestoneBlockEntityRenderer<T extends AbstractGravestoneBlockEntity, U extends AbstractGravestoneBlockEntityRenderer.RenderState> implements BlockEntityRenderer<T, U> {
//?} else {
/*public abstract class AbstractGravestoneBlockEntityRenderer<T extends AbstractGravestoneBlockEntity> implements BlockEntityRenderer<T> {
*///?}
    protected static final float TEXT_SCALE = 1f / 7f;
    protected static final int TEXT_LINE_HEIGHT = 7;
    protected final TextRenderer textRenderer;
    protected final Function<SkullBlock.SkullType, SkullBlockEntityModel> models;

    //? if >=1.21.9 {
    protected final PlayerSkinCache skinCache;
    //?}

    public AbstractGravestoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        //? if >=1.21.9 {
        this.textRenderer = ctx.textRenderer();
        //?} else {
        /*this.textRenderer = ctx.getTextRenderer();
        *///?}

        //? if >=1.21.9 {
        this.models = Util.memoize(type -> SkullBlockEntityRenderer.getModels(ctx.loadedEntityModels(), type));
        //?} else if >=1.21.4 {
        /*this.models = Util.memoize(type -> SkullBlockEntityRenderer.getModels(ctx.getLoadedEntityModels(), type));
        *///?} else {
        /*this.models = Util.memoize(type -> SkullBlockEntityRenderer.getModels(ctx.getLayerRenderDispatcher()).get(type));
        *///?}

        //? if >=1.21.9 {
        this.skinCache = ctx.playerSkinRenderCache();
        //?}
    }

    @Override
    //? if >=1.21.9 {
    public void render(U state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
    //?} else {
    /*public void render(T entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay/^? if >=1.21.5 {^/, Vec3d cameraPos/^?}^/) {
    *///?}
        matrices.push();

        matrices.translate(0.5F, 0.0F, 0.5F);
        int rotation = switch (/*? if >=1.21.9 {*/state.direction/*?} else {*//*entity.getGravestoneDirection()*//*?}*/) {
            case DOWN, UP, NORTH -> 0;
            case WEST -> 90;
            case SOUTH -> 180;
            case EAST -> 270;
        };
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
        matrices.translate(-0.5F, 0.0F, -0.5F);

        //? if >=1.21.9 {
        renderText(state, matrices, queue);
        renderPositionedHead(state, matrices, queue);
        //?} else {
        /*renderText(entity, matrices, vertexConsumers, light);
        renderPositionedHead(entity, matrices, vertexConsumers, light);
        *///?}

        matrices.pop();
    }

    //? if >=1.21.9 {
    public void renderText(U info, MatrixStack matrices, OrderedRenderCommandQueue queue) {
    //?} else {
    /*public void renderText(T info, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
    *///?}
        matrices.push();

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.scale(0.0625F, -0.0625F, 0.0625F);
        matrices.translate(-8, -14, -11.95);

        matrices.scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);

        SignText signText = getSignText(info);
        int textColor = /*? if >=1.21.4 {*/AbstractSignBlockEntityRenderer.getTextColor(signText)/*?} else {*//*SignBlockEntityRenderer.getColor(signText)*//*?}*/;
        OrderedText[] orderedTexts = signText.getOrderedMessages(MinecraftClient.getInstance().shouldFilterText(), (text) -> {
            List<OrderedText> list = this.textRenderer.wrapLines(text, 120);
            return list.isEmpty() ? OrderedText.EMPTY : list.getFirst();
        });
        int color;
        boolean renderOutline;
        int finalLight;
        if (signText.isGlowing()) {
            color = signText.getColor().getSignColor();
            renderOutline = shouldRenderTextOutline(info.getPos(), color);
            finalLight = 15728880;
        } else {
            color = textColor;
            renderOutline = false;
            finalLight = /*? if >=1.21.9 {*/info.lightmapCoordinates/*?} else {*//*light*//*?}*/;
        }

        for(int i = 0; i < 4; ++i) {
            OrderedText orderedText = orderedTexts[i];
            float x = (float)(-this.textRenderer.getWidth(orderedText) / 2);
            int y = (i + 1) * TEXT_LINE_HEIGHT * 2;
            //? if >=1.21.9 {
            queue.submitText(
                    matrices,
                    x, y,
                    orderedText,
                    false,
                    TextRenderer.TextLayerType.POLYGON_OFFSET,
                    finalLight,
                    color,
                    0,
                    renderOutline ? textColor : 0
            );
            //?} else {
            /*if (renderOutline) {
                this.textRenderer.drawWithOutline(
                        orderedText,
                        x, y,
                        color,
                        textColor,
                        matrices.peek().getPositionMatrix(),
                        vertexConsumers,
                        finalLight
                );
            } else {
                this.textRenderer.draw(
                        orderedText,
                        x, y,
                        color,
                        false,
                        matrices.peek().getPositionMatrix(),
                        vertexConsumers,
                        TextRenderer.TextLayerType.POLYGON_OFFSET,
                        0,
                        finalLight
                );
            }
            *///?}
        }

        matrices.pop();
    }

    private static boolean shouldRenderTextOutline(BlockPos pos, int color) {
        //? if >=1.21.9 {
        return color == DyeColor.BLACK.getSignColor() || AbstractSignBlockEntityRenderer.shouldRenderTextOutline(pos);
        //?} else if >=1.21.4 {
        /*return AbstractSignBlockEntityRenderer.shouldRenderTextOutline(pos, color);
        *///?} else {
        /*return SignBlockEntityRenderer.shouldRender(pos, color);
        *///?}
    }

    //? if >=1.21.9 {
    public void renderPositionedHead(U info, MatrixStack matrices, OrderedRenderCommandQueue queue) {
    //?} else {
    /*public void renderPositionedHead(T info, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
     *///?}
        matrices.push();

        matrices.translate(0F, -0.0625F, 0F);

        BlockPos pos = info.getPos();
        // Slightly overcomplicated, but whatever
        long seed = (
                "Pos: " + pos.toString() +
                ", Some other text, I don't know, it doesn't matter what goes here."
        ).hashCode();
        Random random = new CheckedRandom(seed);

        float yaw = random.nextFloat() * 70F - 35F;
        float pitch = random.nextFloat() * -30F;

        matrices.translate(0.5F, 0.0F, 0.475F);

        matrices.scale(-1.0F, -1.0F, 1.0F);
        //? if >=1.21.9 {
        renderHead(info, matrices, queue, yaw, pitch);
        //?} else {
        /*renderHead(info, matrices, vertexConsumers, yaw, pitch, light);
        *///?}

        matrices.pop();
    }

    //? if >=1.21.9 {
    public abstract void renderHead(U info, MatrixStack matrices, OrderedRenderCommandQueue queue, float yaw, float pitch);
    //?} else {
    /*public abstract void renderHead(T info, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float yaw, float pitch, int light);
    *///?}

    //? if >=1.21.9 {
    public void renderHeadModel(U info, MatrixStack matrices, SkullBlockEntityModel model, OrderedRenderCommandQueue queue, RenderLayer renderLayer, float yaw, float pitch) {
        SkullBlockEntityModel.SkullModelState skullState = new SkullBlockEntityModel.SkullModelState();
        skullState.yaw = yaw;
        skullState.pitch = pitch;
        skullState.poweredTicks = 0.0F;
        model.setAngles(skullState);
        queue.submitModel(model, skullState, matrices, renderLayer, info.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0, info.crumblingOverlay);
    }
    //?} else {
    /*public void renderHeadModel(MatrixStack matrices, SkullBlockEntityModel model, VertexConsumer vertexConsumer, float yaw, float pitch, int light) {
        model.setHeadRotation(0.0F, yaw, pitch);
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
    }
    *///?}

    public abstract SignText getSignText(T entity);

    //? if >=1.21.9 {
    public SignText getSignText(U state) {
        return state.signText;
    }

    @Override
    public void updateRenderState(T entity, U state, float tickProgress, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickProgress, cameraPos, crumblingOverlay);
        state.direction = entity.getGravestoneDirection();
        state.signText = getSignText(entity);
    }

    public static class RenderState extends BlockEntityRenderState {
        public SignText signText;
        public Direction direction;

        public BlockPos getPos() {
            return pos;
        }
    }
    //?}
}
