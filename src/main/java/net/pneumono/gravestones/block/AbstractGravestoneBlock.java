package net.pneumono.gravestones.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

//? if >=1.21.2 {
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
//?} else {
/*import net.minecraft.world.level.LevelAccessor;
*///?}

public abstract class AbstractGravestoneBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public AbstractGravestoneBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    //? if <1.20.5 {
    /*@SuppressWarnings("deprecation")
    *///?}
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    //? if >=1.21.2 {
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, level, tickView, pos, direction, neighborPos, neighborState, random);
    }
    //?} else {
    /*//? if <1.20.5 {
    /^@SuppressWarnings("deprecation")
    ^///?}
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
    *///?}

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, Objects.requireNonNull(ctx.getPlayer()).getDirection().getOpposite())
                .setValue(WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).getType() == Fluids.WATER);
    }

    //? if <1.20.5 {
    /*@SuppressWarnings("deprecation")
    *///?}
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    //? if <1.20.5 {
    /*@SuppressWarnings("deprecation")
    *///?}
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(WATERLOGGED);
    }

    //? if <1.20.5 {
    /*@SuppressWarnings("deprecation")
    *///?}
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> SHAPE_E;
            case SOUTH -> SHAPE_S;
            case WEST -> SHAPE_W;
            default -> SHAPE_N;
        };
    }

    private static final VoxelShape SHAPE_N = Stream.of(
            Block.box(1, 0, 10, 15, 2, 16),
            Block.box(2, 2, 12, 14, 14, 14),
            Block.box(4, 14, 12, 12, 16, 14)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    private static final VoxelShape SHAPE_E = Stream.of(
            Block.box(0, 0, 1, 6, 2, 15),
            Block.box(2, 2, 2, 4, 14, 14),
            Block.box(2, 14, 4, 4, 16, 12)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    private static final VoxelShape SHAPE_S = Stream.of(
            Block.box(1, 0, 0, 15, 2, 6),
            Block.box(2, 2, 2, 14, 14, 4),
            Block.box(4, 14, 2, 12, 16, 4)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    private static final VoxelShape SHAPE_W = Stream.of(
            Block.box(10, 0, 1, 16, 2, 15),
            Block.box(12, 2, 2, 14, 14, 14),
            Block.box(12, 14, 4, 14, 16, 12)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
}
