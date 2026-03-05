package net.pneumono.gravestones.gravestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.block.TechnicalGravestoneBlock;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.enums.DecayTimeType;
import net.pneumono.gravestones.multiversion.VersionUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GravestoneDecay extends GravestoneManager {
    public static void timeDecayGravestone(Level level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (
                !GravestonesConfig.DECAY_WITH_TIME.getValue() ||
                !(blockEntity instanceof TechnicalGravestoneBlockEntity entity) ||
                entity.getGraveOwner() == null
        ) return;

        long difference;

        if (GravestonesConfig.DECAY_TIME_TYPE.getValue() == DecayTimeType.TICKS) {
            difference = level.getGameTime() - entity.getSpawnDateTicks();
        } else if (entity.getSpawnDateTime() != null) {
            difference = GravestoneTime.getDifferenceInSeconds(GravestoneTime.READABLE.format(new Date()), entity.getSpawnDateTime()) * 20;
        } else {
            difference = 0;
        }

        long timeUnit = GravestonesConfig.DECAY_TIME.getValue();

        if (difference > (timeUnit * 3)) {
            entity.setAgeDamage(3);

        } else if (difference > (timeUnit * 2)) {
            entity.setAgeDamage(2);

        } else if (difference > timeUnit) {
            entity.setAgeDamage(1);
        }

        updateTotalGravestoneDamage(level, pos, state, entity);
    }

    protected static void deathDamageOldGravestones(MinecraftServer server, List<GlobalPos> oldGravePositions, GlobalPos newPos) {
        if (!GravestonesConfig.DECAY_WITH_DEATHS.getValue()) {
            return;
        }

        List<GlobalPos> checkedPositions = new ArrayList<>();
        checkedPositions.add(newPos);
        for (GlobalPos oldPos : oldGravePositions) {
            if (checkedPositions.contains(oldPos)) {
                continue;
            }

            GravestoneDecay.incrementDeathDamage(server, oldPos);
            checkedPositions.add(oldPos);
        }
    }

    public static void incrementDeathDamage(MinecraftServer server, GlobalPos globalPos) {
        ServerLevel level = server.getLevel(globalPos.dimension());
        BlockPos pos = globalPos.pos();

        if (
                level == null ||
                !(level.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity entity) ||
                entity.getGraveOwner() == null
        ) return;

        entity.setDeathDamage(entity.getDeathDamage() + 1);
        updateTotalGravestoneDamage(level, pos, level.getBlockState(pos), entity);
    }

    public static void updateTotalGravestoneDamage(Level level, BlockPos pos, BlockState state, TechnicalGravestoneBlockEntity entity) {
        int totalDamage = entity.getTotalDamage();
        if (totalDamage == state.getValue(TechnicalGravestoneBlock.DAMAGE)) return;

        if (totalDamage >= 3) {
            if (GravestonesApi.shouldDecayAffectGameplay()) {
                level.destroyBlock(pos, true);
            } else {
                return;
            }
        } else if (totalDamage >= 0) {
            level.setBlockAndUpdate(pos, state.setValue(TechnicalGravestoneBlock.DAMAGE, totalDamage));
        }

        entity.setChanged();
    }
}
