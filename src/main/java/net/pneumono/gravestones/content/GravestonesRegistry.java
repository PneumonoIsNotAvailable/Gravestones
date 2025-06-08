package net.pneumono.gravestones.content;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.block.*;
import net.pneumono.gravestones.networking.GravestoneEditorOpenS2CPayload;
import net.pneumono.gravestones.networking.UpdateGravestoneC2SPayload;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            Registries.BLOCK_ENTITY_TYPE, Gravestones.identifier("technical_gravestone"), BlockEntityType.Builder.create(TechnicalGravestoneBlockEntity::new, GRAVESTONE_TECHNICAL).build()
    );
    public static BlockEntityType<AestheticGravestoneBlockEntity> AESTHETIC_GRAVESTONE_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Gravestones.identifier("aesthetic_gravestone"), BlockEntityType.Builder.create(AestheticGravestoneBlockEntity::new, GRAVESTONE, GRAVESTONE_CHIPPED, GRAVESTONE_DAMAGED).build()
    );

    public static final EntityType<GravestoneSkeletonEntity> GRAVESTONE_SKELETON_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            Gravestones.identifier("gravestone_skeleton"),
            EntityType.Builder.<GravestoneSkeletonEntity>create(GravestoneSkeletonEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6F, 1.99F)
                    .maxTrackingRange(8)
                    .build()
    );

    public static final TagKey<Enchantment> ENCHANTMENT_SKIPS_GRAVESTONES = TagKey.of(RegistryKeys.ENCHANTMENT, Gravestones.identifier("skips_gravestones"));
    public static final TagKey<Item> ITEM_SKIPS_GRAVESTONES = TagKey.of(RegistryKeys.ITEM, Gravestones.identifier("skips_gravestones"));

    public static final SoundEvent SOUND_BLOCK_WAXED_GRAVESTONE_INTERACT_FAIL = waxedInteractFailSound();

    public static final Identifier GRAVESTONES_COLLECTED = Gravestones.identifier("gravestones_collected");

    private static Block registerAestheticGravestone(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        Block block = registerGravestone(name, factory, settings);
        Registry.register(Registries.ITEM, Gravestones.identifier(name), new AestheticGravestoneBlockItem(block, new Item.Settings()));
        return block;
    }

    private static Block registerGravestone(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        return Registry.register(Registries.BLOCK, Gravestones.identifier(name), factory.apply(settings));
    }

    private static SoundEvent waxedInteractFailSound() {
        Identifier id = Gravestones.identifier("block.gravestone.waxed_interact_fail");
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
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

        ArgumentTypeRegistry.registerArgumentType(
                Gravestones.identifier("deaths"),
                DeathArgumentType.class,
                ConstantArgumentSerializer.of(DeathArgumentType::new)
        );

        GravestonesApi.registerDataType(Gravestones.identifier("inventory"), new PlayerInventoryDataType());
        GravestonesApi.registerDataType(Gravestones.identifier("experience"), new ExperienceDataType());
        GravestonesApi.registerItemSkipPredicate((player, stack) ->
                stack.isIn(GravestonesRegistry.ITEM_SKIPS_GRAVESTONES) ||
                EnchantmentHelper.hasAnyEnchantmentsIn(stack, GravestonesRegistry.ENCHANTMENT_SKIPS_GRAVESTONES)
        );
        GravestonesApi.registerItemSkipPredicate((player, stack) ->
                EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)
        );

        addToFunctionalGroup(
                GravestonesRegistry.GRAVESTONE,
                GravestonesRegistry.GRAVESTONE_CHIPPED,
                GravestonesRegistry.GRAVESTONE_DAMAGED
        );

        Registry.register(Registries.CUSTOM_STAT, "gravestones_collected", GRAVESTONES_COLLECTED);
        Stats.CUSTOM.getOrCreateStat(GRAVESTONES_COLLECTED, StatFormatter.DEFAULT);

        PayloadTypeRegistry.playS2C().register(GravestoneEditorOpenS2CPayload.ID, GravestoneEditorOpenS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateGravestoneC2SPayload.ID, UpdateGravestoneC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateGravestoneC2SPayload.ID, (payload, context) -> {
            List<String> list = Stream.of(payload.getText()).map(Formatting::strip).collect(Collectors.toList());
            context.player().networkHandler.filterTexts(list).thenAcceptAsync(texts -> onSignUpdate(context.player(), payload, texts), context.server());
        });
    }

    @SuppressWarnings("deprecation")
    private static void onSignUpdate(ServerPlayerEntity player, UpdateGravestoneC2SPayload payload, List<FilteredMessage> signText) {
        player.updateLastActionTime();
        ServerWorld serverWorld = player.getServerWorld();
        BlockPos blockPos = payload.pos();
        if (serverWorld.isChunkLoaded(blockPos)) {
            if (!(serverWorld.getBlockEntity(blockPos) instanceof AestheticGravestoneBlockEntity blockEntity)) {
                return;
            }

            blockEntity.tryChangeText(player, signText);
        }
    }
}
