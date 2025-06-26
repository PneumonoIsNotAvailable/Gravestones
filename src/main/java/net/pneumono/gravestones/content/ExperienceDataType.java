package net.pneumono.gravestones.content;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestoneDataType;

public class ExperienceDataType extends GravestoneDataType {
    @Override
    public void writeData(WriteView view, PlayerEntity player) {
        if (!GravestonesConfig.STORE_EXPERIENCE.getValue()) return;

        int experience = GravestonesConfig.EXPERIENCE_KEPT.getValue().calculateExperienceKept(player);
        if (GravestonesConfig.EXPERIENCE_CAP.getValue() && experience > 100) {
            experience = 100;
        }

        player.experienceProgress = 0;
        player.experienceLevel = 0;
        player.totalExperience = 0;

        view.putInt("experience", experience);
    }

    @Override
    public void onBreak(ReadView view, World world, BlockPos pos, int decay) {
        dropExperience(view, world, pos, decay);
    }

    @Override
    public void onCollect(ReadView view, World world, BlockPos pos, PlayerEntity player, int decay) {
        dropExperience(view, player.getWorld(), player.getBlockPos(), decay);
    }

    private void dropExperience(ReadView view, World world, BlockPos pos, int decay) {
        dropExperience(world, pos, decay, view.getInt("experience", 0));
    }

    private void dropExperience(World world, BlockPos pos, int decay, int experience) {
        if (world instanceof ServerWorld serverWorld) {
            ExperienceOrbEntity.spawn(
                    serverWorld, new Vec3d(pos.getX(), pos.getY(), pos.getZ()),
                    getExperienceToDrop(experience, decay)
            );
        }
    }

    public static int getExperienceToDrop(int experience, int damage) {
        if (GravestonesConfig.EXPERIENCE_DECAY.getValue()) {
            return experience / (damage + 1);
        } else {
            return experience;
        }
    }
}
