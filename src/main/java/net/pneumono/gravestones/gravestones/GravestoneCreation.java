package net.pneumono.gravestones.gravestones;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.*;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.CancelGravestonePlacementCallback;
import net.pneumono.gravestones.api.GravestonePlacedCallback;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
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

public class GravestoneCreation extends GravestonesManager {
    public static void handleGravestones(PlayerEntity player) {
        World world = player.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        info("----- ----- Beginning Gravestone Work ----- -----");
        info("This mostly exists for debugging purposes, but might be useful for server owners. " +
                "If you don't want to see all this every time someone dies, disable 'console_info' in the config!");

        BlockPos playerPos = player.getBlockPos();
        String playerName = player.getName().getString();
        GameProfile playerProfile = player.getGameProfile();

        if (CancelGravestonePlacementCallback.EVENT.invoker().shouldCancel(
                serverWorld, player, new GlobalPos(serverWorld.getRegistryKey(), playerPos)
        )) {
            info("Gravestone placement cancelled!");
            info("----- ----- Ending Gravestone Work ----- -----");
            return;
        }

        GlobalPos globalDeathPos = new GlobalPos(world.getRegistryKey(), playerPos);
        GlobalPos globalGravestonePos = GravestonePlacement.placeGravestone(
                serverWorld, player, globalDeathPos
        );
        BlockPos gravestonePos = globalGravestonePos == null ? new BlockPos(0, 0, 0) : globalGravestonePos.pos();

        MinecraftServer server = world.getServer();

        if (globalGravestonePos == null || !(server.getWorld(globalGravestonePos.dimension()) instanceof World validWorld)) {
            error("Gravestone was not placed successfully! The items have been dropped on the floor");
        } else {
            String uuid = "";
            if (GravestonesConfig.CONSOLE_INFO.getValue()) {
                uuid = " (" + playerProfile.getId() + ")";
            }
            Gravestones.LOGGER.info("Placed {}'s{} Gravestone at {}", playerName, uuid, posToString(gravestonePos));

            if (GravestonesConfig.BROADCAST_COORDINATES_IN_CHAT.getValue()) {
                server.getPlayerManager().broadcast(Text.translatable("gravestones.grave_spawned", playerName, posToString(gravestonePos)), false);
            }

            if (validWorld.getBlockEntity(gravestonePos) instanceof TechnicalGravestoneBlockEntity gravestone) {
                gravestone.setGraveOwner(new ProfileComponent(playerProfile));
                Date date = new Date();
                gravestone.setSpawnDate(GravestoneTime.READABLE.format(date), validWorld.getTime());

                info("Inserting gravestone data into grave...");
                gravestone.setContents(GravestonesApi.getDataToInsert(player));
                info("Data inserted!");

                recordDeathData(gravestone, player, date);

                validWorld.updateListeners(gravestonePos, gravestone.getCachedState(), gravestone.getCachedState(), Block.NOTIFY_LISTENERS);

                info("Gave Gravestone it's data (graveOwner, spawnDate, and inventory)");
            } else {
                error("Gravestone position does not have a block entity!");
            }
        }

        List<GravestonePosition> oldGravePositions = readAndWriteData(serverWorld, playerProfile, playerName, gravestonePos);
        GravestoneDecay.deathDecayOldGravestones(serverWorld, oldGravePositions, gravestonePos);

        GravestonePlacedCallback.EVENT.invoker().afterGravestonePlace(serverWorld, player, globalDeathPos, globalGravestonePos);

        info("----- ----- Ending Gravestone Work ----- -----");
    }

    private static void recordDeathData(TechnicalGravestoneBlockEntity gravestone, PlayerEntity player, Date date) {
        File deathsFile = new File(
                Gravestones.GRAVESTONES_ROOT.apply(Objects.requireNonNull(player.getServer())), player.getUuidAsString()
        );
        if (deathsFile.mkdirs()) {
            info("No gravestone death data file exists for " + player.getUuidAsString() + ", creating one");
        }
        Path path = deathsFile.toPath().resolve(GravestoneTime.FILE_SAVING.format(date) + ".dat");
        int count = 1;
        while (path.toFile().exists()) {
            count++;
            path = deathsFile.toPath().resolve(GravestoneTime.FILE_SAVING.format(date) + "_" + count + ".dat");
        }

        NbtCompound deathData = new NbtCompound();
        deathData.put("contents", gravestone.getContents());

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