package net.pneumono.gravestones.block;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.networking.GravestoneEditorOpenS2CPayload;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public class AestheticGravestoneBlock extends AbstractGravestoneBlock {
    public AestheticGravestoneBlock(Settings settings) {
        super(settings);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ActionResult result = ActionResult.PASS;
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isEmpty()) {
            result = onUseWithItem(stack, world, pos, player);
        }
        if (result == ActionResult.PASS) {
            result = onUse(world, pos, player);
        }

        return result;
    }

    protected ActionResult onUseWithItem(ItemStack stack, World world, BlockPos pos, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof AestheticGravestoneBlockEntity gravestone)) {
            return ActionResult.PASS;
        }
        Item item = stack.getItem();

        boolean waxed = gravestone.isWaxed();

        if (!waxed && item instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock && gravestone.getHeadStack().isEmpty()) {
            if (!world.isClient()) {
                world.playSound(null, blockEntity.getPos(), GravestonesRegistry.SOUND_BLOCK_GRAVESTONE_ADD_SKULL, SoundCategory.BLOCKS);
                gravestone.setHeadStack(player, stack);
            }
            return ActionResult.SUCCESS;
        }

        if (!world.isClient()) {
            if (
                    item instanceof SignChangingItem signChangingItem &&
                    player.canModifyBlocks() &&
                    !waxed &&
                    noOtherPlayerEditing(player, gravestone) &&
                    signChangingItem.canUseOnSignText(gravestone.getText(), player) &&
                    tryTextChange(world, item, gravestone)
            ) {
                gravestone.runCommandClickEvent(player, world, pos);
                player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, gravestone.getPos(), GameEvent.Emitter.of(player, gravestone.getCachedState()));
                if (!player.isCreative()) {
                    stack.decrement(1);
                }
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.PASS;
            }
        } else {
            if ((!(item instanceof SignChangingItem) || !player.canModifyBlocks()) && !waxed) {
                return ActionResult.CONSUME;
            } else {
                return ActionResult.SUCCESS;
            }
        }
    }

    private static boolean tryTextChange(World world, Item item, AestheticGravestoneBlockEntity gravestone) {
        // Hardcoding all of these sucks but what else am I going to do
        if (item instanceof DyeItem dyeItem) {
            if (gravestone.changeText(text -> text.withColor(dyeItem.getColor()))) {
                world.playSound(null, gravestone.getPos(), SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            } else {
                return false;
            }
        } else if (item instanceof GlowInkSacItem) {
            if (gravestone.changeText(text -> text.withGlowing(true))) {
                world.playSound(null, gravestone.getPos(), SoundEvents.ITEM_GLOW_INK_SAC_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            } else {
                return false;
            }
        } else if (item instanceof InkSacItem) {
            if (gravestone.changeText(text -> text.withGlowing(false))) {
                world.playSound(null, gravestone.getPos(), SoundEvents.ITEM_INK_SAC_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            } else {
                return false;
            }
        } else if (item instanceof HoneycombItem) {
            if (gravestone.setWaxed(true)) {
                world.syncWorldEvent(null, WorldEvents.BLOCK_WAXED, gravestone.getPos(), 0);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    protected ActionResult onUse(World world, BlockPos pos, PlayerEntity player) {
        if (!(world.getBlockEntity(pos) instanceof AestheticGravestoneBlockEntity blockEntity)) return ActionResult.PASS;

        if (world.isClient()) {
            Util.throwOrPause(new IllegalStateException("Expected to only call this on server"));
        }

        ItemStack headStack = blockEntity.getHeadStack();
        if (!blockEntity.isWaxed() && !headStack.isEmpty()) {
            world.playSound(null, blockEntity.getPos(), GravestonesRegistry.SOUND_BLOCK_GRAVESTONE_REMOVE_SKULL, SoundCategory.BLOCKS);
            if (!player.giveItemStack(headStack)) {
                player.dropItem(headStack, false);
            }
            blockEntity.setHeadStack(player, ItemStack.EMPTY);
            return ActionResult.SUCCESS;
        }

        boolean ranCommand = blockEntity.runCommandClickEvent(player, world, pos);
        if (blockEntity.isWaxed()) {
            world.playSound(null, blockEntity.getPos(), GravestonesRegistry.SOUND_BLOCK_WAXED_GRAVESTONE_INTERACT_FAIL, SoundCategory.BLOCKS);
            return ActionResult.SUCCESS;
        } else if (ranCommand) {
            return ActionResult.SUCCESS;
        } else if (noOtherPlayerEditing(player, blockEntity) && player.canModifyBlocks() && this.isTextLiteralOrEmpty(player, blockEntity)) {
            this.openEditScreen(player, blockEntity);
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }

    public void openEditScreen(PlayerEntity player, AestheticGravestoneBlockEntity blockEntity) {
        blockEntity.setEditor(player.getUuid());
        if (player instanceof ServerPlayerEntity serverPlayer) {
            BlockPos pos = blockEntity.getPos();
            serverPlayer.networkHandler.sendPacket(new BlockUpdateS2CPacket(serverPlayer.getWorld(), pos));
            ServerPlayNetworking.send(serverPlayer, GravestoneEditorOpenS2CPayload.ID, GravestoneEditorOpenS2CPayload.create(pos));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (world.getBlockEntity(pos) instanceof AestheticGravestoneBlockEntity blockEntity) {
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), blockEntity.getHeadStack());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    private static boolean noOtherPlayerEditing(PlayerEntity player, AestheticGravestoneBlockEntity blockEntity) {
        UUID uUID = blockEntity.getEditor();
        return uUID == null || uUID.equals(player.getUuid());
    }

    private boolean isTextLiteralOrEmpty(PlayerEntity player, AestheticGravestoneBlockEntity blockEntity) {
        SignText signText = blockEntity.getText();
        return Arrays.stream(signText.getMessages(player.shouldFilterText()))
                .allMatch(message -> message.equals(ScreenTexts.EMPTY) || message.getContent() instanceof LiteralTextContent);
    }

    @Override
    public boolean isEnabled(FeatureSet enabledFeatures) {
        return GravestonesConfig.AESTHETIC_GRAVESTONES.getValue() && super.isEnabled(enabledFeatures);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AestheticGravestoneBlockEntity(pos, state);
    }
}