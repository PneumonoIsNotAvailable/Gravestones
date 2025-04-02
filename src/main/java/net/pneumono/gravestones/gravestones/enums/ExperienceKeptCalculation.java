package net.pneumono.gravestones.gravestones.enums;

import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Function;

@SuppressWarnings("unused")
public enum ExperienceKeptCalculation {
    ALL(ExperienceKeptCalculation::getTotalExperience),
    THREE_QUARTERS(entity -> (int)(getTotalExperience(entity) * 3f / 4f)),
    TWO_THIRDS(entity -> (int)(getTotalExperience(entity) * 2f / 3f)),
    HALF(entity -> (int)(getTotalExperience(entity) / 2f)),
    ONE_THIRD(entity -> (int)(getTotalExperience(entity) / 3f)),
    ONE_QUARTER(entity -> (int)(getTotalExperience(entity) / 4f)),
    VANILLA(entity -> entity.experienceLevel * 7);

    private final Function<PlayerEntity, Integer> calculation;

    ExperienceKeptCalculation(Function<PlayerEntity, Integer> calculation) {
        this.calculation = calculation;
    }

    public static int getTotalExperience(PlayerEntity entity) {
        int xpFromLevel;
        int level = entity.experienceLevel;
        if (level <= 16) {
            xpFromLevel = (level * level) + (6 * level);
        } else if (level <= 31) {
            xpFromLevel = (int)((2.5f * level * level) - (40.5f * level) + 360);
        } else {
            xpFromLevel = (int)((4.5f * level * level) - (162.5f * level) + 2220);
        }

        int xpFromProgress = Math.round(getNextLevelExperience(level) * entity.experienceProgress);

        return xpFromLevel + xpFromProgress;
    }

    public static int getNextLevelExperience(int currentLevel) {
        if (currentLevel >= 30) {
            return 112 + (currentLevel - 30) * 9;
        } else if (currentLevel >= 15) {
            return 37 + (currentLevel - 15) * 5;
        } else {
            return 7 + currentLevel * 2;
        }
    }

    public int calculateExperienceKept(PlayerEntity entity) {
        return this.calculation.apply(entity);
    }
}
