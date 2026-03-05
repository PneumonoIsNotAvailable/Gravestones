package net.pneumono.gravestones.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.gravestones.GravestoneCollection;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.BiConsumer;

//? if <1.21.5 {
/*import net.pneumono.gravestones.api.GravestonesApi;
*///?}

//? if <1.20.5 {
/*import net.minecraft.world.InteractionHand;
*///?}

public class TechnicalGravestoneBlock extends AbstractGravestoneBlock {
    public static final IntegerProperty DAMAGE = IntegerProperty.create("damage", 0, 2);

    public TechnicalGravestoneBlock(Properties settings) {
        super(settings);
    }

    //? if >=1.20.3 {
    public static final MapCodec<TechnicalGravestoneBlock> CODEC = TechnicalGravestoneBlock.simpleCodec(TechnicalGravestoneBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(DAMAGE);
    }

    //? <1.20.5 {
    /*@SuppressWarnings("deprecation")
    *///?}
    @Override
    public InteractionResult /*? if >=1.20.5 {*/useWithoutItem/*?} else {*//*use*//*?}*/(
            BlockState state, Level level, BlockPos pos, Player player,
            /*? <1.20.5 {*//*InteractionHand hand,*//*?}*/ BlockHitResult hit
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        createSoulParticles(level, pos);

        if (GravestoneCollection.collect(serverLevel, player, pos)) {
            player.awardStat(GravestonesRegistry.GRAVESTONES_COLLECTED);
        }

        return InteractionResult.SUCCESS;
    }

    //? if >=1.21.5 {
    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);

        if (state.getBlock() != level.getBlockState(pos).getBlock()) {
            createSoulParticles(level, pos);
        }
    }
    //?} else {
    /*//? <1.20.5 {
    /^@SuppressWarnings("deprecation")
    ^///?}
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        //? if >=1.20.3 {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        //?} else {
        /^level.updateNeighborsAt(pos, this);
        ^///?}

        if (state.getBlock() != level.getBlockState(pos).getBlock()) {
            createSoulParticles(level, pos);

            if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity blockEntity) {
                GravestonesApi.onBreak(serverLevel, pos, state.getValue(TechnicalGravestoneBlock.DAMAGE), blockEntity);
            }
        }

        super.onRemove(state, level, pos, newState, moved);
    }
    *///?}

    public static void createSoulParticles(Level level, BlockPos pos) {
        Random random = new Random();
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 16; ++i) {
                serverLevel.sendParticles(ParticleTypes.SOUL, pos.getX() + (random.nextFloat() * 0.6) + 0.2, pos.getY() + (random.nextFloat() / 10) + 0.25, pos.getZ() + (random.nextFloat() * 0.6) + 0.2, 1, ((double) random.nextFloat() - 0.5) * 0.08, ((double) random.nextFloat() - 0.5) * 0.08, ((double) random.nextFloat() - 0.5) * 0.08, 0.1);
            }
        }
    }

    //? if >=1.20.3 {
    @Override
    public void onExplosionHit(BlockState state, /*? if >=1.21.2 {*/ServerLevel/*?} else {*//*Level*//*?}*/ level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {

    }
    //?}

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TechnicalGravestoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, GravestonesRegistry.TECHNICAL_GRAVESTONE_ENTITY, TechnicalGravestoneBlockEntity::tick);
    }
}