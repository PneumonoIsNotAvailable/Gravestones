package net.pneumono.gravestones.content;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.ModSupport;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.GravestoneCreation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

public class TechnicalGravestoneBlock extends BlockWithEntity implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final IntProperty DAMAGE = IntProperty.of("damage", 0, 2);
    public static final IntProperty AGE_DAMAGE = IntProperty.of("age_damage", 0, 2);
    public static final IntProperty DEATH_DAMAGE = IntProperty.of("death_damage", 0, 2);

    public TechnicalGravestoneBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(WATERLOGGED, false)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DAMAGE);
        builder.add(AGE_DAMAGE);
        builder.add(DEATH_DAMAGE);
        builder.add(WATERLOGGED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state,BlockView view, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        createSoulParticles(world, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.onStacksDropped(state, world, pos, tool, dropExperience);
        BlockEntity entity = world.getBlockEntity(pos);

        if (entity instanceof TechnicalGravestoneBlockEntity gravestone) {
            ExperienceOrbEntity.spawn(world, new Vec3d(pos.getX(), pos.getY(), pos.getZ()), gravestone.getExperienceToDrop(state));
            for (ModSupport support : GravestonesApi.getModSupports()) {
                support.onBreak(gravestone);
            }
        }
    }

    private void createSoulParticles(World world, BlockPos pos) {
        Random random = new Random();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            for (int i = 0; i < 16; ++i) {
                serverWorld.spawnParticles(ParticleTypes.SOUL, pos.getX() + (random.nextFloat() * 0.6) + 0.2, pos.getY() + (random.nextFloat() / 10) + 0.25, pos.getZ() + (random.nextFloat() * 0.6) + 0.2, 1, ((double) random.nextFloat() - 0.5) * 0.08, ((double) random.nextFloat() - 0.5) * 0.08, ((double) random.nextFloat() - 0.5) * 0.08, 0.1);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
    }

    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(4, 14, 13, 12, 16, 15),
            Block.createCuboidShape(2, 2, 13, 14, 14, 15),
            Block.createCuboidShape(0, 0, 0, 16, 2, 16),
            Block.createCuboidShape(1, 2, 1, 15, 3, 14)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TechnicalGravestoneBlockEntity) {
                ItemScatterer.spawn(world, pos, (TechnicalGravestoneBlockEntity)blockEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient() && hand == Hand.MAIN_HAND && world.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity gravestone) {
            GameProfile graveOwner = gravestone.getGraveOwner();
            if (Objects.equals(graveOwner, player.getGameProfile()) || !Gravestones.GRAVESTONE_ACCESSIBLE_OWNER_ONLY.getValue()) {
                String uuid = "";
                if (Gravestones.CONSOLE_INFO.getValue()) {
                    uuid = " (" + player.getGameProfile().getId() + ")";
                }
                Gravestones.LOGGER.info(player.getName().getString() + uuid + " has found their grave at " + GravestoneCreation.posToString(pos));

                GravestoneCreation.logger("Returning items...");
                PlayerInventory inventory = player.getInventory();
                for (int i = 0; i < gravestone.size(); ++i) {
                    if (gravestone.getStack(i) != null) {
                        if (i < 41 && inventory.getStack(i).isEmpty()) {
                            inventory.setStack(i, gravestone.getStack(i));
                        } else {
                            ItemEntity item = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), gravestone.getStack(i));
                            world.spawnEntity(item);
                        }

                        gravestone.removeStack(i);
                    }
                }

                if (world instanceof ServerWorld serverWorld) {
                    ExperienceOrbEntity.spawn(serverWorld, new Vec3d(pos.getX(), pos.getY(), pos.getZ()), gravestone.getExperienceToDrop(state));
                    gravestone.setExperience(0);
                }

                for (ModSupport support : GravestonesApi.getModSupports()) {
                    support.onCollect(player, gravestone);
                }

                player.incrementStat(GravestonesRegistry.GRAVESTONES_COLLECTED);
                MinecraftServer server = world.getServer();
                if (server != null && Gravestones.BROADCAST_COLLECT_IN_CHAT.getValue()) {
                    if (Gravestones.BROADCAST_COORDINATES_IN_CHAT.getValue()) {
                        server.getPlayerManager().broadcast(Text.translatable("gravestones.player_collected_grave_at_coords", player.getName().getString(), GravestoneCreation.posToString(pos)).formatted(Formatting.AQUA), false);
                    } else {
                        server.getPlayerManager().broadcast(Text.translatable("gravestones.player_collected_grave", player.getName().getString()).formatted(Formatting.AQUA), false);
                    }
                }
                world.breakBlock(pos, true);
            } else if (graveOwner != null) {
                player.sendMessage(Text.translatable("gravestones.cannot_open_wrong_player", graveOwner.getName()), true);
            } else {
                player.sendMessage(Text.translatable("gravestones.cannot_open_no_owner"), true);
            }
            createSoulParticles(world, pos);
        }

        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TechnicalGravestoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, GravestonesRegistry.TECHNICAL_GRAVESTONE_ENTITY, TechnicalGravestoneBlockEntity::tick);
    }
}