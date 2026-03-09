package net.pneumono.gravestones.content;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;

public class ExperienceDataType extends GravestoneDataType {
    private static final String KEY = "experience";

    @Override
    public void writeData(CompoundTag tag, DynamicOps<Tag> ops, Player player) {
        if (!GravestonesConfig.STORE_EXPERIENCE.getValue()) return;

        int experience = GravestonesConfig.EXPERIENCE_KEPT.getValue().calculateExperienceKept(player);
        if (GravestonesConfig.EXPERIENCE_CAP.getValue() && experience > 100) {
            experience = 100;
        }

        player.experienceProgress = 0;
        player.experienceLevel = 0;
        player.totalExperience = 0;

        tag.putInt(KEY, experience);
    }

    @Override
    public void onBreak(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, int decay) {
        if (level instanceof ServerLevel serverLevel) {
            ExperienceOrb.award(
                    serverLevel, new Vec3(pos.getX(), pos.getY(), pos.getZ()),
                    getExperience(tag, decay)
            );
        }
    }

    @Override
    public void onCollect(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, Player player, int decay) {
        if (GravestonesConfig.DROP_EXPERIENCE.getValue()) {
            onBreak(tag, ops, level, pos, decay);
        } else {
            player.giveExperiencePoints(getExperience(tag, decay));
        }
    }

    private static int getExperience(CompoundTag nbt, int decay) {
        int experience = nbt.getInt(KEY)/*? if >=1.21.5 {*/.orElse(0)/*?}*/;
        return GravestonesApi.getDecayedExperience(experience, decay);
    }
}
