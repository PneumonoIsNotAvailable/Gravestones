package net.pneumono.gravestones.gravestones;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.*;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.registry.DynamicRegistryManager;
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
import java.nio.file.Path;
import java.util.*;

public class GravestoneCreation {
    public static void info(String string) {
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            Gravestones.LOGGER.info(string);
        }
    }

    public static void warn(String string) {
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            Gravestones.LOGGER.warn(string);
        }
    }

    public static void error(String string) {
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            Gravestones.LOGGER.error(string);
        }
    }

    public static void error(String string, Throwable t) {
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            Gravestones.LOGGER.error(string, t);
        }
    }

    public static String posToString(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    public static void handleGravestones(PlayerEntity player) {
        info("----- ----- Beginning Gravestone Work ----- -----");
        info("This mostly exists for debugging purposes, but might be useful for server owners. " +
                "If you don't want to see all this every time someone dies, disable 'console_info' in the config!");

        World world = player.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }
        BlockPos playerPos = player.getBlockPos();
        String playerName = player.getName().getString();
        GameProfile playerProfile = player.getGameProfile();

        if (serverWorld.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            info("Nevermind, keepInventory is on!");
            info("----- ----- Ending Gravestone Work ----- -----");
            return;
        }

        BlockPos gravestonePos = GravestonePlacement.placeGravestone(world, playerPos);

        if (gravestonePos == null) {
            error("Gravestone was not placed successfully! The items have been dropped on the floor");
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
                Date date = new Date();
                gravestone.setSpawnDate(GravestoneTime.READABLE.format(date), world.getTime());
                insertPlayerItemsAndExperience(gravestone, player);
                insertModData(player, gravestone);

                recordDeathData(gravestone, player, date);

                world.updateListeners(gravestonePos, world.getBlockState(gravestonePos), world.getBlockState(gravestonePos), Block.NOTIFY_LISTENERS);

                info("Gave Gravestone it's data (graveOwner, spawnDate, and inventory)");
            } else {
                error("Gravestone position does not have a block entity!");
            }
        }

        List<GravestonePosition> oldGravePositions = readAndWriteData(serverWorld, playerProfile, playerName, gravestonePos);
        if (!GravestonesConfig.DECAY_WITH_DEATHS.getValue()) {
            info("Gravestone death damage has been disabled in the config, so no graves were damaged");
        } else {
            if (oldGravePositions == null) {
                info("No graves to damage!");
            } else {
                List<GravestonePosition> usedPositions = new ArrayList<>();
                usedPositions.add(new GravestonePosition(serverWorld.getRegistryKey().getValue(), gravestonePos));
                for (GravestonePosition oldPos : oldGravePositions) {
                    if (usedPositions.contains(oldPos)) {
                        info("Gravestone at " + posToString(oldPos.asBlockPos()) + " in dimension " + oldPos.dimension.toString() + " has already been damaged, skipping");
                        continue;
                    }

                    ServerWorld graveWorld = serverWorld.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, oldPos.dimension));

                    if (graveWorld == null) {
                        error("GravePosition's dimension (" + oldPos.dimension.toString() + ") does not exist!");
                    } else {
                        if (!graveWorld.getBlockState(oldPos.asBlockPos()).isOf(GravestonesRegistry.GRAVESTONE_TECHNICAL)) {
                            info("No gravestone was found at the position " + posToString(oldPos.asBlockPos()) + " in dimension " + oldPos.dimension.toString()
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
                            info("Gravestone (" + graveData + ") " + damageType + " at the position " + posToString(oldPos.asBlockPos()) + " in dimension " + oldPos.dimension.toString());
                        }
                    }
                    usedPositions.add(oldPos);
                }
            }
        }
        info("----- ----- Ending Gravestone Work ----- -----");
    }

    public static void insertPlayerItemsAndExperience(TechnicalGravestoneBlockEntity gravestone, PlayerEntity player) {
        info("Inserting Inventory items and experience into grave...");
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (shouldSkipItem(player, stack)) {
                continue;
            }

            gravestone.setStack(i, inventory.removeStack(i));
        }

        info("Items inserted!");

        if (GravestonesConfig.STORE_EXPERIENCE.getValue()) {
            int experience = GravestonesConfig.EXPERIENCE_KEPT.getValue().calculateExperienceKept(player);
            if (GravestonesConfig.EXPERIENCE_CAP.getValue() && experience > 100) {
                experience = 100;
            }

            gravestone.setExperience(experience);
            player.experienceProgress = 0;
            player.experienceLevel = 0;
            player.totalExperience = 0;

            info("Experience inserted!");
        } else {
            info("Experience storing is disabled!");
        }
    }

    public static boolean shouldSkipItem(PlayerEntity player, ItemStack stack) {
        return GravestonesApi.shouldSkipItem(player, stack) ||
                stack.isIn(GravestonesRegistry.ITEM_SKIPS_GRAVESTONES) ||
                EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP) ||
                EnchantmentHelper.hasAnyEnchantmentsIn(stack, GravestonesRegistry.ENCHANTMENT_SKIPS_GRAVESTONES);
    }

    public static void insertModData(PlayerEntity entity, TechnicalGravestoneBlockEntity gravestone) {
        info("Inserting additional mod data into grave...");

        for (ModSupport support : GravestonesApi.getModSupports()) {
            support.insertData(entity, gravestone);
        }

        info("Data inserted!");
    }

    private static void recordDeathData(TechnicalGravestoneBlockEntity gravestone, PlayerEntity player, Date date) {
        File deathsFile = new File(
                Objects.requireNonNull(player.getServer()).getSavePath(WorldSavePath.ROOT).toString(),
                "gravestones/" + player.getUuidAsString()
        );
        if (deathsFile.mkdirs()) {
            info("No gravestone death data file exists for " + player.getUuidAsString() + ", creating one");
        }
        Path path = deathsFile.toPath().resolve(GravestoneTime.FILE_SAVING.format(date) + ".dat");

        NbtCompound deathData = new NbtCompound();
        DynamicRegistryManager registries = Objects.requireNonNull(gravestone.getWorld()).getRegistryManager();
        Inventories.writeNbt(deathData, gravestone.getItems(), registries);
        deathData.putInt("experience", gravestone.getExperience());
        deathData.put("modData", gravestone.getAllModData());

        try {
            NbtIo.writeCompressed(deathData, path);
        } catch (IOException e) {
            error("Could not save gravestones death data", e);
        }
    }

    private static List<GravestonePosition> readAndWriteData(ServerWorld serverWorld, GameProfile playerProfile, String playerName, BlockPos gravestonePos) {
        UUID uuid = playerProfile.getId();

        File gravestoneFile = new File(serverWorld.getServer().getSavePath(WorldSavePath.ROOT).toString(), "gravestone_data.json");
        List<GravestonePosition> posList = null;

        if (!gravestoneFile.exists()) {
            warn("No gravestone data file exists! Creating one");
            try {
                Writer writer = Files.newBufferedWriter(gravestoneFile.toPath());
                (new GsonBuilder().serializeNulls().setPrettyPrinting().create()).toJson(new GravestoneData(), writer);
                writer.close();
            } catch (IOException e) {
                error("Could not create gravestone data file", e);
            }
        }

        try {
            Identifier dimension = serverWorld.getRegistryKey().getValue();

            info("Reading gravestone data file");
            Reader reader = Files.newBufferedReader(gravestoneFile.toPath());
            GravestoneData data = (new GsonBuilder().setPrettyPrinting().create()).fromJson(reader, GravestoneData.class);
            reader.close();
            if (!data.hasData()) {
                info("Gravestone data file has no data!");
            }

            info("Updating data/creating new data");
            posList = data.getPlayerGravePositions(uuid);

            PlayerGravestoneData playerData = data.getPlayerData(uuid);
            if (playerData != null) {
                playerData.shiftGraves(new GravestonePosition(dimension, gravestonePos));
            } else {
                playerData = new PlayerGravestoneData(uuid, new GravestonePosition(dimension, gravestonePos));
                info("Player does not have existing gravestone data, and so new data was created");
            }
            data.setPlayerData(playerData, uuid, new GravestonePosition(dimension, gravestonePos));
            info("Data added, " + playerName + " (" + uuid + ") has a new gravestone at " + posToString(playerData.firstGrave.asBlockPos()) + " in dimension " + playerData.firstGrave.dimension.toString());

            info("Writing updated data back to file");
            Writer writer = Files.newBufferedWriter(gravestoneFile.toPath());
            new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(data, writer);
            writer.close();
            info("Attempting to damage previous graves");
        } catch (IOException e) {
            error("Could not update gravestone data file", e);
        }

        return posList;
    }
}