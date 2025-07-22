package net.pneumono.gravestones.content;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;

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
        if (world instanceof ServerWorld serverWorld) {
            ExperienceOrbEntity.spawn(
                    serverWorld, new Vec3d(pos.getX(), pos.getY(), pos.getZ()),
                    getExperience(view, decay)
            );
        }
    }

    @Override
    public void onCollect(ReadView view, World world, BlockPos pos, PlayerEntity player, int decay) {
        if (GravestonesConfig.DROP_EXPERIENCE.getValue()) {
            onBreak(view, world, pos, decay);
        } else {
            player.addExperience(getExperience(view, decay));
        }
    }

    private static int getExperience(ReadView view, int decay) {
        int experience = view.getInt("experience", 0);
        return GravestonesApi.getDecayedExperience(experience, decay);
    }
}
