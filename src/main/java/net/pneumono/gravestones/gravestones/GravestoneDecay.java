package net.pneumono.gravestones.gravestones;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestonesApi;
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
        int ageDamage = entity.getAgeDamage();

        if (difference > (timeUnit * 3)) {
            entity.setAgeDamage(3);

        } else if (difference > (timeUnit * 2) && ageDamage != 2) {
            entity.setAgeDamage(2);

        } else if (difference > timeUnit && ageDamage != 1) {
            entity.setAgeDamage(1);
        }

        updateTotalGravestoneDamage(world, pos, state, entity);
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

        if (
                !(world.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity entity) ||
                entity.getGraveOwner() == null
        ) return;

        entity.setDeathDamage(entity.getDeathDamage() + 1);
        updateTotalGravestoneDamage(world, pos, world.getBlockState(pos), entity);
    }

    public static void updateTotalGravestoneDamage(World world, BlockPos pos, BlockState state, TechnicalGravestoneBlockEntity entity) {
        int totalDamage = entity.getTotalDamage();
        if (totalDamage == state.get(TechnicalGravestoneBlock.DAMAGE)) return;

        if (totalDamage >= 3) {
            if (GravestonesApi.shouldDecayAffectGameplay()) {
                world.breakBlock(pos, true);
            } else {
                return;
            }
        } else if (totalDamage >= 0) {
            world.setBlockState(pos, state.with(TechnicalGravestoneBlock.DAMAGE, totalDamage));
        }

        entity.markDirty();
    }
}
