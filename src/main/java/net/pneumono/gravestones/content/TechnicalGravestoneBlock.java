package net.pneumono.gravestones.content;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.type.ProfileComponent;
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
import net.minecraft.world.WorldView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.tick.ScheduledTickView;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.ModSupport;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.GravestoneCreation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class TechnicalGravestoneBlock extends BlockWithEntity implements Waterloggable {
    public static final MapCodec<TechnicalGravestoneBlock> CODEC = TechnicalGravestoneBlock.createCodec(TechnicalGravestoneBlock::new);
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
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DAMAGE);
        builder.add(AGE_DAMAGE);
        builder.add(DEATH_DAMAGE);
        builder.add(WATERLOGGED);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return SHAPE;
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
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        createSoulParticles(world, pos);

        BlockEntity entity = world.getBlockEntity(pos);

        if (entity instanceof TechnicalGravestoneBlockEntity gravestone && world instanceof ServerWorld serverWorld) {
            ExperienceOrbEntity.spawn(serverWorld, new Vec3d(pos.getX(), pos.getY(), pos.getZ()), gravestone.getExperienceToDrop(state));
            for (ModSupport support : GravestonesApi.getModSupports()) {
                support.onBreak(gravestone);
            }
        }

        return super.onBreak(world, pos, state, player);
    }

    public static void createSoulParticles(World world, BlockPos pos) {
        Random random = new Random();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            for (int i = 0; i < 16; ++i) {
                serverWorld.spawnParticles(ParticleTypes.SOUL, pos.getX() + (random.nextFloat() * 0.6) + 0.2, pos.getY() + (random.nextFloat() / 10) + 0.25, pos.getZ() + (random.nextFloat() * 0.6) + 0.2, 1, ((double) random.nextFloat() - 0.5) * 0.08, ((double) random.nextFloat() - 0.5) * 0.08, ((double) random.nextFloat() - 0.5) * 0.08, 0.1);
            }
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
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
            Block.createCuboidShape(1, 0, 10, 15, 2, 16),
            Block.createCuboidShape(2, 2, 12, 14, 14, 14),
            Block.createCuboidShape(4, 14, 12, 12, 16, 14)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (state.getBlock() != world.getBlockState(pos).getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TechnicalGravestoneBlockEntity) {
                ItemScatterer.spawn(world, pos, (TechnicalGravestoneBlockEntity)blockEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, moved);
        }
    }

    @Override
    protected void onExploded(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient() && !(player instanceof FakePlayer) && world.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity gravestone) {
            ProfileComponent graveOwner = gravestone.getGraveOwner();
            if ((graveOwner != null && graveOwner.gameProfile().getId().equals(player.getGameProfile().getId())) || !GravestonesConfig.GRAVESTONE_ACCESSIBLE_OWNER_ONLY.getValue()) {
                String uuid = "";
                if (GravestonesConfig.CONSOLE_INFO.getValue()) {
                    uuid = " (" + player.getGameProfile().getId() + ")";
                }
                Gravestones.LOGGER.info("{}{} has found their grave at {}", player.getName().getString(), uuid, GravestoneCreation.posToString(pos));

                GravestoneCreation.info("Returning items...");
                PlayerInventory inventory = player.getInventory();

                List<ItemStack> extraStacks = new ArrayList<>();
                for (int i = 0; i < gravestone.size(); ++i) {
                    if (gravestone.getStack(i) != null) {
                        if (i < 41 && inventory.getStack(i).isEmpty()) {
                            inventory.setStack(i, gravestone.getStack(i).copy());
                        } else {
                            extraStacks.add(gravestone.getStack(i).copy());
                        }

                        gravestone.removeStack(i);
                    }
                }
                for (ItemStack stack : extraStacks) {
                    if (!player.giveItemStack(stack)) {
                        ItemEntity itemEntity = player.dropItem(stack, false);
                        if (itemEntity != null) {
                            itemEntity.resetPickupDelay();
                            itemEntity.setOwner(player.getUuid());
                        }
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
                if (server != null && GravestonesConfig.BROADCAST_COLLECT_IN_CHAT.getValue()) {
                    if (GravestonesConfig.BROADCAST_COORDINATES_IN_CHAT.getValue()) {
                        server.getPlayerManager().broadcast(Text.translatable("gravestones.player_collected_grave_at_coords", player.getName().getString(), GravestoneCreation.posToString(pos)).formatted(Formatting.AQUA), false);
                    } else {
                        server.getPlayerManager().broadcast(Text.translatable("gravestones.player_collected_grave", player.getName().getString()).formatted(Formatting.AQUA), false);
                    }
                }
                world.breakBlock(pos, true);
            } else if (graveOwner != null) {
                player.sendMessage(Text.translatable("gravestones.cannot_open_wrong_player", graveOwner.name().orElse("???")), true);
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
        return TechnicalGravestoneBlock.validateTicker(type, GravestonesRegistry.TECHNICAL_GRAVESTONE_ENTITY, TechnicalGravestoneBlockEntity::tick);
    }
}