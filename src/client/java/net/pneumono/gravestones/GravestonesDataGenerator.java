package net.pneumono.gravestones;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.pneumono.gravestones.datagen.*;

public class GravestonesDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(GravestonesRecipeProvider::new);
        pack.addProvider(GravestonesLootTableProvider::new);
        pack.addProvider(GravestonesBlockTagProvider::new);
        pack.addProvider(GravestonesEnglishLangProvider::new);
        pack.addProvider(GravestonesLolcatLangProvider::new);
    }
}
