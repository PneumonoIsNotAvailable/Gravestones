package net.pneumono.gravestones;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.gravestones.content.GravestonesRegistry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class GravestonesDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(GravestoneTagsGenerator::new);
        pack.addProvider(RecipesGenerator::new);
        pack.addProvider(GravestoneLootTables::new);
    }

    private static class GravestoneTagsGenerator extends FabricTagProvider.BlockTagProvider {
        public GravestoneTagsGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            FabricTagBuilder builder = getOrCreateTagBuilder(GravestonesRegistry.TAG_GRAVESTONE_IRREPLACEABLE);
            for (Block block : Registries.BLOCK.stream().toList()) {
                if (block.getHardness() < 0 || block.getBlastResistance() >= 3600000 || block instanceof BlockWithEntity) {
                    builder.add(block);
                }
            }
            builder.add(Blocks.STRUCTURE_VOID);
        }
    }

    private static class RecipesGenerator extends FabricRecipeProvider {
        public RecipesGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        public void generate(RecipeExporter exporter) {
            ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, GravestonesRegistry.GRAVESTONE)
                    .pattern(" S ")
                    .pattern("S#S")
                    .pattern("sDs")
                    .input('S', Items.STONE)
                    .input('#', Items.SOUL_SAND)
                    .input('s', Items.STONE_SLAB)
                    .input('D', Items.COARSE_DIRT)
                    .criterion(FabricRecipeProvider.hasItem(Items.LEATHER), FabricRecipeProvider.conditionsFromItem(Items.LEATHER))
                    .criterion(FabricRecipeProvider.hasItem(Items.STICK), FabricRecipeProvider.conditionsFromItem(Items.STICK))
                    .offerTo(withConditions(exporter, new ConfigResourceCondition()));

            RecipeProvider.offerSmelting(withConditions(exporter, new ConfigResourceCondition()),
                    List.of(GravestonesRegistry.GRAVESTONE),
                    RecipeCategory.DECORATIONS,
                    GravestonesRegistry.GRAVESTONE_CHIPPED,
                    0.1F,
                    200,
                    "gravestone_cracking"
            );

            RecipeProvider.offerSmelting(withConditions(exporter, new ConfigResourceCondition()),
                    List.of(GravestonesRegistry.GRAVESTONE_CHIPPED),
                    RecipeCategory.DECORATIONS,
                    GravestonesRegistry.GRAVESTONE_DAMAGED,
                    0.1F,
                    200,
                    "gravestone_cracking"
            );
        }
    }

    private static class GravestoneLootTables extends FabricBlockLootTableProvider {
        public GravestoneLootTables(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generate() {
            addDrop(GravestonesRegistry.GRAVESTONE, drops(GravestonesRegistry.GRAVESTONE));
            addDrop(GravestonesRegistry.GRAVESTONE_CHIPPED, drops(GravestonesRegistry.GRAVESTONE_CHIPPED));
            addDrop(GravestonesRegistry.GRAVESTONE_DAMAGED, drops(GravestonesRegistry.GRAVESTONE_DAMAGED));
        }
    }
}
