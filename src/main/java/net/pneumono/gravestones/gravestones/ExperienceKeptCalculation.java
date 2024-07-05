package net.pneumono.gravestones.gravestones;

import net.minecraft.entity.player.PlayerEntity;

@SuppressWarnings("unused")
public enum ExperienceKeptCalculation {
    ALL(entity -> entity.totalExperience),
    THREE_QUARTERS(entity -> (int)(entity.totalExperience * 3f / 4f)),
    TWO_THIRDS(entity -> (int)(entity.totalExperience * 2f / 3f)),
    HALF(entity -> (int)(entity.totalExperience / 2f)),
    ONE_THIRD(entity -> (int)(entity.totalExperience / 3f)),
    ONE_QUARTER(entity -> (int)(entity.totalExperience / 4f)),
    VANILLA(entity -> entity.experienceLevel * 7);

    private final Calculation calculation;

    ExperienceKeptCalculation(Calculation calculation) {
        this.calculation = calculation;
    }

    public int calculateExperienceKept(PlayerEntity entity) {
        return this.calculation.calculateExperienceKept(entity);
    }

    public interface Calculation {
        int calculateExperienceKept(PlayerEntity entity);
    }
}
