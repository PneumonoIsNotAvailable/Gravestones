package net.pneumono.gravestones.content;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.pneumono.gravestones.block.AestheticGravestoneBlockEntity;

//? if >=1.21.9 {
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
//?} else {
/*import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.jetbrains.annotations.Nullable;
*///?}

//? if >=1.20.6 {
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
//?} else {
/*import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Util;
*///?}

//? if >=1.21.9 {
public class AestheticGravestoneBlockEntityRenderer extends AbstractGravestoneBlockEntityRenderer<AestheticGravestoneBlockEntity, AestheticGravestoneBlockEntityRenderer.AestheticRenderState> {
//?} else {
/*public class AestheticGravestoneBlockEntityRenderer extends AbstractGravestoneBlockEntityRenderer<AestheticGravestoneBlockEntity> {
*///?}
    public AestheticGravestoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public SignText getSignText(AestheticGravestoneBlockEntity entity) {
        return entity.getText();
    }

    @Override
    //? if >=1.21.9 {
    public void renderHead(AestheticRenderState info, MatrixStack matrices, OrderedRenderCommandQueue queue, float yaw, float pitch) {
    //?} else {
    /*public void renderHead(AestheticGravestoneBlockEntity info, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float yaw, float pitch, int light) {
    *///?}
        ItemStack headStack = info.getHeadStack();
        if (headStack.isEmpty()) return;

        if (
                !(headStack.getItem() instanceof BlockItem blockItem) ||
                !(blockItem.getBlock() instanceof AbstractSkullBlock skullBlock)
        ) return;

        SkullBlock.SkullType type = skullBlock.getSkullType();

        //? if >=1.21.9 {
        ProfileComponent profileComponent = headStack.get(DataComponentTypes.PROFILE);
        RenderLayer layer;
        if (profileComponent != null) {
            layer = this.skinCache.get(profileComponent).getRenderLayer();
        } else {
            layer = SkullBlockEntityRenderer.getCutoutRenderLayer(type, null);
        }

        renderHeadModel(
                info,
                matrices,
                this.models.apply(type),
                queue,
                layer,
                yaw,
                pitch
        );
        //?} else {
        /*VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                SkullBlockEntityRenderer.getRenderLayer(type, getStackProfile(headStack))
        );
        renderHeadModel(matrices, this.models.apply(type), vertexConsumer, yaw, pitch, light);
        *///?}
    }

    //? if >=1.20.6 {
    @Nullable
    private static ProfileComponent getStackProfile(ItemStack stack) {
        return stack.get(DataComponentTypes.PROFILE);
    }
    //?} else {
    /*@Nullable
    private static GameProfile getStackProfile(ItemStack stack) {
        GameProfile gameProfile = null;
        NbtCompound nbtCompound = stack.getNbt();
        if (nbtCompound != null && !nbtCompound.isEmpty()) {
            if (nbtCompound.contains("SkullOwner", NbtElement.COMPOUND_TYPE)) {
                gameProfile = NbtHelper.toGameProfile(nbtCompound.getCompound("SkullOwner"));
            } else if (nbtCompound.contains("SkullOwner", NbtElement.STRING_TYPE) && !Util.isBlank(nbtCompound.getString("SkullOwner"))) {
                gameProfile = new GameProfile(Util.NIL_UUID, nbtCompound.getString("SkullOwner"));
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
    public void updateRenderState(AestheticGravestoneBlockEntity entity, AestheticRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(entity, state, tickProgress, cameraPos, crumblingOverlay);
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
