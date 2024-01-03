package net.pneumono.gravestones.content;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.content.entity.GravestoneBlockEntity;

public class GravestonesContent {
    public static final Block GRAVESTONE_TECHNICAL = registerBlockWithoutItem("gravestone_technical",
            new TechnicalGravestoneBlock(FabricBlockSettings.copyOf(Blocks.STONE).strength(-1.0F, 3600000.0F).nonOpaque()));
    public static final Block GRAVESTONE_DEFAULT = registerBlock("gravestone_default",
            new AestheticGravestoneBlock(FabricBlockSettings.copyOf(Blocks.STONE).strength(3.5F).nonOpaque().requiresTool()));
    public static final Block GRAVESTONE_CHIPPED = registerBlock("gravestone_chipped",
            new AestheticGravestoneBlock(FabricBlockSettings.copyOf(Blocks.STONE).strength(3.5F).nonOpaque().requiresTool()));
    public static final Block GRAVESTONE_DAMAGED = registerBlock("gravestone_damaged",
            new AestheticGravestoneBlock(FabricBlockSettings.copyOf(Blocks.STONE).strength(3.5F).nonOpaque().requiresTool()));

    public static final TagKey<Block> TAG_GRAVESTONE_IRREPLACEABLE = TagKey.of(RegistryKeys.BLOCK, new Identifier(Gravestones.MOD_ID, "gravestone_irreplaceable"));

    public static final Identifier GRAVESTONES_COLLECTED = new Identifier(Gravestones.MOD_ID, "gravestones_collected");

    public static BlockEntityType<GravestoneBlockEntity> GRAVESTONE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, new Identifier("gravestone"), FabricBlockEntityTypeBuilder.create(GravestoneBlockEntity::new, GravestonesContent.GRAVESTONE_TECHNICAL).build()
    );

    public static final EntityType<GravestoneSkeletonEntity> GRAVESTONE_SKELETON_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Gravestones.MOD_ID, "gravestone_skeleton"),
            FabricEntityTypeBuilder.<GravestoneSkeletonEntity>create(SpawnGroup.MISC, GravestoneSkeletonEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.99F))
                    .trackRangeBlocks(8)
                    .build()
    );

    private static Block registerBlockWithoutItem(String name, Block block) {
        return Registry.register(Registries.BLOCK, new Identifier(Gravestones.MOD_ID, name), block);
    }

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(Gravestones.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, new Identifier(Gravestones.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    private static void addToFunctionalGroup(ItemConvertible... items) {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((content) -> {
            for (ItemConvertible item : items) {
                content.add(item);
            }
        });
    }

    public static void registerModContent() {
        FabricDefaultAttributeRegistry.register(GRAVESTONE_SKELETON_ENTITY_TYPE, GravestoneSkeletonEntity.createAbstractSkeletonAttributes());

        addToFunctionalGroup(
                GravestonesContent.GRAVESTONE_DEFAULT,
                GravestonesContent.GRAVESTONE_CHIPPED,
                GravestonesContent.GRAVESTONE_DAMAGED
        );

        Registry.register(Registries.CUSTOM_STAT, "gravestones_collected", GRAVESTONES_COLLECTED);
        Stats.CUSTOM.getOrCreateStat(GRAVESTONES_COLLECTED, StatFormatter.DEFAULT);
    }
}
