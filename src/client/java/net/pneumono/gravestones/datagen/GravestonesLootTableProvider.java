package net.pneumono.gravestones.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.gravestones.content.GravestonesRegistry;

import java.util.concurrent.CompletableFuture;

public class GravestonesLootTableProvider extends FabricBlockLootTableProvider {
    public GravestonesLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(GravestonesRegistry.GRAVESTONE, drops(GravestonesRegistry.GRAVESTONE));
        addDrop(GravestonesRegistry.GRAVESTONE_CHIPPED, drops(GravestonesRegistry.GRAVESTONE_CHIPPED));
        addDrop(GravestonesRegistry.GRAVESTONE_DAMAGED, drops(GravestonesRegistry.GRAVESTONE_DAMAGED));
    }
}
