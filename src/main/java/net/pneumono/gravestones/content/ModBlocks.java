package net.pneumono.gravestones.content;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.content.entity.GravestoneBlockEntity;

public class ModBlocks {
    public static final Block GRAVESTONE_TECHNICAL = registerBlock("gravestone_technical",
            new TechnicalGravestoneBlock(FabricBlockSettings.copyOf(Blocks.STONE).strength(-1.0F, 3600000.0F).nonOpaque()));
    public static final Block GRAVESTONE_DEFAULT = registerBlock("gravestone_default",
            new AestheticGravestoneBlock(FabricBlockSettings.copyOf(Blocks.STONE).strength(3.5F).nonOpaque().requiresTool()));
    public static final Block GRAVESTONE_CHIPPED = registerBlock("gravestone_chipped",
            new AestheticGravestoneBlock(FabricBlockSettings.copyOf(Blocks.STONE).strength(3.5F).nonOpaque().requiresTool()));
    public static final Block GRAVESTONE_DAMAGED = registerBlock("gravestone_damaged",
            new AestheticGravestoneBlock(FabricBlockSettings.copyOf(Blocks.STONE).strength(3.5F).nonOpaque().requiresTool()));

    public static BlockEntityType<GravestoneBlockEntity> GRAVESTONE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, new Identifier("gravestone"), FabricBlockEntityTypeBuilder.create(GravestoneBlockEntity::new, ModBlocks.GRAVESTONE_TECHNICAL).build()
    );

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(Gravestones.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, new Identifier(Gravestones.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {}
}
