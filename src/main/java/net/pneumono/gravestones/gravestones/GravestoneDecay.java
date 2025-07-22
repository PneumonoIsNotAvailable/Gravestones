package net.pneumono.gravestones.gravestones;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.block.TechnicalGravestoneBlock;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.data.GravestonePosition;
import net.pneumono.gravestones.gravestones.enums.DecayTimeType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GravestoneDecay extends GravestonesManager {
    public static void timeDecayGravestone(World world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof TechnicalGravestoneBlockEntity entity)) return;

        if (GravestonesConfig.DECAY_WITH_TIME.getValue() && entity.getGraveOwner() != null) {
            long difference;

            if (GravestonesConfig.DECAY_TIME_TYPE.getValue() == DecayTimeType.TICKS) {
                difference = world.getTime() - entity.getSpawnDateTicks();
            } else if (entity.getSpawnDateTime() != null) {
                difference = GravestoneTime.getDifferenceInSeconds(GravestoneTime.READABLE.format(new Date()), entity.getSpawnDateTime()) * 20;
            } else {
                difference = 0;
            }

            long timeUnit = GravestonesConfig.DECAY_TIME.getValue();
            if (difference > (timeUnit * 3)) {
                if (GravestonesApi.shouldDecayAffectGameplay()) {
                    world.breakBlock(pos, true);
                }
            } else if (difference > (timeUnit * 2) && !(state.get(TechnicalGravestoneBlock.AGE_DAMAGE) > 1)) {
                world.setBlockState(pos, state.with(TechnicalGravestoneBlock.AGE_DAMAGE, 2));
            } else if (difference > (timeUnit) && !(state.get(TechnicalGravestoneBlock.AGE_DAMAGE) > 0)) {
                world.setBlockState(pos, state.with(TechnicalGravestoneBlock.AGE_DAMAGE, 1));
            }

            entity.markDirty();
        }

        updateGravestoneDamage(world, pos, state);
    }

    public static void deathDecayOldGravestones(ServerWorld serverWorld, List<GravestonePosition> oldGravePositions, BlockPos gravestonePos) {
        if (!GravestonesConfig.DECAY_WITH_DEATHS.getValue()) {
            info("Gravestone death damage has been disabled in the config, so no graves were damaged");
            return;
        }
        if (oldGravePositions == null) {
            info("No graves to damage!");
            return;
        }

        List<GravestonePosition> usedPositions = new ArrayList<>();
        usedPositions.add(new GravestonePosition(serverWorld.getRegistryKey().getValue(), gravestonePos));
        for (GravestonePosition oldPos : oldGravePositions) {
            if (usedPositions.contains(oldPos)) {
                info("Gravestone at " + posToString(oldPos.asBlockPos()) + " in dimension " + oldPos.dimension.toString() + " has already been damaged, skipping");
                continue;
            }

            GravestoneDecay.deathDecayGravestone(serverWorld, oldPos);
            usedPositions.add(oldPos);
        }
    }

    public static void deathDecayGravestone(ServerWorld serverWorld, GravestonePosition pos) {
        ServerWorld graveWorld = serverWorld.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, pos.dimension));

        if (graveWorld == null) {
            error("GravePosition's dimension (" + pos.dimension.toString() + ") does not exist!");
        } else {
            if (!graveWorld.getBlockState(pos.asBlockPos()).isOf(GravestonesRegistry.GRAVESTONE_TECHNICAL)) {
                info("No gravestone was found at the position " + posToString(pos.asBlockPos()) + " in dimension " + pos.dimension.toString()
                        + ". Most likely this is because the grave has already been collected, or was decayed");
            } else {

                int deathDamage = graveWorld.getBlockState(pos.asBlockPos()).get(TechnicalGravestoneBlock.DEATH_DAMAGE);
                int ageDamage = graveWorld.getBlockState(pos.asBlockPos()).get(TechnicalGravestoneBlock.AGE_DAMAGE);
                String damageType;

                String graveData = "Age: " + ageDamage + ", Death: " + deathDamage;
                if (ageDamage + deathDamage >= 2) {
                    damageType = "broken";
                    if (GravestonesApi.shouldDecayAffectGameplay()) {
                        graveWorld.breakBlock(pos.asBlockPos(), true);
                    }
                } else {
                    damageType = "damaged";
                    graveWorld.setBlockState(pos.asBlockPos(), graveWorld.getBlockState(pos.asBlockPos()).with(TechnicalGravestoneBlock.DEATH_DAMAGE, deathDamage + 1));
                }
                info("Gravestone (" + graveData + ") " + damageType + " at the position " + posToString(pos.asBlockPos()) + " in dimension " + pos.dimension.toString());
            }
        }
    }

    public static void updateGravestoneDamage(World world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof TechnicalGravestoneBlockEntity entity)) return;

        if (state.get(TechnicalGravestoneBlock.DAMAGE) != state.get(TechnicalGravestoneBlock.AGE_DAMAGE) + state.get(TechnicalGravestoneBlock.DEATH_DAMAGE)) {
            if (state.get(TechnicalGravestoneBlock.AGE_DAMAGE) + state.get(TechnicalGravestoneBlock.DEATH_DAMAGE) > 2) {
                if (GravestonesApi.shouldDecayAffectGameplay()) {
                    world.breakBlock(pos, true);
                }
            } else {
                world.setBlockState(pos, state.with(TechnicalGravestoneBlock.DAMAGE, state.get(TechnicalGravestoneBlock.AGE_DAMAGE) + state.get(TechnicalGravestoneBlock.DEATH_DAMAGE)));
            }

            entity.markDirty();
        }

        if (state.get(TechnicalGravestoneBlock.DAMAGE) >= 3 && GravestonesApi.shouldDecayAffectGameplay()) {
            world.breakBlock(pos, true);

            entity.markDirty();
        }
    }
}
