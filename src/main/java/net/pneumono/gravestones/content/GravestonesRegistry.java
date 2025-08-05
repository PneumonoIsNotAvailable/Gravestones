package net.pneumono.gravestones.content;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
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
import net.minecraft.world.World;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.CancelGravestonePlacementCallback;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.InsertGravestoneItemCallback;
import net.pneumono.gravestones.block.*;
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
            Registries.BLOCK_ENTITY_TYPE, Gravestones.id("technical_gravestone"), FabricBlockEntityTypeBuilder.create(
                    TechnicalGravestoneBlockEntity::new,
                    GRAVESTONE_TECHNICAL
            ).build()
    );
    public static BlockEntityType<AestheticGravestoneBlockEntity> AESTHETIC_GRAVESTONE_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Gravestones.id("aesthetic_gravestone"), FabricBlockEntityTypeBuilder.create(
                    AestheticGravestoneBlockEntity::new,
                    GRAVESTONE, GRAVESTONE_CHIPPED, GRAVESTONE_DAMAGED
            ).build()
    );

    public static final EntityType<GravestoneSkeletonEntity> GRAVESTONE_SKELETON_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            Gravestones.id("gravestone_skeleton"),
            FabricEntityTypeBuilder.<GravestoneSkeletonEntity>create(SpawnGroup.MISC, GravestoneSkeletonEntity::new)
                    .dimensions(new EntityDimensions(0.6F, 1.99F, true))
                    .trackRangeBlocks(8)
                    .build()
    );

    public static final TagKey<Item> ITEM_SKIPS_GRAVESTONES = TagKey.of(RegistryKeys.ITEM, Gravestones.id("skips_gravestones"));
    public static final TagKey<Block> BLOCK_GRAVESTONE_IRREPLACEABLE = TagKey.of(RegistryKeys.BLOCK, Gravestones.id("gravestone_irreplaceable"));

    public static final SoundEvent SOUND_BLOCK_WAXED_GRAVESTONE_INTERACT_FAIL = waxedInteractFailSound();

    public static final Identifier GRAVESTONES_COLLECTED = Gravestones.id("gravestones_collected");

    private static Block registerAestheticGravestone(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        Block block = registerGravestone(name, factory, settings);
        Registry.register(Registries.ITEM, Gravestones.id(name), new AestheticGravestoneBlockItem(block, new Item.Settings()));
        return block;
    }

    private static Block registerGravestone(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        return Registry.register(Registries.BLOCK, Gravestones.id(name), factory.apply(settings));
    }

    private static SoundEvent waxedInteractFailSound() {
        Identifier id = Gravestones.id("block.gravestone.waxed_interact_fail");
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

        ServerPlayNetworking.registerGlobalReceiver(UpdateGravestoneC2SPayload.ID, (server, player, handler, buf, sender) -> {
            UpdateGravestoneC2SPayload payload = UpdateGravestoneC2SPayload.fromBuf(buf);
            List<String> list = Stream.of(payload.getText()).map(Formatting::strip).collect(Collectors.toList());
            handler.filterTexts(list).thenAcceptAsync(texts ->
                    onSignUpdate(player, payload, texts), server
            );
        });
    }

    private static void registerAPIUsages() {
        GravestonesApi.registerDataType(Gravestones.id("inventory"), new PlayerInventoryDataType());
        GravestonesApi.registerDataType(Gravestones.id("experience"), new ExperienceDataType());

        InsertGravestoneItemCallback.EVENT.register((player, itemStack) ->
                itemStack.isIn(GravestonesRegistry.ITEM_SKIPS_GRAVESTONES) ||
                hasEnchantment(Identifier.of("alessandrvenchantments", "soulbound"), itemStack) ||
                hasEnchantment(Identifier.of("enchantery", "soulbound"), itemStack) ||
                hasEnchantment(Identifier.of("enderzoology", "soulbound"), itemStack) ||
                hasEnchantment(Identifier.of("soulbound", "soulbound"), itemStack) ||
                hasEnchantment(Identifier.of("soulbound_enchantment", "soulbound"), itemStack)
        );
        InsertGravestoneItemCallback.EVENT.register((player, itemStack) ->
                EnchantmentHelper.getLevel(Enchantments.VANISHING_CURSE, itemStack) > 0
        );

        CancelGravestonePlacementCallback.EVENT.register((world, player, deathPos) ->
                world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)
        );
    }

    public static boolean hasEnchantment(Identifier identifier, ItemStack stack) {
        if (stack.isEmpty()) return false;

        NbtList nbtList = stack.getEnchantments();

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            Identifier identifier2 = EnchantmentHelper.getIdFromNbt(nbtCompound);
            if (identifier2 != null && identifier2.equals(identifier)) {
                return EnchantmentHelper.getLevelFromNbt(nbtCompound) > 0;
            }
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    private static void onSignUpdate(ServerPlayerEntity player, UpdateGravestoneC2SPayload payload, List<FilteredMessage> signText) {
        World world = player.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) return;

        player.updateLastActionTime();
        BlockPos blockPos = payload.pos();
        if (serverWorld.isChunkLoaded(blockPos)) {
            if (!(serverWorld.getBlockEntity(blockPos) instanceof AestheticGravestoneBlockEntity blockEntity)) {
                return;
            }

            blockEntity.tryChangeText(player, signText);
        }
    }
}
