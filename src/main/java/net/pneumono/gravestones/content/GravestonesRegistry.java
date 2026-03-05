package net.pneumono.gravestones.content;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.block.*;
import net.pneumono.gravestones.networking.UpdateGravestoneC2SPayload;
import net.pneumono.pneumonocore.config_api.registry.ServerConfigCommandRegistry;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//? if >=1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.pneumono.gravestones.networking.GravestoneEditorOpenS2CPayload;
//?}

@SuppressWarnings("deprecation")
public class GravestonesRegistry {
    public static final Block GRAVESTONE_TECHNICAL = registerGravestone("gravestone_technical",
            TechnicalGravestoneBlock::new, copy(Blocks.STONE).strength(-1.0F, 3600000.0F).noOcclusion());
    public static final Block GRAVESTONE = registerAestheticGravestone("gravestone",
            AestheticGravestoneBlock::new, copy(Blocks.STONE).strength(3.5F).noOcclusion().requiresCorrectToolForDrops());
    public static final Block GRAVESTONE_CHIPPED = registerAestheticGravestone("gravestone_chipped",
            AestheticGravestoneBlock::new, copy(Blocks.STONE).strength(3.5F).noOcclusion().requiresCorrectToolForDrops());
    public static final Block GRAVESTONE_DAMAGED = registerAestheticGravestone("gravestone_damaged",
            AestheticGravestoneBlock::new, copy(Blocks.STONE).strength(3.5F).noOcclusion().requiresCorrectToolForDrops());

    public static BlockEntityType<TechnicalGravestoneBlockEntity> TECHNICAL_GRAVESTONE_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Gravestones.id("technical_gravestone"), FabricBlockEntityTypeBuilder.create(
                    TechnicalGravestoneBlockEntity::new,
                    GRAVESTONE_TECHNICAL
            ).build()
    );
    public static BlockEntityType<AestheticGravestoneBlockEntity> AESTHETIC_GRAVESTONE_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Gravestones.id("aesthetic_gravestone"), FabricBlockEntityTypeBuilder.create(
                    AestheticGravestoneBlockEntity::new,
                    GRAVESTONE, GRAVESTONE_CHIPPED, GRAVESTONE_DAMAGED
            )/*? if >=1.21.4 {*/.canPotentiallyExecuteCommands(true)/*?}*/.build()
    );

    public static final EntityType<GravestoneSkeletonEntity> GRAVESTONE_SKELETON_ENTITY_TYPE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Gravestones.id("gravestone_skeleton"),
            EntityType.Builder.<GravestoneSkeletonEntity>of(GravestoneSkeletonEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.99F)
                    .clientTrackingRange(8)
                    //? if >=1.21.2 {
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Gravestones.id("gravestone_skeleton")))
                    //?} else {
                    /*.build("gravestone_skeleton")
                    *///?}
    );

    /**
     * @deprecated Use {@link net.pneumono.gravestones.api.GravestonesApi#ITEM_SKIPS_GRAVESTONES ITEM_SKIPS_GRAVESTONES} instead
     */
    @Deprecated
    public static final TagKey<Item> ITEM_SKIPS_GRAVESTONES = TagKey.create(Registries.ITEM, Gravestones.id("skips_gravestones"));
    /**
     * @deprecated Use {@link net.pneumono.gravestones.api.GravestonesApi#ENCHANTMENT_SKIPS_GRAVESTONES ENCHANTMENT_SKIPS_GRAVESTONES} instead
     */
    @Deprecated
    public static final TagKey<Enchantment> ENCHANTMENT_SKIPS_GRAVESTONES = TagKey.create(Registries.ENCHANTMENT, Gravestones.id("skips_gravestones"));
    /**
     * @deprecated Use {@link net.pneumono.gravestones.api.GravestonesApi#BLOCK_GRAVESTONE_IRREPLACEABLE BLOCK_GRAVESTONE_IRREPLACEABLE} instead
     */
    @Deprecated
    public static final TagKey<Block> BLOCK_GRAVESTONE_IRREPLACEABLE = TagKey.create(Registries.BLOCK, Gravestones.id("gravestone_irreplaceable"));

    public static final SoundEvent SOUND_BLOCK_WAXED_GRAVESTONE_INTERACT_FAIL = registerSoundEvent("block.gravestone.waxed_interact_fail");
    public static final SoundEvent SOUND_BLOCK_GRAVESTONE_ADD_SKULL = registerSoundEvent("block.gravestone.add_skull");
    public static final SoundEvent SOUND_BLOCK_GRAVESTONE_REMOVE_SKULL = registerSoundEvent("block.gravestone.remove_skull");

    public static final Identifier GRAVESTONES_COLLECTED = Gravestones.id("gravestones_collected");

    private static Block registerAestheticGravestone(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        Block block = registerGravestone(name, factory, properties);
        Registry.register(BuiltInRegistries.ITEM, Gravestones.id(name), new AestheticGravestoneBlockItem(block,
                new Item.Properties()/*? if >=1.21.2 {*/.useBlockDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, Gravestones.id(name)))/*?}*/
        ));
        return block;
    }

    private static Block registerGravestone(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        return Registry.register(BuiltInRegistries.BLOCK, Gravestones.id(name),
                factory.apply(properties/*? if >=1.21.2 {*/.setId(ResourceKey.create(Registries.BLOCK, Gravestones.id(name)))/*?}*/)
        );
    }

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Gravestones.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }
    private static BlockBehaviour.Properties copy(Block block) {
        //? if >=1.20.3 {
        return BlockBehaviour.Properties.ofFullCopy(block);
        //?} else {
        /*return BlockBehaviour.Properties.copy(block);
        *///?}
    }

    public static void registerModContent() {
        ServerConfigCommandRegistry.registerServerConfigCommand(Gravestones.MOD_ID, "gravestonesconfig");

        FabricDefaultAttributeRegistry.register(GRAVESTONE_SKELETON_ENTITY_TYPE,
                GravestoneSkeletonEntity.createAttributes()
        );

        ArgumentTypeRegistry.registerArgumentType(
                Gravestones.id("deaths"),
                DeathArgumentType.class,
                SingletonArgumentInfo.contextFree(DeathArgumentType::new)
        );

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {
            content.accept(GRAVESTONE);
            content.accept(GRAVESTONE_CHIPPED);
            content.accept(GRAVESTONE_DAMAGED);
        });

        Registry.register(BuiltInRegistries.CUSTOM_STAT, "gravestones_collected", GRAVESTONES_COLLECTED);
        Stats.CUSTOM.get(GRAVESTONES_COLLECTED, StatFormatter.DEFAULT);

        //? if >=1.20.5 {
        PayloadTypeRegistry.playS2C().register(GravestoneEditorOpenS2CPayload.PAYLOAD_ID, GravestoneEditorOpenS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateGravestoneC2SPayload.PAYLOAD_ID, UpdateGravestoneC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateGravestoneC2SPayload.PAYLOAD_ID, (payload, context) -> {
            List<String> list = Stream.of(payload.getText()).map(ChatFormatting::stripFormatting).collect(Collectors.toList());
            context.player().connection.filterTextPacket(list).thenAcceptAsync(texts ->
                    onSignUpdate(context.player(), payload, texts),
                    /*? if =1.20.5 {*//*context.player().getServer()*//*?} else {*/context.server()/*?}*/
            );
        });
        //?} else {
        /*ServerPlayNetworking.registerGlobalReceiver(UpdateGravestoneC2SPayload.ID, (server, player, handler, buf, sender) -> {
            UpdateGravestoneC2SPayload payload = UpdateGravestoneC2SPayload.read(buf);

            List<String> list = Stream.of(payload.getText()).map(ChatFormatting::stripFormatting).collect(Collectors.toList());
            player.connection.filterTextPacket(list).thenAcceptAsync(texts ->
                    onSignUpdate(player, payload, texts), server
            );
        });
        *///?}
    }

    @SuppressWarnings("deprecation")
    private static void onSignUpdate(ServerPlayer player, UpdateGravestoneC2SPayload payload, List<FilteredText> signText) {
        player.resetLastActionTime();
        ServerLevel level = (ServerLevel) player.level();
        BlockPos pos = payload.pos();
        if (level.hasChunkAt(pos)) {
            if (!(level.getBlockEntity(pos) instanceof AestheticGravestoneBlockEntity blockEntity)) {
                return;
            }

            blockEntity.tryChangeText(player, signText);
        }
    }
}
