package net.pneumono.gravestones.gravestones;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.pneumono.gravestones.multiversion.VersionUtil;
import net.pneumono.pneumonocore.util.MultiVersionUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class GravestoneDataSaving extends GravestoneManager {
    protected static void saveBackup(NbtCompound contents, PlayerEntity player) {
        saveBackup(contents, player, new Date());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected static void saveBackup(NbtCompound contents, PlayerEntity player, Date date) {
        String uuidString = VersionUtil.getId(player.getGameProfile()).toString();
        File deathsFile = new File(
                getOrCreateGravestonesFolder(Objects.requireNonNull(MultiVersionUtil.getWorld(player).getServer())), uuidString
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

    public static List<RecentGraveHistory> readHistories(MinecraftServer server) {
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getOrCreateGravestonesFolder(MinecraftServer server) {
        File gravestonesFile = new File(server.getSavePath(WorldSavePath.ROOT).toString(), "gravestones");

        gravestonesFile.mkdirs();

        return gravestonesFile;
    }
}
