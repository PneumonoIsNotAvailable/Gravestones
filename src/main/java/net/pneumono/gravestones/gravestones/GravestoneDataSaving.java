package net.pneumono.gravestones.gravestones;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.pneumono.gravestones.multiversion.VersionUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class GravestoneDataSaving extends GravestoneManager {
    protected static void saveBackup(CompoundTag contents, Player player) {
        saveBackup(contents, player, new Date());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected static void saveBackup(CompoundTag contents, Player player, Date date) {
        String uuidString = VersionUtil.getId(player.getGameProfile()).toString();
        File deathsFile = new File(
                getOrCreateGravestonesFolder(Objects.requireNonNull(player.level().getServer())), uuidString
        );

        deathsFile.mkdirs();

        Path path = deathsFile.toPath().resolve(GravestoneTime.FILE_SAVING.format(date) + ".dat");
        int count = 1;
        while (path.toFile().exists()) {
            count++;
            path = deathsFile.toPath().resolve(GravestoneTime.FILE_SAVING.format(date) + "_" + count + ".dat");
        }

        CompoundTag deathData = new CompoundTag();
        deathData.put("contents", contents);

        try {
            //? if >=1.20.3 {
            NbtIo.writeCompressed(deathData, path);
            //?} else {
            /*NbtIo.writeCompressed(deathData, path.toFile());
            *///?}
        } catch (IOException e) {
            error("Failed to write Gravestone Contents Data", e);
        }
    }

    public static GravestoneHistory readHistory(MinecraftServer server, UUID uuid) {
        Path path = getOrCreateGravestoneHistoryFile(server, uuid);

        if (!path.toFile().exists()) {
            info("Gravestone History file for {} does not exist! Creating new Gravestone History...", uuid);
            return new GravestoneHistory();
        }

        CompoundTag compound = new CompoundTag();
        try {
            //? if <1.20.3 {
            /*compound = NbtIo.readCompressed(path.toFile());
             *///?} else if =1.20.3 {
            /*NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
             *///?} else {
            compound = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            //?}
        } catch (IOException e) {
            error("Failed to read Gravestone History for {}", uuid, e);
        }

        return VersionUtil.get(compound, "data", GravestoneHistory.CODEC).orElse(new GravestoneHistory());
    }

    public static void writeHistory(MinecraftServer server, UUID uuid, GravestoneHistory history) {
        Path path = getOrCreateGravestoneHistoryFile(server, uuid);

        CompoundTag compound = new CompoundTag();
        VersionUtil.put(compound, "data", GravestoneHistory.CODEC, history);

        try {
            //? if >=1.20.3 {
            NbtIo.writeCompressed(compound, path);
             //?} else {
            /*NbtIo.writeCompressed(compound, path.toFile());
            *///?}
        } catch (IOException e) {
            error("Failed to write Gravestone History for {}", uuid, e);
        }
    }

    public static Path getOrCreateGravestoneHistoryFile(MinecraftServer server, UUID uuid) {
        File gravestoneFile = getOrCreateGravestonesFolder(server);

        return gravestoneFile.toPath().resolve(uuid.toString()).resolve("data.dat");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getOrCreateGravestonesFolder(MinecraftServer server) {
        File gravestonesFile = new File(server.getWorldPath(LevelResource.ROOT).toString(), "gravestones");

        gravestonesFile.mkdirs();

        return gravestonesFile;
    }
}
