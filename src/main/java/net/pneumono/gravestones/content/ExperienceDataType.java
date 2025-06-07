package net.pneumono.gravestones.content;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.gravestones.GravestoneContents;

public class ExperienceDataType extends GravestoneDataType {
    @Override
    public NbtElement getDataToInsert(PlayerEntity player) {
        if (!GravestonesConfig.STORE_EXPERIENCE.getValue()) return NbtInt.of(0);

        int experience = GravestonesConfig.EXPERIENCE_KEPT.getValue().calculateExperienceKept(player);
        if (GravestonesConfig.EXPERIENCE_CAP.getValue() && experience > 100) {
            experience = 100;
        }

        player.experienceProgress = 0;
        player.experienceLevel = 0;
        player.totalExperience = 0;

        return NbtInt.of(experience);
    }

    @Override
    public void onBreak(World world, BlockPos pos, int decay, NbtElement nbt) {
        dropExperience(world, pos, decay, nbt);
    }

    @Override
    public void onCollect(PlayerEntity player, int decay, NbtElement nbt) {
        dropExperience(player.getWorld(), player.getBlockPos(), decay, nbt);
    }

    private void dropExperience(World world, BlockPos pos, int decay, NbtElement nbt) {
        if (nbt == null) {
            return;
        }
        dropExperience(world, pos, decay, nbt.asInt().orElse(0));
    }

    private void dropExperience(World world, BlockPos pos, int decay, int experience) {
        if (world instanceof ServerWorld serverWorld) {
            ExperienceOrbEntity.spawn(
                    serverWorld, new Vec3d(pos.getX(), pos.getY(), pos.getZ()),
                    GravestoneContents.getExperienceToDrop(experience, decay)
            );
        }
    }
}
