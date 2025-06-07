package net.pneumono.gravestones.gravestones;

import net.minecraft.entity.player.PlayerEntity;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;

public class GravestoneContents extends GravestonesManager {
    public static void insertGravestoneData(PlayerEntity entity, TechnicalGravestoneBlockEntity gravestone) {
        info("Inserting gravestone data into grave...");

        gravestone.setContents(GravestonesApi.getDataToInsert(entity));

        info("Data inserted!");
    }

    public static int getExperienceToDrop(int experience, int damage) {
        if (GravestonesConfig.EXPERIENCE_DECAY.getValue()) {
            return experience / (damage + 1);
        } else {
            return experience;
        }
    }
}
