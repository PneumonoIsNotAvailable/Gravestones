package net.pneumono.gravestones.gravestones;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.GlobalPos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class GravestoneDataSaving extends GravestoneManager {
    protected static void saveDeathData(NbtCompound contents, PlayerEntity player, Date date) {
        String uuidString = player.getGameProfile().getId().toString();
        File deathsFile = new File(
                getOrCreateGravestonesFolder(Objects.requireNonNull(player.getServer())), uuidString
        );

        deathsFile.mkdirs();

        Path path = deathsFile.toPath().resolve(GravestoneTime.FILE_SAVING.format(date) + ".dat");
        int count = 1;
        while (path.toFile().exists()) {
            count++;
            path = deathsFile.toPath().resolve(GravestoneTime.FILE_SAVING.format(date) + "_" + count + ".dat");
        }

        NbtCompound deathData = new NbtCompound();
        deathData.put("contents", contents);

        try {
            NbtIo.writeCompressed(deathData, path);
        } catch (IOException e) {
            error("Failed to write Gravestone Contents Data", e);
        }
    }

    protected static List<GlobalPos> readAndWriteData(MinecraftServer server, UUID uuid, GlobalPos newPos) {
        // Read Data
        List<RecentGraveHistory> histories = new ArrayList<>(readData(server));

        // Process Data
        RecentGraveHistory history = new RecentGraveHistory(uuid);
        for (int i = 0; i < histories.size(); ++i) {
            RecentGraveHistory checkedHistory = histories.get(i);
            if (checkedHistory.owner().equals(uuid)) {
                history = histories.remove(i);
                break;
            }
        }

        List<GlobalPos> posList = history.getList();
        histories.add(history.getShifted(newPos));

        // Write data
        writeData(server, histories);

        return posList;
    }

    public static List<RecentGraveHistory> readData(MinecraftServer server) {
        Path path = getOrCreateGravestonesDataFile(server);

        NbtCompound compound = new NbtCompound();
        try {
            compound = NbtIo.readCompressed(path, NbtSizeTracker.ofUnlimitedBytes());
        } catch (IOException e) {
            error("Failed to read Gravestone Data", e);
        }

        return compound.get("data", RecentGraveHistory.CODEC.listOf()).orElse(new ArrayList<>());
    }

    public static void writeData(MinecraftServer server, List<RecentGraveHistory> histories) {
        Path path = getOrCreateGravestonesDataFile(server);

        NbtCompound compound = new NbtCompound();
        compound.put("data", RecentGraveHistory.CODEC.listOf(), histories);

        try {
            NbtIo.writeCompressed(compound, path);
        } catch (IOException e) {
            error("Failed to write Gravestone Data", e);
        }
    }

    public static Path getOrCreateGravestonesDataFile(MinecraftServer server) {
        File gravestoneFile = getOrCreateGravestonesFolder(server);

        return gravestoneFile.toPath().resolve("data.dat");
    }

    public static File getOrCreateGravestonesFolder(MinecraftServer server) {
        File gravestonesFile = new File(server.getSavePath(WorldSavePath.ROOT).toString(), "gravestones");

        gravestonesFile.mkdirs();

        return gravestonesFile;
    }
}
