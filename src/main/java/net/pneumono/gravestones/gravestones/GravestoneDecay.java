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

    /**
     * @return List of newly broken gravestones
     */
    protected static List<GlobalPos> deathDamageOldGravestones(MinecraftServer server, List<GlobalPos> oldGravePositions) {
        if (!GravestonesConfig.DECAY_WITH_DEATHS.getValue()) {
            return oldGravePositions;
        }

        List<GlobalPos> clearedPositions = new ArrayList<>();
        List<GlobalPos> checkedPositions = new ArrayList<>();

        for (GlobalPos oldPos : oldGravePositions) {
            if (checkedPositions.contains(oldPos)) {
                clearedPositions.add(oldPos);
                continue;
            }

            if (GravestoneDecay.incrementDeathDamage(server, oldPos)) {
                clearedPositions.add(oldPos);
            }
            checkedPositions.add(oldPos);
        }

        return clearedPositions;
    }

    /**
     * @return {@code true} if the gravestone was broken or not present, {@code false} otherwise
     */
    public static boolean incrementDeathDamage(MinecraftServer server, GlobalPos globalPos) {
        ServerLevel level = server.getLevel(globalPos.dimension());
        BlockPos pos = globalPos.pos();

        if (
                level == null ||
                !(level.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity entity) ||
                entity.getGraveOwner() == null
        ) return true;

        entity.setDeathDamage(entity.getDeathDamage() + 1);
        return updateTotalGravestoneDamage(level, pos, level.getBlockState(pos), entity);
    }

    /**
     * @return {@code true} if the gravestone was broken, {@code false} otherwise
     */
    public static boolean updateTotalGravestoneDamage(Level level, BlockPos pos, BlockState state, TechnicalGravestoneBlockEntity entity) {
        int decayStage = calculateDecayStage(entity.getTotalDamage());
        boolean broken = decayStage >= 3;
        if (decayStage == state.getValue(TechnicalGravestoneBlock.DAMAGE)) return broken;

        if (broken) {
            if (GravestonesApi.shouldDecayAffectGameplay()) {
                level.destroyBlock(pos, true);
            } else {
                return true;
            }
        } else if (decayStage >= 0) {
            level.setBlockAndUpdate(pos, state.setValue(TechnicalGravestoneBlock.DAMAGE, decayStage));
        }

        entity.setChanged();
        return broken;
    }

    // If the gravestone is damaged above the damage to break, break immediately (3)
    // If the gravestone is undamaged, show as undamaged (0)
    // If the gravestone is one step away from breaking, show as final decay stage (2)
    // Else, calculate distance from breaking normally (0-2)
    public static int calculateDecayStage(int totalDamage) {
        int damageToBreak = GravestonesConfig.DAMAGE_TO_BREAK.getValue();

        if (totalDamage >= damageToBreak) return 3;
        if (totalDamage <= 0) return 0;
        if (totalDamage + 1 == damageToBreak) return 2;

        return (3 * totalDamage) / damageToBreak;
    }
}
