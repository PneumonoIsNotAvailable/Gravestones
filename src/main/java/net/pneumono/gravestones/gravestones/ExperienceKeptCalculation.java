package net.pneumono.gravestones.gravestones;

import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Function;

@SuppressWarnings("unused")
public enum ExperienceKeptCalculation {
    ALL(entity -> entity.totalExperience),
    THREE_QUARTERS(entity -> (int)(entity.totalExperience * 3f / 4f)),
    TWO_THIRDS(entity -> (int)(entity.totalExperience * 2f / 3f)),
    HALF(entity -> (int)(entity.totalExperience / 2f)),
    ONE_THIRD(entity -> (int)(entity.totalExperience / 3f)),
    ONE_QUARTER(entity -> (int)(entity.totalExperience / 4f)),
    VANILLA(entity -> entity.experienceLevel * 7);

    private final Function<PlayerEntity, Integer> calculation;

    ExperienceKeptCalculation(Function<PlayerEntity, Integer> calculation) {
        this.calculation = calculation;
    }

    public int calculateExperienceKept(PlayerEntity entity) {
        return this.calculation.apply(entity);
    }
}
