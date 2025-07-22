package net.pneumono.gravestones.gravestones;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.block.TechnicalGravestoneBlock;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.enums.DecayTimeType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GravestoneDecay extends GravestoneManager {
    public static void timeDecayGravestone(World world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (
                GravestonesConfig.DECAY_WITH_TIME.getValue() ||
                !(blockEntity instanceof TechnicalGravestoneBlockEntity entity) ||
                entity.getGraveOwner() == null
        ) return;

        long difference;

        if (GravestonesConfig.DECAY_TIME_TYPE.getValue() == DecayTimeType.TICKS) {
            difference = world.getTime() - entity.getSpawnDateTicks();
        } else if (entity.getSpawnDateTime() != null) {
            difference = GravestoneTime.getDifferenceInSeconds(GravestoneTime.READABLE.format(new Date()), entity.getSpawnDateTime()) * 20;
        } else {
            difference = 0;
        }

        long timeUnit = GravestonesConfig.DECAY_TIME.getValue();
        int ageDamage = state.get(TechnicalGravestoneBlock.AGE_DAMAGE);

        if (difference > (timeUnit * 3)) {
            if (GravestonesApi.shouldDecayAffectGameplay()) {
                world.breakBlock(pos, true);
                return;
            }

        } else if (difference > (timeUnit * 2) && ageDamage != 2) {
            world.setBlockState(pos, state.with(TechnicalGravestoneBlock.AGE_DAMAGE, 2));

        } else if (difference > timeUnit && ageDamage != 1) {
            world.setBlockState(pos, state.with(TechnicalGravestoneBlock.AGE_DAMAGE, 1));
        }

        updateTotalGravestoneDamage(world, pos, state);
    }

    protected static void deathDamageOldGravestones(ServerWorld serverWorld, List<GlobalPos> oldGravePositions, GlobalPos newPos) {
        if (!GravestonesConfig.DECAY_WITH_DEATHS.getValue()) {
            return;
        }

        List<GlobalPos> checkedPositions = new ArrayList<>();
        checkedPositions.add(newPos);
        for (GlobalPos oldPos : oldGravePositions) {
            if (checkedPositions.contains(oldPos)) {
                continue;
            }

            GravestoneDecay.incrementDeathDamage(serverWorld, oldPos);
            checkedPositions.add(oldPos);
        }
    }

    public static void incrementDeathDamage(ServerWorld world, GlobalPos globalPos) {
        BlockPos pos = globalPos.pos();
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(GravestonesRegistry.GRAVESTONE_TECHNICAL)) return;

        int ageDamage = state.get(TechnicalGravestoneBlock.DEATH_DAMAGE) + 1;

        if (ageDamage >= 3) {
            if (GravestonesApi.shouldDecayAffectGameplay()) {
                world.breakBlock(pos, true);
            }
        } else {
            world.setBlockState(pos, state.with(TechnicalGravestoneBlock.DEATH_DAMAGE, ageDamage));
            updateTotalGravestoneDamage(world, pos, world.getBlockState(pos));
        }
    }

    public static void updateTotalGravestoneDamage(World world, BlockPos pos, BlockState state) {
        int totalDamage = state.get(TechnicalGravestoneBlock.AGE_DAMAGE) + state.get(TechnicalGravestoneBlock.DEATH_DAMAGE);
        if (totalDamage == state.get(TechnicalGravestoneBlock.DAMAGE)) return;

        if (totalDamage >= 3) {
            if (GravestonesApi.shouldDecayAffectGameplay()) {
                world.breakBlock(pos, true);
            } else {
                return;
            }
        } else {
            world.setBlockState(pos, state.with(TechnicalGravestoneBlock.DAMAGE, totalDamage));
        }

        if (world.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity entity) {
            entity.markDirty();
        }
    }
}
