package net.pneumono.gravestones;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;
import net.pneumono.gravestones.content.GravestonesRegistry;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class GravestonesDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(RecipesGenerator::new);
        pack.addProvider(GravestoneLootTables::new);
    }

    private static class RecipesGenerator extends FabricRecipeProvider {
        public RecipesGenerator(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generate(Consumer<RecipeJsonProvider> exporter) {
            ConditionJsonProvider provider = new ConditionJsonProvider() {
                @Override
                public Identifier getConditionId() {
                    return Gravestones.identifier("configurations");
                }

                @Override
                public void writeParameters(JsonObject object) {

                }
            };

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
                    .offerTo(withConditions(exporter, provider));

            RecipeProvider.offerSmelting(withConditions(exporter, provider),
                    List.of(GravestonesRegistry.GRAVESTONE),
                    RecipeCategory.DECORATIONS,
                    GravestonesRegistry.GRAVESTONE_CHIPPED,
                    0.1F,
                    200,
                    "gravestone_cracking"
            );

            RecipeProvider.offerSmelting(withConditions(exporter, provider),
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
        public GravestoneLootTables(FabricDataOutput dataOutput) {
            super(dataOutput);
        }

        @Override
        public void generate() {
            addDrop(GravestonesRegistry.GRAVESTONE, drops(GravestonesRegistry.GRAVESTONE));
            addDrop(GravestonesRegistry.GRAVESTONE_CHIPPED, drops(GravestonesRegistry.GRAVESTONE_CHIPPED));
            addDrop(GravestonesRegistry.GRAVESTONE_DAMAGED, drops(GravestonesRegistry.GRAVESTONE_DAMAGED));
        }
    }
}
