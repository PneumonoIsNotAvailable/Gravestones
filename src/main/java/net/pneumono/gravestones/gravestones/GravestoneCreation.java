package net.pneumono.gravestones.gravestones;


import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
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
import net.minecraft.world.dimension.DimensionTypes;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.content.GravestonesContent;
import net.pneumono.gravestones.content.TechnicalGravestoneBlock;
import net.pneumono.gravestones.content.entity.GravestoneBlockEntity;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GravestoneCreation {
    private static void logger(String string) {
        logger(string, LoggerInfoType.INFO, null);
    }

    private static void logger(String string, LoggerInfoType type) {
        logger(string, type, null);
    }

    private static void logger(String string, LoggerInfoType type, Throwable t) {
        if (Gravestones.CONSOLE_INFO.getValue()) {
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

    private enum LoggerInfoType {
        INFO,
        WARN,
        ERROR
    }

    public static String posToString(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    public static void handleGravestones(PlayerEntity player) {
        logger("----- ----- Beginning Gravestone Work ----- -----");
        logger("If you don't want to see all this every time someone dies, disable 'gravestoneInfoInConsole' in the config!");
        World world = player.getWorld();
        if (!world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            BlockPos playerPos = player.getBlockPos();

            BlockPos gravestonePos = placeGravestone(world, playerPos);

            if (gravestonePos == null) {
                logger("Gravestone was not placed successfully! The items have been dropped on the floor", LoggerInfoType.ERROR);
            } else {
                logger("Placed " + player.getName().getString() + "'s (" + player.getGameProfile().getId() + ") Gravestone at " + posToString(gravestonePos));

                MinecraftServer server = world.getServer();
                if (server != null && Gravestones.BROADCAST_COORDINATES_IN_CHAT.getValue()) {
                    server.getPlayerManager().broadcast(Text.translatable("gravestones.grave_spawned", player.getName().getString(), posToString(gravestonePos)).formatted(Formatting.AQUA), false);
                }

                if (world.getBlockEntity(gravestonePos) instanceof GravestoneBlockEntity gravestone) {
                    gravestone.setGraveOwner(player.getGameProfile());
                    gravestone.setSpawnDate(GravestoneTime.getCurrentTimeAsString());
                    insertPlayerItems(gravestone, player);
                    world.updateListeners(gravestonePos, world.getBlockState(gravestonePos), world.getBlockState(gravestonePos), Block.NOTIFY_LISTENERS);

                    logger("Gave Gravestone it's data (graveOwner, spawnDate, and inventory)");
                } else {
                    logger("Gravestone position does not have a block entity!", LoggerInfoType.ERROR);
                }
            }

            if (world instanceof ServerWorld serverWorld) {
                List<GravestonePosition> oldGravePositions = readAndWriteData(serverWorld, player, gravestonePos);
                if (Gravestones.GRAVESTONES_DECAY_WITH_DEATHS.getValue()) {
                    if (oldGravePositions != null) {
                        for (GravestonePosition oldPos : oldGravePositions) {
                            ServerWorld graveWorld = serverWorld.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, oldPos.dimension));
                            if (graveWorld != null) {
                                if (graveWorld.getBlockState(oldPos.asBlockPos()).isOf(GravestonesContent.GRAVESTONE_TECHNICAL)) {
                                    int damage = graveWorld.getBlockState(oldPos.asBlockPos()).get(TechnicalGravestoneBlock.DEATH_DAMAGE);
                                    String damageType;
                                    String graveData = "Age: " + graveWorld.getBlockState(oldPos.asBlockPos()).get(TechnicalGravestoneBlock.AGE_DAMAGE) + ", Death: " + graveWorld.getBlockState(oldPos.asBlockPos()).get(TechnicalGravestoneBlock.DEATH_DAMAGE);
                                    if (damage + 1 > 2) {
                                        graveWorld.breakBlock(oldPos.asBlockPos(), true);
                                        damageType = "broken";
                                    } else {
                                        if (graveWorld.getBlockState(oldPos.asBlockPos()).get(TechnicalGravestoneBlock.AGE_DAMAGE) + graveWorld.getBlockState(oldPos.asBlockPos()).get(TechnicalGravestoneBlock.DEATH_DAMAGE) >= 2) {
                                            damageType = "broken";
                                        } else {
                                            damageType = "damaged";
                                        }
                                        graveWorld.setBlockState(oldPos.asBlockPos(), graveWorld.getBlockState(oldPos.asBlockPos()).with(TechnicalGravestoneBlock.DEATH_DAMAGE, damage + 1));
                                    }
                                    logger("Gravestone (" + graveData + ") " + damageType + " at the position " + posToString(oldPos.asBlockPos()) + " in dimension " + oldPos.dimension.toString());
                                } else {
                                    logger("No gravestone was found at the position " + posToString(oldPos.asBlockPos()) + " in dimension " + oldPos.dimension.toString() + ". Most likely this is because the grave has already been collected, or was decayed");
                                }
                            } else {
                                logger("GravePosition's dimension (" + oldPos.dimension.toString() + ") does not exist!", LoggerInfoType.ERROR);
                            }
                        }
                    } else {
                        logger("No graves to damage!");
                    }
                } else {
                    logger("Gravestone death damage has been disabled in the config, so no graves were damaged");
                }
            }
        } else {
            logger("Nevermind, keepInventory is on!");
        }
        logger("----- ----- Ending Gravestone Work ----- -----");
    }

    public static void insertPlayerItems(GravestoneBlockEntity gravestone, PlayerEntity player) {
        logger("Inserting Inventory items into grave...");
        StringBuilder inventoryString = new StringBuilder().append("Inserting the Following Items: ");
        for (int i = 0; i < player.getInventory().size(); ++i) {
            Registries.ITEM.getId(player.getInventory().getStack(i).getItem());
            Identifier id = Registries.ITEM.getId(player.getInventory().getStack(i).getItem());
            inventoryString.append(id.getNamespace()).append(id.getPath());
            if (player.getInventory().getStack(i).hasNbt()) {
                inventoryString.append(" with NBT ").append(Objects.requireNonNull(player.getInventory().getStack(i).getNbt()).asString());
            }
            inventoryString.append(", ");

            if (!(EnchantmentHelper.getLevel(Enchantments.VANISHING_CURSE, player.getInventory().getStack(i)) > 0)) {
                gravestone.setStack(i, player.getInventory().removeStack(i));
            } else {
                player.getInventory().removeStack(i);
            }
        }

        logger("Items inserted!");
    }

    private static List<GravestonePosition> readAndWriteData(ServerWorld serverWorld, PlayerEntity player, BlockPos gravestonePos) {
        UUID uuid = player.getGameProfile().getId();

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
            logger("Data added, " + player.getName().getString() + " (" + uuid + ") has a new gravestone at " + posToString(playerData.firstGrave.asBlockPos()) + " in dimension " + playerData.firstGrave.dimension.toString());

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

    private static boolean hasNoIrreplaceableBlocks(World world, BlockPos blockPos) {
        return !world.getBlockState(blockPos).isIn(Gravestones.GRAVESTONE_IRREPLACEABLE);
    }

    private static void placeGravestoneAtPos(World world, BlockPos blockPos) {
        BlockState gravestoneBlock = GravestonesContent.GRAVESTONE_TECHNICAL.getDefaultState();
        world.breakBlock(blockPos, true);
        world.setBlockState(blockPos, gravestoneBlock);
    }

    private static BlockPos placeGravestone(World world, BlockPos blockPos) {
        if (blockPos.getY() > 0 || (blockPos.getY() > -64 && world.getDimensionKey() == DimensionTypes.OVERWORLD)) {
            return placeGravestoneAtValidPos(world, blockPos);

        } else if (world.getDimensionKey() == DimensionTypes.THE_END) {
            BlockPos islandCenter = placeGravestoneAtValidPos(world, blockPos.withY(70));
            if (!(islandCenter == null)) {
                createGravestoneIsland(world, islandCenter, Blocks.END_STONE.getDefaultState());
                return islandCenter;
            } else {
                return null;
            }

        } else if (world.getDimensionKey() == DimensionTypes.THE_NETHER) {
            BlockPos islandCenter = placeGravestoneAtValidPos(world, blockPos.withY(2));
            if (!(islandCenter == null)) {
                createGravestoneIsland(world, islandCenter, Blocks.NETHERRACK.getDefaultState());
                return islandCenter;
            } else {
                return null;
            }

        } else if (world.getDimensionKey() == DimensionTypes.OVERWORLD) {
            BlockPos islandCenter = placeGravestoneAtValidPos(world, blockPos.withY(-62));
            if (!(islandCenter == null)) {
                createGravestoneIsland(world, islandCenter, Blocks.DEEPSLATE.getDefaultState());
                return islandCenter;
            } else {
                return null;
            }

        } else {
            return null;
        }
    }

    private static void createGravestoneIsland(World world, BlockPos gravestonePos, BlockState state) {
        BlockPos islandCorner = gravestonePos.down().south().west();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                createIslandBlock(world, state, islandCorner.north(i).east(j));
            }
        }
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                for (int k = 0; k < 2; ++k) {
                    removeIslandBlock(world, islandCorner.north(i).east(j).up(k + 1));
                }
            }
        }
    }

    private static void createIslandBlock(World world, BlockState state, BlockPos blockPos) {
        if (world.getBlockState(blockPos).isAir()) {
            world.setBlockState(blockPos, state);
        }
    }

    private static void removeIslandBlock(World world, BlockPos blockPos) {
        if (!world.getBlockState(blockPos).isIn(Gravestones.GRAVESTONE_IRREPLACEABLE)) {
            world.breakBlock(blockPos, true);
        }
    }

    private static BlockPos placeGravestoneAtValidPos(World world, BlockPos blockPos) {
        if (hasNoIrreplaceableBlocks(world, blockPos)) {
            placeGravestoneAtPos(world, blockPos);
            return blockPos;

        } else if (hasNoIrreplaceableBlocks(world, blockPos.up())) {
            placeGravestoneAtPos(world, blockPos.up());
            return blockPos.up();

        } else if (hasNoIrreplaceableBlocks(world, blockPos.up(2))) {
            placeGravestoneAtPos(world, blockPos.up(2));
            return blockPos.up(2);

        } else if (hasNoIrreplaceableBlocks(world, blockPos.north())) {
            placeGravestoneAtPos(world, blockPos.north());
            return blockPos.north();

        } else if (hasNoIrreplaceableBlocks(world, blockPos.north().up())) {
            placeGravestoneAtPos(world, blockPos.north().up());
            return blockPos.north().up();

        } else if (hasNoIrreplaceableBlocks(world, blockPos.east())) {
            placeGravestoneAtPos(world, blockPos.east());
            return blockPos.east();

        } else if (hasNoIrreplaceableBlocks(world, blockPos.east().up())) {
            placeGravestoneAtPos(world, blockPos.east().up());
            return blockPos.east().up();

        } else if (hasNoIrreplaceableBlocks(world, blockPos.south())) {
            placeGravestoneAtPos(world, blockPos.south());
            return blockPos.south();

        } else if (hasNoIrreplaceableBlocks(world, blockPos.south().up())) {
            placeGravestoneAtPos(world, blockPos.south().up());
            return blockPos.south().up();

        } else if (hasNoIrreplaceableBlocks(world, blockPos.west())) {
            placeGravestoneAtPos(world, blockPos.west());
            return blockPos.west();

        } else if (hasNoIrreplaceableBlocks(world, blockPos.west().up())) {
            placeGravestoneAtPos(world, blockPos.west().up());
            return blockPos.west().up();

        } else if (hasNoIrreplaceableBlocks(world, blockPos.down())) {
            placeGravestoneAtPos(world, blockPos.down());
            return blockPos.down();

        } else {
            return null;
        }
    }
}