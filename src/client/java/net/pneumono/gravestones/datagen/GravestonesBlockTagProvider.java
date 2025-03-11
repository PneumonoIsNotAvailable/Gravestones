package net.pneumono.gravestones.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.pneumono.gravestones.content.GravestonesRegistry;

import java.util.concurrent.CompletableFuture;

public class GravestonesBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public GravestonesBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(
                GravestonesRegistry.GRAVESTONE,
                GravestonesRegistry.GRAVESTONE_CHIPPED,
                GravestonesRegistry.GRAVESTONE_DAMAGED
        );
        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL).add(
                GravestonesRegistry.GRAVESTONE,
                GravestonesRegistry.GRAVESTONE_CHIPPED,
                GravestonesRegistry.GRAVESTONE_DAMAGED
        );
        getOrCreateTagBuilder(BlockTags.WITHER_IMMUNE).add(GravestonesRegistry.GRAVESTONE_TECHNICAL);
        getOrCreateTagBuilder(BlockTags.DRAGON_TRANSPARENT).add(GravestonesRegistry.GRAVESTONE_TECHNICAL);
        getOrCreateTagBuilder(BlockTags.FEATURES_CANNOT_REPLACE).add(GravestonesRegistry.GRAVESTONE_TECHNICAL);
    }
}
