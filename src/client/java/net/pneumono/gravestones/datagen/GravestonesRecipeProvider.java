package net.pneumono.gravestones.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.CookingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.pneumonocore.datagen.ConfigResourceCondition;

import java.util.concurrent.CompletableFuture;

public class GravestonesRecipeProvider extends FabricRecipeProvider {
    public GravestonesRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
        return new RecipeGenerator(wrapperLookup, recipeExporter) {
            @Override
            public void generate() {
                ConfigResourceCondition condition = new ConfigResourceCondition(Gravestones.AESTHETIC_GRAVESTONES, ConfigResourceCondition.Operator.EQUAL, "true");

                this.createShaped(RecipeCategory.DECORATIONS, GravestonesRegistry.GRAVESTONE)
                        .pattern(" s ")
                        .pattern("SSS")
                        .pattern("sss")
                        .input('S', Items.STONE)
                        .input('s', Items.STONE_SLAB)
                        .criterion(hasItem(Items.LEATHER), conditionsFromItem(Items.LEATHER))
                        .criterion(hasItem(Items.STICK), conditionsFromItem(Items.STICK))
                        .offerTo(withConditions(exporter, condition));

                // This is bad, but I have no idea what the intended way to use resource conditions is anymore-
                CookingRecipeJsonBuilder.create(Ingredient.ofItem(GravestonesRegistry.GRAVESTONE), RecipeCategory.DECORATIONS, GravestonesRegistry.GRAVESTONE_CHIPPED, 0.1F, 200, RecipeSerializer.SMELTING, SmeltingRecipe::new)
                        .group("gravestone_cracking")
                        .criterion(hasItem(GravestonesRegistry.GRAVESTONE), this.conditionsFromItem(GravestonesRegistry.GRAVESTONE))
                        .offerTo(withConditions(this.exporter, condition), getItemPath(GravestonesRegistry.GRAVESTONE_CHIPPED));

                CookingRecipeJsonBuilder.create(Ingredient.ofItem(GravestonesRegistry.GRAVESTONE_CHIPPED), RecipeCategory.DECORATIONS, GravestonesRegistry.GRAVESTONE_DAMAGED, 0.1F, 200, RecipeSerializer.SMELTING, SmeltingRecipe::new)
                        .group("gravestone_cracking")
                        .criterion(hasItem(GravestonesRegistry.GRAVESTONE_CHIPPED), this.conditionsFromItem(GravestonesRegistry.GRAVESTONE_CHIPPED))
                        .offerTo(withConditions(this.exporter, condition), getItemPath(GravestonesRegistry.GRAVESTONE_DAMAGED));
            }
        };
    }

    @Override
    public String getName() {
        return "Recipes (Gravestones)";
    }
}
