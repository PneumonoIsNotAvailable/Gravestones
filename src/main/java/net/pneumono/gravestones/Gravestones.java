package net.pneumono.gravestones;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.content.ModContent;
import net.pneumono.gravestones.content.entity.GravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.GravestoneData;
import net.pneumono.gravestones.gravestones.GravestonePosition;
import net.pneumono.pneumonocore.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Gravestones implements ModInitializer {
	public static final String MOD_ID = "gravestones";
	public static final Logger LOGGER = LoggerFactory.getLogger("Gravestones");
	public static final BooleanConfiguration GRAVESTONES_DECAY_WITH_TIME = Configs.register(new BooleanConfiguration(MOD_ID, "gravestones_decay_with_time", ConfigEnv.SERVER, true, MOD_ID + ".configs.gravestones_decay_with_time.tooltip"));
	public static final BooleanConfiguration GRAVESTONES_DECAY_WITH_DEATHS = Configs.register(new BooleanConfiguration(MOD_ID, "gravestones_decay_with_deaths", ConfigEnv.SERVER, true, MOD_ID + ".configs.gravestones_decay_with_deaths.tooltip"));
	public static final IntegerConfiguration GRAVESTONE_DECAY_TIME_HOURS = Configs.register(new IntegerConfiguration(MOD_ID, "gravestone_decay_time_hours",ConfigEnv.SERVER, 0, 100, 8, MOD_ID + ".configs.gravestone_decay_time_hours.tooltip"));
	public static final BooleanConfiguration GRAVESTONE_ACCESSIBLE_OWNER_ONLY = Configs.register(new BooleanConfiguration(MOD_ID, "gravestone_accessible_owner_only", ConfigEnv.SERVER, true, MOD_ID + ".configs.gravestone_accessible_owner_only.tooltip"));
	public static final BooleanConfiguration SPAWN_GRAVESTONE_SKELETONS = Configs.register(new BooleanConfiguration(MOD_ID, "spawn_gravestone_skeletons", ConfigEnv.SERVER, false, MOD_ID + ".configs.spawn_gravestone_skeletons.tooltip"));
	public static final BooleanConfiguration BROADCAST_COLLECT_IN_CHAT = Configs.register(new BooleanConfiguration(MOD_ID, "broadcast_collect_in_chat", ConfigEnv.SERVER, false, MOD_ID + ".configs.broadcast_collect_in_chat.tooltip"));
	public static final BooleanConfiguration BROADCAST_COORDINATES_IN_CHAT = Configs.register(new BooleanConfiguration(MOD_ID, "broadcast_coordinates_in_chat", ConfigEnv.SERVER, false, MOD_ID + ".configs.broadcast_coordinates_in_chat.tooltip"));
	public static final BooleanConfiguration CONSOLE_INFO = Configs.register(new BooleanConfiguration(MOD_ID, "gravestone_console_info", ConfigEnv.CLIENT, false, MOD_ID + ".configs.gravestone_console_info.tooltip"));
	public static final StringConfiguration TIME_FORMAT = Configs.register(new StringConfiguration(MOD_ID, "time_format", ConfigEnv.CLIENT, "MM/dd/yyyy HH:mm:ss", MOD_ID + ".configs.time_format.tooltip"));

	public static final TagKey<Block> GRAVESTONE_IRREPLACEABLE = TagKey.of(RegistryKeys.BLOCK, new Identifier(MOD_ID, "gravestone_irreplaceable"));
	public static final TagKey<Block> AESTHETIC_GRAVESTONES = TagKey.of(RegistryKeys.BLOCK, new Identifier(MOD_ID, "aesthetic_gravestones"));

	public static final Identifier GRAVESTONES_COLLECTED = new Identifier(MOD_ID, "gravestones_collected");

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Gravestones");
		Configs.reload(MOD_ID);

		ModContent.registerModContent();
		registerCommands();

		Registry.register(Registries.CUSTOM_STAT, "gravestones_collected", GRAVESTONES_COLLECTED);
		Stats.CUSTOM.getOrCreateStat(GRAVESTONES_COLLECTED, StatFormatter.DEFAULT);

		addToVanillaGroup(ItemGroups.BUILDING_BLOCKS,
				ModContent.GRAVESTONE_DEFAULT,
				ModContent.GRAVESTONE_CHIPPED,
				ModContent.GRAVESTONE_DAMAGED
		);

		addToVanillaGroup(ItemGroups.OPERATOR,
				ModContent.GRAVESTONE_TECHNICAL
		);
	}

	private static void addToVanillaGroup(RegistryKey<ItemGroup> group, ItemConvertible... items) {
		ItemGroupEvents.modifyEntriesEvent(group).register((content) -> {
			for (ItemConvertible item : items) {
				content.add(item);
			}
		});
	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("gravestones")
				.requires(source -> source.hasPermissionLevel(4))
				.then(literal("getdata")
					.then(literal("gravestone")
						.then(argument("position", BlockPosArgumentType.blockPos())
							.executes(context -> {
								World world = context.getSource().getWorld();
								BlockPos pos = BlockPosArgumentType.getBlockPos(context, "position");

								if (world.getBlockState(pos).isIn(AESTHETIC_GRAVESTONES)) {
									context.getSource().sendMessage(Text.literal("Haha, see what you did there. No gravestone *with gravestone data* at that position!").formatted(Formatting.RED));
								} else if (!(world.getBlockState(pos).isOf(ModContent.GRAVESTONE_TECHNICAL))) {
									context.getSource().sendMessage(Text.literal("No gravestone at that position!").formatted(Formatting.RED));
								} else if (world.getBlockEntity(pos) instanceof GravestoneBlockEntity entity){
									GameProfile owner = entity.getGraveOwner();
									if (owner != null) {
										context.getSource().sendMessage(Text.literal("Gravestone has a spawnDate of " + entity.getSpawnDate() + " and a graveOwner of " + owner.getName() + ", " + owner.getId().toString()).formatted(Formatting.GREEN));
									} else {
										context.getSource().sendMessage(Text.literal("Gravestone has a spawnDate of " + entity.getSpawnDate() + " but no graveOwner!").formatted(Formatting.RED));
									}

									StringBuilder itemMessage = new StringBuilder();
									for (ItemStack item : entity.getInventoryAsItemList()) {
										itemMessage.append(", ").append(item.toString());
									}

									context.getSource().sendMessage(Text.literal("Gravestone has the following items" + itemMessage).formatted(Formatting.GOLD));
								}
								return 1;
							})
						)
					)
					.then(literal("player")
						.then(argument("player", EntityArgumentType.player())
							.executes(context -> {
								File gravestoneFile = new File(context.getSource().getWorld().getServer().getSavePath(WorldSavePath.ROOT).toString(), "gravestone_data.json");

								try {
									if (gravestoneFile.exists()) {
										Reader reader = Files.newBufferedReader(gravestoneFile.toPath());
										GravestoneData data = new GsonBuilder().serializeNulls().setPrettyPrinting().create().fromJson(reader, GravestoneData.class);
										reader.close();

										List<GravestonePosition> positions = data.getPlayerGravePositions(EntityArgumentType.getPlayer(context, "player").getUuid());
										StringBuilder posList = new StringBuilder();
										for (GravestonePosition pos : positions) {
											posList.append("(").append(pos.posX).append(",").append(pos.posY).append(",").append(pos.posZ).append(") in ").append(pos.dimension.toString()).append(", ");
										}
										context.getSource().sendMessage(Text.literal("" + EntityArgumentType.getPlayer(context, "player").getDisplayName().getString() + " has graves at the following locations: " + posList));
									} else {
										LOGGER.error("Could not find gravestone data file.");
										context.getSource().sendMessage(Text.literal("Could not find gravestone data file.").formatted(Formatting.RED));
									}
								} catch (IOException e) {
									LOGGER.error("Could not read gravestone data file!", e);
									context.getSource().sendMessage(Text.literal("Could not read gravestone data file!").formatted(Formatting.RED));
								}

								return 1;
							})
						)
					)
				)
			);
		});
	}
}