package net.pneumono.gravestones.content;

import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;

public class ExperienceDataType extends GravestoneDataType {
    private static final String KEY = "experience";

    @Override
    public void writeData(NbtCompound nbt, DynamicOps<NbtElement> ops, PlayerEntity player) {
        if (!GravestonesConfig.STORE_EXPERIENCE.getValue()) return;

        int experience = GravestonesConfig.EXPERIENCE_KEPT.getValue().calculateExperienceKept(player);
        if (GravestonesConfig.EXPERIENCE_CAP.getValue() && experience > 100) {
            experience = 100;
        }

        player.experienceProgress = 0;
        player.experienceLevel = 0;
        player.totalExperience = 0;

        nbt.putInt(KEY, experience);
    }

    @Override
    public void onBreak(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, int decay) {
        if (world instanceof ServerWorld serverWorld) {
            ExperienceOrbEntity.spawn(
                    serverWorld, new Vec3d(pos.getX(), pos.getY(), pos.getZ()),
                    getExperience(nbt, decay)
            );
        }
    }

    @Override
    public void onCollect(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, PlayerEntity player, int decay) {
        if (GravestonesConfig.DROP_EXPERIENCE.getValue()) {
            onBreak(nbt, ops, world, pos, decay);
        } else {
            player.addExperience(getExperience(nbt, decay));
        }
    }

    private static int getExperience(NbtCompound nbt, int decay) {
        int experience = nbt.getInt(KEY/*? if >=1.21.5 {*/, 0/*?}*/);
        return GravestonesApi.getDecayedExperience(experience, decay);
    }
}
