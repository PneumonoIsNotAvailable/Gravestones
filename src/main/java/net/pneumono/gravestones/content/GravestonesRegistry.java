package net.pneumono.gravestones.content;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
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
import net.minecraft.world.GameRules;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.CancelGravestonePlacementCallback;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.InsertGravestoneItemCallback;
import net.pneumono.gravestones.block.*;
import net.pneumono.gravestones.networking.UpdateGravestoneC2SPayload;
import net.pneumono.pneumonocore.util.MultiVersionUtil;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//? if >=1.21.1 {
import net.minecraft.component.EnchantmentEffectComponentTypes;
//?} else if >=1.20.6 {
/*import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
*///?} else {
/*import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
*///?}

//? if >=1.20.6 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.pneumono.gravestones.networking.GravestoneEditorOpenS2CPayload;
//?}

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
            Registries.BLOCK_ENTITY_TYPE, Gravestones.id("technical_gravestone"), FabricBlockEntityTypeBuilder.create(
                    TechnicalGravestoneBlockEntity::new,
                    GRAVESTONE_TECHNICAL
            ).build()
    );
    public static BlockEntityType<AestheticGravestoneBlockEntity> AESTHETIC_GRAVESTONE_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Gravestones.id("aesthetic_gravestone"), FabricBlockEntityTypeBuilder.create(
                    AestheticGravestoneBlockEntity::new,
                    GRAVESTONE, GRAVESTONE_CHIPPED, GRAVESTONE_DAMAGED
            )/*? if >=1.21.4 {*/.canPotentiallyExecuteCommands(true)/*?}*/.build()
    );

    public static final EntityType<GravestoneSkeletonEntity> GRAVESTONE_SKELETON_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            Gravestones.id("gravestone_skeleton"),
            EntityType.Builder.<GravestoneSkeletonEntity>create(GravestoneSkeletonEntity::new, SpawnGroup.MISC)
                    //? if >=1.20.6 {
                    .dimensions(0.6F, 1.99F)
                    //?} else {
                    /*.setDimensions(0.6F, 1.99F)
                    *///?}
                    .maxTrackingRange(8)
                    //? if >=1.21.3 {
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Gravestones.id("gravestone_skeleton")))
                    //?} else {
                    /*.build("gravestone_skeleton")
                    *///?}
    );

    public static final TagKey<Item> ITEM_SKIPS_GRAVESTONES = TagKey.of(RegistryKeys.ITEM, Gravestones.id("skips_gravestones"));
    public static final TagKey<Enchantment> ENCHANTMENT_SKIPS_GRAVESTONES = TagKey.of(RegistryKeys.ENCHANTMENT, Gravestones.id("skips_gravestones"));
    public static final TagKey<Block> BLOCK_GRAVESTONE_IRREPLACEABLE = TagKey.of(RegistryKeys.BLOCK, Gravestones.id("gravestone_irreplaceable"));

    public static final SoundEvent SOUND_BLOCK_WAXED_GRAVESTONE_INTERACT_FAIL = registerSoundEvent("block.gravestone.waxed_interact_fail");
    public static final SoundEvent SOUND_BLOCK_GRAVESTONE_ADD_SKULL = registerSoundEvent("block.gravestone.add_skull");
    public static final SoundEvent SOUND_BLOCK_GRAVESTONE_REMOVE_SKULL = registerSoundEvent("block.gravestone.remove_skull");

    public static final Identifier GRAVESTONES_COLLECTED = Gravestones.id("gravestones_collected");

    private static Block registerAestheticGravestone(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        Block block = registerGravestone(name, factory, settings);
        Registry.register(Registries.ITEM, Gravestones.id(name), new AestheticGravestoneBlockItem(block,
                new Item.Settings()/*? if >=1.21.3 {*/.useBlockPrefixedTranslationKey().registryKey(RegistryKey.of(RegistryKeys.ITEM, Gravestones.id(name)))/*?}*/
        ));
        return block;
    }

    private static Block registerGravestone(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        return Registry.register(Registries.BLOCK, Gravestones.id(name),
                factory.apply(settings/*? if >=1.21.3 {*/.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Gravestones.id(name)))/*?}*/)
        );
    }

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Gravestones.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerModContent() {
        FabricDefaultAttributeRegistry.register(GRAVESTONE_SKELETON_ENTITY_TYPE,
                GravestoneSkeletonEntity.createAbstractSkeletonAttributes()
        );

        ArgumentTypeRegistry.registerArgumentType(
                Gravestones.id("deaths"),
                DeathArgumentType.class,
                ConstantArgumentSerializer.of(DeathArgumentType::new)
        );

        registerAPIUsages();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            content.add(GRAVESTONE);
            content.add(GRAVESTONE_CHIPPED);
            content.add(GRAVESTONE_DAMAGED);
        });

        Registry.register(Registries.CUSTOM_STAT, "gravestones_collected", GRAVESTONES_COLLECTED);
        Stats.CUSTOM.getOrCreateStat(GRAVESTONES_COLLECTED, StatFormatter.DEFAULT);

        //? if >=1.20.6 {
        PayloadTypeRegistry.playS2C().register(GravestoneEditorOpenS2CPayload.PAYLOAD_ID, GravestoneEditorOpenS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateGravestoneC2SPayload.PAYLOAD_ID, UpdateGravestoneC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateGravestoneC2SPayload.PAYLOAD_ID, (payload, context) -> {
            List<String> list = Stream.of(payload.getText()).map(Formatting::strip).collect(Collectors.toList());
            context.player().networkHandler.filterTexts(list).thenAcceptAsync(texts ->
                    onSignUpdate(context.player(), payload, texts), context.server()
            );
        });
        //?} else {
        /*ServerPlayNetworking.registerGlobalReceiver(UpdateGravestoneC2SPayload.ID, (server, player, handler, buf, sender) -> {
            UpdateGravestoneC2SPayload payload = UpdateGravestoneC2SPayload.read(buf);

            List<String> list = Stream.of(payload.getText()).map(Formatting::strip).collect(Collectors.toList());
            player.networkHandler.filterTexts(list).thenAcceptAsync(texts ->
                    onSignUpdate(player, payload, texts), server
            );
        });
        *///?}
    }

    private static void registerAPIUsages() {
        GravestonesApi.registerDataType(Gravestones.id("inventory"), new PlayerInventoryDataType());
        GravestonesApi.registerDataType(Gravestones.id("experience"), new ExperienceDataType());

        InsertGravestoneItemCallback.EVENT.register((player, itemStack) ->
                itemStack.isIn(ITEM_SKIPS_GRAVESTONES) ||
                //? if >=1.21.1 {
                EnchantmentHelper.hasAnyEnchantmentsIn(itemStack, ENCHANTMENT_SKIPS_GRAVESTONES)
                //?} else {
                /*hasSkippableEnchantments(itemStack)
                *///?}
        );
        InsertGravestoneItemCallback.EVENT.register((player, itemStack) ->
                //? if >=1.21.1 {
                EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)
                //?} else {
                /*EnchantmentHelper.hasVanishingCurse(itemStack)
                *///?}
        );

        CancelGravestonePlacementCallback.EVENT.register((world, player, deathPos) ->
                world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) && !GravestonesConfig.SPAWN_GRAVESTONES_WITH_KEEPINV.getValue()
        );
        CancelGravestonePlacementCallback.EVENT.register((world, player, deathPos) ->
                player.isCreative() && !GravestonesConfig.SPAWN_GRAVESTONES_IN_CREATIVE.getValue()
        );
    }

    //? if <1.20.6 {
    /*@SuppressWarnings("deprecation")
    *///?}
    //? if <1.21.1 {
    /*private static boolean hasSkippableEnchantments(ItemStack stack) {
        //? if >=1.20.6 {
        ItemEnchantmentsComponent component = stack.getEnchantments();
        for (RegistryEntry<Enchantment> enchantment : component.getEnchantments()) {
            if (enchantment.isIn(ENCHANTMENT_SKIPS_GRAVESTONES)) {
                return true;
            }
        }
        return false;
        //?} else {
        /^for (RegistryEntry<Enchantment> enchantment : EnchantmentHelper.get(stack).keySet().stream()
                .map(Registries.ENCHANTMENT::getEntry)
                .toList()
        ) {
            if (enchantment.isIn(ENCHANTMENT_SKIPS_GRAVESTONES)) {
                return true;
            }
        }
        return false;
        ^///?}
    }
    *///?}

    @SuppressWarnings("deprecation")
    private static void onSignUpdate(ServerPlayerEntity player, UpdateGravestoneC2SPayload payload, List<FilteredMessage> signText) {
        player.updateLastActionTime();
        ServerWorld serverWorld = (ServerWorld) MultiVersionUtil.getWorld(player);
        BlockPos blockPos = payload.pos();
        if (serverWorld.isChunkLoaded(blockPos)) {
            if (!(serverWorld.getBlockEntity(blockPos) instanceof AestheticGravestoneBlockEntity blockEntity)) {
                return;
            }

            blockEntity.tryChangeText(player, signText);
        }
    }
}
