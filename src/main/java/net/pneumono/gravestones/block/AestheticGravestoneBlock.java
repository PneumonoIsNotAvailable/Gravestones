package net.pneumono.gravestones.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.PlainTextContent;
import net.minecraft.util.*;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.networking.GravestoneEditorOpenS2CPayload;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public class AestheticGravestoneBlock extends BlockWithEntity implements Waterloggable {
    public static final MapCodec<AestheticGravestoneBlock> CODEC = AestheticGravestoneBlock.createCodec(AestheticGravestoneBlock::new);
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public AestheticGravestoneBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false)
        );
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(FACING, Objects.requireNonNull(ctx.getPlayer()).getHorizontalFacing().getOpposite())
                .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(WATERLOGGED);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state,BlockView view, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case EAST -> SHAPE_E;
            case SOUTH -> SHAPE_S;
            case WEST -> SHAPE_W;
            default -> SHAPE_N;
        };
    }

    private static final VoxelShape SHAPE_N = Stream.of(
            Block.createCuboidShape(1, 0, 10, 15, 2, 16),
            Block.createCuboidShape(2, 2, 12, 14, 14, 14),
            Block.createCuboidShape(4, 14, 12, 12, 16, 14)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
    private static final VoxelShape SHAPE_E = Stream.of(
            Block.createCuboidShape(0, 0, 1, 6, 2, 15),
            Block.createCuboidShape(2, 2, 2, 4, 14, 14),
            Block.createCuboidShape(2, 14, 4, 4, 16, 12)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
    private static final VoxelShape SHAPE_S = Stream.of(
            Block.createCuboidShape(1, 0, 0, 15, 2, 6),
            Block.createCuboidShape(2, 2, 2, 14, 14, 4),
            Block.createCuboidShape(4, 14, 2, 12, 16, 4)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
    private static final VoxelShape SHAPE_W = Stream.of(
            Block.createCuboidShape(10, 0, 1, 16, 2, 15),
            Block.createCuboidShape(12, 2, 2, 14, 14, 14),
            Block.createCuboidShape(12, 14, 4, 14, 16, 12)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof AestheticGravestoneBlockEntity gravestone)) {
            return ActionResult.PASS;
        }
        Item item = stack.getItem();

        if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock && gravestone.getHeadStack().isEmpty()) {
            if (!world.isClient()) {
                gravestone.setHeadStack(player, stack);
            }
            return ActionResult.SUCCESS;
        }

        boolean waxed = gravestone.isWaxed();

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
                stack.decrementUnlessCreative(1, player);
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
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

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof AestheticGravestoneBlockEntity blockEntity)) return ActionResult.PASS;

        if (world.isClient()) {
            Util.getFatalOrPause(new IllegalStateException("Expected to only call this on server"));
        }

        ItemStack headStack = blockEntity.getHeadStack();
        if (!headStack.isEmpty()) {
            if (!player.giveItemStack(headStack)) {
                player.dropItem(headStack, false);
            }
            blockEntity.setHeadStack(player, ItemStack.EMPTY);
            return ActionResult.SUCCESS;
        }

        boolean ranCommand = blockEntity.runCommandClickEvent(player, world, pos);
        if (blockEntity.isWaxed()) {
            world.playSound(null, blockEntity.getPos(), GravestonesRegistry.SOUND_BLOCK_WAXED_GRAVESTONE_INTERACT_FAIL, SoundCategory.BLOCKS);
            return ActionResult.SUCCESS_SERVER;
        } else if (ranCommand) {
            return ActionResult.SUCCESS_SERVER;
        } else if (noOtherPlayerEditing(player, blockEntity) && player.canModifyBlocks() && this.isTextLiteralOrEmpty(player, blockEntity)) {
            this.openEditScreen(player, blockEntity);
            return ActionResult.SUCCESS_SERVER;
        } else {
            return ActionResult.PASS;
        }
    }

    public void openEditScreen(PlayerEntity player, AestheticGravestoneBlockEntity blockEntity) {
        blockEntity.setEditor(player.getUuid());
        if (player instanceof ServerPlayerEntity serverPlayer) {
            BlockPos pos = blockEntity.getPos();
            serverPlayer.networkHandler.sendPacket(new BlockUpdateS2CPacket(serverPlayer.getWorld(), pos));
            ServerPlayNetworking.send(serverPlayer, new GravestoneEditorOpenS2CPayload(pos));
        }
    }

    private static boolean noOtherPlayerEditing(PlayerEntity player, AestheticGravestoneBlockEntity blockEntity) {
        UUID uUID = blockEntity.getEditor();
        return uUID == null || uUID.equals(player.getUuid());
    }

    private boolean isTextLiteralOrEmpty(PlayerEntity player, AestheticGravestoneBlockEntity blockEntity) {
        SignText signText = blockEntity.getText();
        return Arrays.stream(signText.getMessages(player.shouldFilterText()))
                .allMatch(message -> message.equals(ScreenTexts.EMPTY) || message.getContent() instanceof PlainTextContent);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.math.random.Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
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