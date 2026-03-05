package net.pneumono.gravestones.content;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SignText;
import net.pneumono.gravestones.block.AestheticGravestoneBlockEntity;

//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderType;
//?} else if >=1.21.9 {
/*import net.minecraft.client.renderer.RenderType;
*///?}

//? if >=1.21.9 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
//?} else {
/*import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;
*///?}

//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ResolvableProfile;
//?} else {
/*import net.minecraft.Util;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
*///?}

//? if >=1.21.9 {
public class AestheticGravestoneBlockEntityRenderer extends AbstractGravestoneBlockEntityRenderer<AestheticGravestoneBlockEntity, AestheticGravestoneBlockEntityRenderer.AestheticRenderState> {
//?} else {
/*public class AestheticGravestoneBlockEntityRenderer extends AbstractGravestoneBlockEntityRenderer<AestheticGravestoneBlockEntity> {
*///?}
    public AestheticGravestoneBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public SignText getSignText(AestheticGravestoneBlockEntity entity) {
        return entity.getText();
    }

    @Override
    //? if >=1.21.9 {
    public void renderHead(AestheticRenderState info, PoseStack poseStack, SubmitNodeCollector queue, float yaw, float pitch) {
    //?} else {
    /*public void renderHead(AestheticGravestoneBlockEntity info, PoseStack poseStack, MultiBufferSource bufferSource, float yaw, float pitch, int light) {
    *///?}
        ItemStack headStack = info.getHeadStack();
        if (headStack.isEmpty()) return;

        if (
                !(headStack.getItem() instanceof BlockItem blockItem) ||
                !(blockItem.getBlock() instanceof AbstractSkullBlock skullBlock)
        ) return;

        SkullBlock.Type type = skullBlock.getType();

        //? if >=1.21.9 {
        ResolvableProfile profileComponent = headStack.get(DataComponents.PROFILE);
        RenderType layer;
        if (profileComponent != null) {
            layer = this.skinCache.getOrDefault(profileComponent).renderType();
        } else {
            layer = SkullBlockRenderer.getSkullRenderType(type, null);
        }

        renderHeadModel(
                info,
                poseStack,
                this.models.apply(type),
                queue,
                layer,
                yaw,
                pitch
        );
        //?} else {
        /*VertexConsumer vertexConsumer = bufferSource.getBuffer(
                SkullBlockRenderer.getRenderType(type, getStackProfile(headStack))
        );
        renderHeadModel(poseStack, this.models.apply(type), vertexConsumer, yaw, pitch, light);
        *///?}
    }

    //? if >=1.20.5 {
    @Nullable
    private static ResolvableProfile getStackProfile(ItemStack stack) {
        return stack.get(DataComponents.PROFILE);
    }
    //?} else {
    /*@Nullable
    private static GameProfile getStackProfile(ItemStack stack) {
        GameProfile gameProfile = null;
        CompoundTag tag = stack.getTag();
        if (tag != null && !tag.isEmpty()) {
            if (tag.contains("SkullOwner", Tag.TAG_COMPOUND)) {
                gameProfile = NbtUtils.readGameProfile(tag.getCompound("SkullOwner"));
            } else if (tag.contains("SkullOwner", Tag.TAG_STRING) && !Util.isBlank(tag.getString("SkullOwner"))) {
                gameProfile = new GameProfile(Util.NIL_UUID, tag.getString("SkullOwner"));
            }
        }

        return gameProfile;
    }
    *///?}

    //? if >=1.21.9 {
    @Override
    public AestheticRenderState createRenderState() {
        return new AestheticRenderState();
    }

    @Override
    public void extractRenderState(AestheticGravestoneBlockEntity entity, AestheticRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(entity, state, tickProgress, cameraPos, crumblingOverlay);
        state.headStack = entity.getHeadStack();
    }

    public static class AestheticRenderState extends RenderState {
        public ItemStack headStack;

        public ItemStack getHeadStack() {
            return headStack;
        }
    }
    //?}
}
