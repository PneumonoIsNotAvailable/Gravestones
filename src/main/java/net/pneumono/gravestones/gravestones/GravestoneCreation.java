package net.pneumono.gravestones.gravestones;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.*;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.ModSupport;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.content.TechnicalGravestoneBlock;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.data.GravestonePosition;
import net.pneumono.gravestones.gravestones.data.GravestoneData;
import net.pneumono.gravestones.gravestones.data.PlayerGravestoneData;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GravestoneCreation {
    public static void logger(String string) {
        logger(string, LoggerInfoType.INFO, null);
    }

    public static void logger(String string, LoggerInfoType type) {
        logger(string, type, null);
    }

    public static void logger(String string, LoggerInfoType type, Throwable t) {
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            switch (type) {
                case INFO -> Gravestones.LOGGER.info(string);
                case WARN -> Gravestones.LOGGER.warn(string);
                case ERROR -> {
                    if (t != null) {
                        Gravestones.LOGGER.error(string, t);
                    } else {
                        Gravestones.LOGGER.error(string);
                    }
                }
            }
        }
    }

    public static String posToString(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    public static void handleGravestones(PlayerEntity player) {
        logger("----- ----- Beginning Gravestone Work ----- -----");
        logger("This mostly exists for debugging purposes, but might be useful for server owners. " +
                "If you don't want to see all this every time someone dies, disable 'console_info' in the config!");

        World world = player.getWorld();
        ServerWorld serverWorld;
        if (world instanceof ServerWorld) {
            serverWorld = (ServerWorld)world;
        } else {
            return;
        }
        BlockPos playerPos = player.getBlockPos();
        String playerName = player.getName().getString();
        GameProfile playerProfile = player.getGameProfile();

        if (serverWorld.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            logger("Nevermind, keepInventory is on!");
            logger("----- ----- Ending Gravestone Work ----- -----");
            return;
        }

        BlockPos gravestonePos = GravestonePlacement.placeGravestone(world, playerPos);

        if (gravestonePos == null) {
            logger("Gravestone was not placed successfully! The items have been dropped on the floor", LoggerInfoType.ERROR);
        } else {
            String uuid = "";
            if (GravestonesConfig.CONSOLE_INFO.getValue()) {
                uuid = " (" + playerProfile.getId() + ")";
            }
            Gravestones.LOGGER.info("Placed {}'s{} Gravestone at {}", playerName, uuid, posToString(gravestonePos));

            MinecraftServer server = world.getServer();
            if (GravestonesConfig.BROADCAST_COORDINATES_IN_CHAT.getValue()) {
                server.getPlayerManager().broadcast(Text.translatable("gravestones.grave_spawned", playerName, posToString(gravestonePos)).formatted(Formatting.AQUA), false);
            }

            if (world.getBlockEntity(gravestonePos) instanceof TechnicalGravestoneBlockEntity gravestone) {
                gravestone.setGraveOwner(new ProfileComponent(playerProfile));
                gravestone.setSpawnDate(GravestoneTime.getCurrentTimeAsString(), world.getTime());
                insertPlayerItemsAndExperience(gravestone, player);
                insertModData(player, gravestone);

                world.updateListeners(gravestonePos, world.getBlockState(gravestonePos), world.getBlockState(gravestonePos), Block.NOTIFY_LISTENERS);

                logger("Gave Gravestone it's data (graveOwner, spawnDate, and inventory)");
            } else {
                logger("Gravestone position does not have a block entity!", LoggerInfoType.ERROR);
            }
        }

        List<GravestonePosition> oldGravePositions = readAndWriteData(serverWorld, playerProfile, playerName, gravestonePos);
        if (!GravestonesConfig.DECAY_WITH_DEATHS.getValue()) {
            logger("Gravestone death damage has been disabled in the config, so no graves were damaged");
        } else {
            if (oldGravePositions == null) {
                logger("No graves to damage!");
            } else {
                List<GravestonePosition> usedPositions = new ArrayList<>();
                usedPositions.add(new GravestonePosition(serverWorld.getRegistryKey().getValue(), gravestonePos));
                for (GravestonePosition oldPos : oldGravePositions) {
                    if (usedPositions.contains(oldPos)) {
                        logger("Gravestone at " + posToString(oldPos.asBlockPos()) + " in dimension " + oldPos.dimension.toString() + " has already been damaged, skipping");
                        continue;
                    }

                    ServerWorld graveWorld = serverWorld.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, oldPos.dimension));

                    if (graveWorld == null) {
                        logger("GravePosition's dimension (" + oldPos.dimension.toString() + ") does not exist!", LoggerInfoType.ERROR);
                    } else {
                        if (!graveWorld.getBlockState(oldPos.asBlockPos()).isOf(GravestonesRegistry.GRAVESTONE_TECHNICAL)) {
                            logger("No gravestone was found at the position " + posToString(oldPos.asBlockPos()) + " in dimension " + oldPos.dimension.toString()
                                    + ". Most likely this is because the grave has already been collected, or was decayed");
                        } else {

                            int deathDamage = graveWorld.getBlockState(oldPos.asBlockPos()).get(TechnicalGravestoneBlock.DEATH_DAMAGE);
                            int ageDamage = graveWorld.getBlockState(oldPos.asBlockPos()).get(TechnicalGravestoneBlock.AGE_DAMAGE);
                            String damageType;

                            String graveData = "Age: " + ageDamage + ", Death: " + deathDamage;
                            if (ageDamage + deathDamage >= 2) {
                                damageType = "broken";
                                graveWorld.breakBlock(oldPos.asBlockPos(), true);
                            } else {
                                damageType = "damaged";
                                graveWorld.setBlockState(oldPos.asBlockPos(), graveWorld.getBlockState(oldPos.asBlockPos()).with(TechnicalGravestoneBlock.DEATH_DAMAGE, deathDamage + 1));
                            }
                            logger("Gravestone (" + graveData + ") " + damageType + " at the position " + posToString(oldPos.asBlockPos()) + " in dimension " + oldPos.dimension.toString());
                        }
                    }
                    usedPositions.add(oldPos);
                }
            }
        }
        logger("----- ----- Ending Gravestone Work ----- -----");
    }

    public static void insertPlayerItemsAndExperience(TechnicalGravestoneBlockEntity gravestone, PlayerEntity player) {
        logger("Inserting Inventory items and experience into grave...");
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); ++i) {
            if (GravestonesApi.shouldSkipItem(player, inventory.getStack(i))) {
                continue;
            }

            if (!EnchantmentHelper.hasAnyEnchantmentsWith(inventory.getStack(i), EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
                gravestone.setStack(i, inventory.removeStack(i));
            } else {
                inventory.removeStack(i);
            }
        }

        logger("Items inserted!");

        if (GravestonesConfig.STORE_EXPERIENCE.getValue()) {
            int experience = GravestonesConfig.EXPERIENCE_KEPT.getValue().calculateExperienceKept(player);
            if (GravestonesConfig.EXPERIENCE_CAP.getValue() && experience > 100) {
                experience = 100;
            }

            gravestone.setExperience(experience);
            player.experienceProgress = 0;
            player.experienceLevel = 0;
            player.totalExperience = 0;

            logger("Experience inserted!");
        } else {
            logger("Experience storing is disabled!");
        }
    }

    public static void insertModData(PlayerEntity entity, TechnicalGravestoneBlockEntity gravestone) {
        logger("Inserting additional mod data into grave...");

        for (ModSupport support : GravestonesApi.getModSupports()) {
            support.insertData(entity, gravestone);
        }

        logger("Data inserted!");
    }

    private static List<GravestonePosition> readAndWriteData(ServerWorld serverWorld, GameProfile playerProfile, String playerName, BlockPos gravestonePos) {
        UUID uuid = playerProfile.getId();

        File gravestoneFile = new File(serverWorld.getServer().getSavePath(WorldSavePath.ROOT).toString(), "gravestone_data.json");
        List<GravestonePosition> posList = null;

        if (!gravestoneFile.exists()) {
            logger("No gravestone data file exists! Creating one", LoggerInfoType.WARN);
            try {
                Writer writer = Files.newBufferedWriter(gravestoneFile.toPath());
                (new GsonBuilder().serializeNulls().setPrettyPrinting().create()).toJson(new GravestoneData(), writer);
                writer.close();
            } catch (IOException e) {
                logger("Could not create gravestone data file.", LoggerInfoType.ERROR, e);
            }
        }

        try {
            Identifier dimension = serverWorld.getRegistryKey().getValue();

            logger("Reading gravestone data file");
            Reader reader = Files.newBufferedReader(gravestoneFile.toPath());
            GravestoneData data = (new GsonBuilder().setPrettyPrinting().create()).fromJson(reader, GravestoneData.class);
            reader.close();
            if (!data.hasData()) {
                logger("Gravestone data file has no data!");
            }

            logger("Updating data/creating new data");
            posList = data.getPlayerGravePositions(uuid);

            PlayerGravestoneData playerData = data.getPlayerData(uuid);
            if (playerData != null) {
                playerData.shiftGraves(new GravestonePosition(dimension, gravestonePos));
            } else {
                playerData = new PlayerGravestoneData(uuid, new GravestonePosition(dimension, gravestonePos));
                logger("Player does not have existing gravestone data, and so new data was created");
            }
            data.setPlayerData(playerData, uuid, new GravestonePosition(dimension, gravestonePos));
            logger("Data added, " + playerName + " (" + uuid + ") has a new gravestone at " + posToString(playerData.firstGrave.asBlockPos()) + " in dimension " + playerData.firstGrave.dimension.toString());

            logger("Writing updated data back to file");
            Writer writer = Files.newBufferedWriter(gravestoneFile.toPath());
            new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(data, writer);
            writer.close();
            logger("Attempting to damage previous graves");
        } catch (IOException e) {
            logger("Could not update gravestone data file!", LoggerInfoType.ERROR, e);
        }

        return posList;
    }

    public enum LoggerInfoType {
        INFO,
        WARN,
        ERROR
    }
}