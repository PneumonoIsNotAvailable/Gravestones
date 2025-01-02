package net.pneumono.gravestones;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.pneumono.gravestones.datagen.GravestonesBlockTagProvider;
import net.pneumono.gravestones.datagen.GravestonesLootTableProvider;
import net.pneumono.gravestones.datagen.GravestonesRecipeProvider;

@SuppressWarnings("unused")
public class GravestonesDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(GravestonesRecipeProvider::new);
        pack.addProvider(GravestonesLootTableProvider::new);
        pack.addProvider(GravestonesBlockTagProvider::new);
    }
}
