package net.pneumono.gravestones.content;

import net.fabricmc.fabric.api.event.registry.FabricRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
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
import net.minecraft.registry.*;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.content.entity.AestheticGravestoneBlockEntity;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;
import net.pneumono.pneumonocore.migration.Migration;

import java.util.function.Function;

public class GravestonesRegistry {
    public static final Block GRAVESTONE_TECHNICAL = registerGravestone("gravestone_technical",
            TechnicalGravestoneBlock::new, AbstractBlock.Settings.copy(Blocks.STONE).strength(-1.0F, 3600000.0F).nonOpaque());
    public static final Block GRAVESTONE = registerAestheticGravestone("gravestone",
            AestheticGravestoneBlock::new, AbstractBlock.Settings.copy(Blocks.STONE).strength(3.5F).nonOpaque().requiresTool());
    public static final Block GRAVESTONE_CHIPPED = registerAestheticGravestone("gravestone_chipped",
            AestheticGravestoneBlock::new, AbstractBlock.Settings.copy(Blocks.STONE).strength(3.5F).nonOpaque().requiresTool());
    public static final Block GRAVESTONE_DAMAGED = registerAestheticGravestone("gravestone_damaged",
            AestheticGravestoneBlock::new, AbstractBlock.Settings.copy(Blocks.STONE).strength(3.5F).nonOpaque().requiresTool());

    public static BlockEntityType<TechnicalGravestoneBlockEntity> TECHNICAL_GRAVESTONE_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Gravestones.identifier("technical_gravestone"), FabricBlockEntityTypeBuilder.create(TechnicalGravestoneBlockEntity::new, GRAVESTONE_TECHNICAL).build()
    );
    public static BlockEntityType<AestheticGravestoneBlockEntity> AESTHETIC_GRAVESTONE_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Gravestones.identifier("aesthetic_gravestone"), FabricBlockEntityTypeBuilder.create(AestheticGravestoneBlockEntity::new, GRAVESTONE, GRAVESTONE_CHIPPED, GRAVESTONE_DAMAGED).build()
    );

    public static final EntityType<GravestoneSkeletonEntity> GRAVESTONE_SKELETON_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            Gravestones.identifier("gravestone_skeleton"),
            EntityType.Builder.<GravestoneSkeletonEntity>create(GravestoneSkeletonEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6F, 1.99F)
                    .maxTrackingRange(8)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Gravestones.identifier("gravestone_skeleton")))
    );

    public static final Identifier GRAVESTONES_COLLECTED = Gravestones.identifier("gravestones_collected");

    private static Block registerAestheticGravestone(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        Block block = registerGravestone(name, factory, settings);
        Registry.register(Registries.ITEM, Gravestones.identifier(name), new AestheticGravestoneBlockItem(block, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Gravestones.identifier(name)))));
        return block;
    }

    private static Block registerGravestone(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        return Registry.register(Registries.BLOCK, Gravestones.identifier(name), factory.apply(settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Gravestones.identifier(name)))));
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

        Registries.ITEM.addAlias(Gravestones.identifier("gravestone_default"), Gravestones.identifier("gravestone"));
        Registries.BLOCK.addAlias(Gravestones.identifier("gravestone_default"), Gravestones.identifier("gravestone"));
        Registries.BLOCK_ENTITY_TYPE.addAlias(Identifier.of("gravestone"), Gravestones.identifier("technical_gravestone"));
        Registries.BLOCK_ENTITY_TYPE.addAlias(Gravestones.identifier("gravestone"), Gravestones.identifier("technical_gravestone"));
    }
}
