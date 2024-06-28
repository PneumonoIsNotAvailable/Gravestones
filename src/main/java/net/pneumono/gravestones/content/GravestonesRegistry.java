package net.pneumono.gravestones.content;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
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
import net.pneumono.gravestones.content.entity.AestheticGravestoneBlockEntity;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;
import net.pneumono.pneumonocore.migration.Migration;

public class GravestonesRegistry {
    public static final Block GRAVESTONE_TECHNICAL = registerTechnicalGravestone(
            new TechnicalGravestoneBlock(AbstractBlock.Settings.copy(Blocks.STONE).strength(-1.0F, 3600000.0F).nonOpaque()));
    public static final Block GRAVESTONE = registerAestheticGravestone("gravestone",
            new AestheticGravestoneBlock(AbstractBlock.Settings.copy(Blocks.STONE).strength(3.5F).nonOpaque().requiresTool()));
    public static final Block GRAVESTONE_CHIPPED = registerAestheticGravestone("gravestone_chipped",
            new AestheticGravestoneBlock(AbstractBlock.Settings.copy(Blocks.STONE).strength(3.5F).nonOpaque().requiresTool()));
    public static final Block GRAVESTONE_DAMAGED = registerAestheticGravestone("gravestone_damaged",
            new AestheticGravestoneBlock(AbstractBlock.Settings.copy(Blocks.STONE).strength(3.5F).nonOpaque().requiresTool()));

    public static BlockEntityType<TechnicalGravestoneBlockEntity> TECHNICAL_GRAVESTONE_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Identifier.of(Gravestones.MOD_ID, "technical_gravestone"), BlockEntityType.Builder.create(TechnicalGravestoneBlockEntity::new, GRAVESTONE_TECHNICAL).build()
    );
    public static BlockEntityType<AestheticGravestoneBlockEntity> AESTHETIC_GRAVESTONE_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Identifier.of(Gravestones.MOD_ID, "aesthetic_gravestone"), BlockEntityType.Builder.create(AestheticGravestoneBlockEntity::new, GRAVESTONE, GRAVESTONE_CHIPPED, GRAVESTONE_DAMAGED).build()
    );

    public static final EntityType<GravestoneSkeletonEntity> GRAVESTONE_SKELETON_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Gravestones.MOD_ID, "gravestone_skeleton"),
            EntityType.Builder.<GravestoneSkeletonEntity>create(GravestoneSkeletonEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6F, 1.99F)
                    .maxTrackingRange(8)
                    .build()
    );

    public static final Identifier GRAVESTONES_COLLECTED = Identifier.of(Gravestones.MOD_ID, "gravestones_collected");

    private static Block registerTechnicalGravestone(Block block) {
        return Registry.register(Registries.BLOCK, Identifier.of(Gravestones.MOD_ID, "gravestone_technical"), block);
    }

    private static Block registerAestheticGravestone(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(Gravestones.MOD_ID, name), new AestheticGravestoneBlockItem(block, new Item.Settings()));
        return Registry.register(Registries.BLOCK, Identifier.of(Gravestones.MOD_ID, name), block);
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
                GravestonesRegistry.GRAVESTONE,
                GravestonesRegistry.GRAVESTONE_CHIPPED,
                GravestonesRegistry.GRAVESTONE_DAMAGED
        );

        Registry.register(Registries.CUSTOM_STAT, "gravestones_collected", GRAVESTONES_COLLECTED);
        Stats.CUSTOM.getOrCreateStat(GRAVESTONES_COLLECTED, StatFormatter.DEFAULT);

        Migration.registerItemMigration(Identifier.of(Gravestones.MOD_ID, "gravestone_default"), GRAVESTONE.asItem());
        Migration.registerBlockMigration(Identifier.of(Gravestones.MOD_ID, "gravestone_default"), GRAVESTONE);
        Migration.registerBlockEntityMigration(Identifier.of("gravestone"), TECHNICAL_GRAVESTONE_ENTITY);
        Migration.registerBlockEntityMigration(Identifier.of(Gravestones.MOD_ID, "gravestone"), TECHNICAL_GRAVESTONE_ENTITY);
    }
}
