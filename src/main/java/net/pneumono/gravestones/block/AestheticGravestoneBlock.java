package net.pneumono.gravestones.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.networking.GravestoneEditorOpenS2CPayload;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

//? if >=26.1
import net.minecraft.core.component.DataComponents;

//? if <1.21.11
//import net.minecraft.Util;

//? if <1.21.2 && >=1.20.5 {
/*import net.minecraft.world.ItemInteractionResult;
*///?}

//? if >=1.20.3 {
import net.minecraft.network.chat.contents.PlainTextContents;
//?} else {
/*import net.minecraft.network.chat.contents.LiteralContents;
*///?}

public class AestheticGravestoneBlock extends AbstractGravestoneBlock {
    public AestheticGravestoneBlock(Properties properties) {
        super(properties);
    }

    //? if >=1.20.3 {
    public static final MapCodec<AestheticGravestoneBlock> CODEC = AestheticGravestoneBlock.simpleCodec(AestheticGravestoneBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    //?}

    //? if <1.20.5 {
    /*@SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = useItemOn(player.getItemInHand(hand), state, level, pos, player, hand, hit);
        if (result == InteractionResult.PASS) {
            return useWithoutItem(state, level, pos, player, hit);
        } else {
            return result;
        }
    }
    *///?}

    //? if >=1.20.5 {
    @Override
    //?}
    protected /*? if >=1.21.2 {*/InteractionResult/*?} else if >=1.20.5 {*//*ItemInteractionResult*//*?} else {*//*InteractionResult*//*?}*/ useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof AestheticGravestoneBlockEntity gravestone)) {
            //? if >=1.21.2 {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
            //?} else if >=1.20.5 {
            /*return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            *///?} else {
            /*return InteractionResult.PASS;
            *///?}
        }
        Item item = stack.getItem();

        boolean waxed = gravestone.isWaxed();

        if (!waxed && item instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock && gravestone.getHeadStack().isEmpty()) {
            if (!level.isClientSide()) {
                level.playSound(null, blockEntity.getBlockPos(), GravestonesRegistry.SOUND_BLOCK_GRAVESTONE_ADD_SKULL, SoundSource.BLOCKS);
                gravestone.setHeadStack(player, stack);
            }
            //? if >=1.21.2 {
            return InteractionResult.SUCCESS;
            //?} else if >=1.20.5 {
            /*return ItemInteractionResult.SUCCESS;
            *///?} else {
            /*return InteractionResult.SUCCESS;
            *///?}
        }

        if (!level.isClientSide()) {
            if (
                    item instanceof SignApplicator signChangingItem &&
                    player.mayBuild() &&
                    !waxed &&
                    noOtherPlayerEditing(player, gravestone) &&
                    signChangingItem.canApplyToSign(gravestone.getText()/*? if >=26.1 {*/, stack/*?}*/, player) &&
                    tryTextChange(level, stack, gravestone)
            ) {
                gravestone.runCommandClickEvent(player, level, pos);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                level.gameEvent(GameEvent.BLOCK_CHANGE, gravestone.getBlockPos(), GameEvent.Context.of(player, gravestone.getBlockState()));
                //? if >=1.20.5 {
                stack.consume(1, player);
                //?} else {
                /*if (!player.isCreative()) {
                    stack.shrink(1);
                }
                *///?}

                //? if >=1.21.2 {
                return InteractionResult.SUCCESS;
                //?} else if >=1.20.5 {
                /*return ItemInteractionResult.SUCCESS;
                *///?} else {
                /*return InteractionResult.SUCCESS;
                *///?}
            } else {
                //? if >=1.21.2 {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
                //?} else if >=1.20.5 {
                /*return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                *///?} else {
                /*return InteractionResult.PASS;
                *///?}
            }
        } else {
            if ((!(item instanceof SignApplicator) || !player.mayBuild()) && !waxed) {
                //? if >=1.21.2 {
                return InteractionResult.CONSUME;
                //?} else if >=1.20.5 {
                /*return ItemInteractionResult.CONSUME;
                *///?} else {
                /*return InteractionResult.CONSUME;
                *///?}
            } else {
                //? if >=1.21.2 {
                return InteractionResult.SUCCESS;
                //?} else if >=1.20.5 {
                /*return ItemInteractionResult.SUCCESS;
                *///?} else {
                /*return InteractionResult.SUCCESS;
                *///?}
            }
        }
    }

    private static boolean tryTextChange(Level level, ItemStack stack, AestheticGravestoneBlockEntity gravestone) {
        // Hardcoding all of these sucks but what else am I going to do
        Item item = stack.getItem();
        if (item instanceof DyeItem dyeItem) {

            //? if >=26.1 {
            DyeColor color = stack.get(DataComponents.DYE);
            //?} else {
            /*DyeColor color = dyeItem.getDyeColor();
            *///?}

            if (color != null && gravestone.changeText(text -> text.setColor(color))) {
                level.playSound(null, gravestone.getBlockPos(), SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                return true;
            } else {
                return false;
            }
        } else if (item instanceof GlowInkSacItem) {
            if (gravestone.changeText(text -> text.setHasGlowingText(true))) {
                level.playSound(null, gravestone.getBlockPos(), SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                return true;
            } else {
                return false;
            }
        } else if (item instanceof InkSacItem) {
            if (gravestone.changeText(text -> text.setHasGlowingText(false))) {
                level.playSound(null, gravestone.getBlockPos(), SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                return true;
            } else {
                return false;
            }
        } else if (item instanceof HoneycombItem) {
            if (gravestone.setWaxed(true)) {
                level.levelEvent(null, LevelEvent.PARTICLES_AND_SOUND_WAX_ON, gravestone.getBlockPos(), 0);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    //? >=1.20.5 {
    @Override
    //?}
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof AestheticGravestoneBlockEntity blockEntity)) return InteractionResult.PASS;

        if (level.isClientSide()) {
            Util.pauseInIde(new IllegalStateException("Expected to only call this on server"));
        }

        ItemStack headStack = blockEntity.getHeadStack();
        if (!blockEntity.isWaxed() && !headStack.isEmpty()) {
            level.playSound(null, blockEntity.getBlockPos(), GravestonesRegistry.SOUND_BLOCK_GRAVESTONE_REMOVE_SKULL, SoundSource.BLOCKS);
            if (!player.addItem(headStack)) {
                player.drop(headStack, false);
            }
            blockEntity.setHeadStack(player, ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        }

        boolean ranCommand = blockEntity.runCommandClickEvent(player, level, pos);
        if (blockEntity.isWaxed()) {
            level.playSound(null, blockEntity.getBlockPos(), GravestonesRegistry.SOUND_BLOCK_WAXED_GRAVESTONE_INTERACT_FAIL, SoundSource.BLOCKS);
            return InteractionResult.SUCCESS;
        } else if (ranCommand) {
            return InteractionResult.SUCCESS;
        } else if (noOtherPlayerEditing(player, blockEntity) && player.mayBuild() && this.isTextLiteralOrEmpty(player, blockEntity)) {
            this.openEditScreen(player, blockEntity);
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    //? <1.20.5 {
    /*@SuppressWarnings("deprecation")
    *///?}
    //? if <1.21.5 {
    /*@Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (level.getBlockEntity(pos) instanceof AestheticGravestoneBlockEntity blockEntity) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), blockEntity.getHeadStack());
        }
        super.onRemove(state, level, pos, newState, moved);
    }
    *///?}

    public void openEditScreen(Player player, AestheticGravestoneBlockEntity blockEntity) {
        blockEntity.setEditor(player.getUUID());
        if (player instanceof ServerPlayer serverPlayer) {
            BlockPos pos = blockEntity.getBlockPos();
            serverPlayer.connection.send(new ClientboundBlockUpdatePacket(serverPlayer.level(), pos));
            GravestoneEditorOpenS2CPayload payload = new GravestoneEditorOpenS2CPayload(pos);
            //? if >=1.20.5 {
            ServerPlayNetworking.send(serverPlayer, payload);
            //?} else {
            /*ServerPlayNetworking.send(serverPlayer, payload.id(), payload.write());
            *///?}
        }
    }

    private static boolean noOtherPlayerEditing(Player player, AestheticGravestoneBlockEntity blockEntity) {
        UUID uUID = blockEntity.getEditor();
        return uUID == null || uUID.equals(player.getUUID());
    }

    private boolean isTextLiteralOrEmpty(Player player, AestheticGravestoneBlockEntity blockEntity) {
        SignText signText = blockEntity.getText();
        return Arrays.stream(signText.getMessages(player.isTextFilteringEnabled()))
                .allMatch(message -> message.equals(CommonComponents.EMPTY) || message.getContents() instanceof /*? if >=1.20.3 {*/PlainTextContents/*?} else {*//*LiteralContents*//*?}*/);
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return GravestonesConfig.AESTHETIC_GRAVESTONES.getValue() && super.isEnabled(enabledFeatures);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AestheticGravestoneBlockEntity(pos, state);
    }
}