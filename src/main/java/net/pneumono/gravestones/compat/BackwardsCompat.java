package net.pneumono.gravestones.compat;

import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.gravestones.GravestoneDataSaving;
import net.pneumono.gravestones.gravestones.GravestoneHistory;
import net.pneumono.gravestones.multiversion.VersionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BackwardsCompat {
    public static final Logger LOGGER = LoggerFactory.getLogger("Gravestones Backwards Compat");

    public static void convertOldFiles(MinecraftServer server) {
        // Goes through each version of gravestone data, reading and rewriting it as the next version each time.
        // Not efficient, but is easier to maintain, and who cares, it only happens on server start anyway.
        convertGravestoneDataFile(server);
        convertRecentGravestoneHistoryFile(server);
    }

    // Gravestone Data File

    private static void convertGravestoneDataFile(MinecraftServer server) {
        File gravestoneFile = new File(server.getWorldPath(LevelResource.ROOT).toString(), "gravestone_data.json");
        if (!gravestoneFile.exists()) return;

        LOGGER.info("Found old gravestone data file! Converting to new format...");

        try {
            Reader reader = Files.newBufferedReader(gravestoneFile.toPath());
            GravestoneData data = (new GsonBuilder().setPrettyPrinting().create()).fromJson(reader, GravestoneData.class);
            reader.close();

            Path path = GravestoneDataSaving.getOrCreateGravestonesFolder(server).toPath().resolve("data.dat");

            CompoundTag compound = new CompoundTag();
            VersionUtil.put(compound, "data", RecentGraveHistory.CODEC.listOf(), data.data().stream().map(
                    playerData -> new RecentGraveHistory(
                            playerData.owner(),
                            playerData.firstGrave().convert(),
                            playerData.secondGrave().convert(),
                            playerData.thirdGrave().convert()
                    )
            ).toList());

            try {
                //? if >=1.20.3 {
                NbtIo.writeCompressed(compound, path);
                //?} else {
                /*NbtIo.writeCompressed(compound, path.toFile());
                 *///?}
            } catch (IOException e) {
                LOGGER.error("Failed to write Gravestone Data", e);
            }

            if (!gravestoneFile.delete()) {
                LOGGER.info("Failed to delete old gravestone data file!");
            }

        } catch (IOException e) {
            LOGGER.info("Old gravestone data file could not be read!");
        }
    }

    // Recent Gravestone History File

    private static void convertRecentGravestoneHistoryFile(MinecraftServer server) {
        Path dataFile = GravestoneDataSaving.getOrCreateGravestonesFolder(server).toPath().resolve("data.dat");
        if (!dataFile.toFile().exists()) return;

        Gravestones.LOGGER.info("Found old gravestone history file! Converting to new format...");

        CompoundTag compound = new CompoundTag();
        try {
            //? if <1.20.3 {
            /*compound = NbtIo.readCompressed(dataFile.toFile());
             *///?} else if =1.20.3 {
            /*NbtIo.readCompressed(dataFile, NbtAccounter.unlimitedHeap());
             *///?} else {
            compound = NbtIo.readCompressed(dataFile, NbtAccounter.unlimitedHeap());
            //?}
        } catch (IOException e) {
            LOGGER.error("Failed to read Gravestone Data", e);
        }

        List<RecentGraveHistory> histories = VersionUtil.get(compound, "data", RecentGraveHistory.CODEC.listOf()).orElse(new ArrayList<>());

        for (RecentGraveHistory oldHistory : histories) {
            UUID uuid = oldHistory.owner();
            GravestoneHistory newHistory = new GravestoneHistory(oldHistory.getList());
            GravestoneDataSaving.writeHistory(server, uuid, newHistory);
        }

        if (!dataFile.toFile().delete()) {
            LOGGER.info("Failed to delete old gravestone history file!");
        }
    }

    // Old classes/records

    private record GravestoneData(List<PlayerGravestoneData> data) {

    }

    private record PlayerGravestoneData(UUID owner, GravestonePosition firstGrave, GravestonePosition secondGrave, GravestonePosition thirdGrave) {

    }

    private record GravestonePosition(Identifier dimension, int posX, int posY, int posZ) {
        private Optional<GlobalPos> convert() {
            return Optional.of(VersionUtil.createGlobalPos(ResourceKey.create(Registries.DIMENSION, dimension()), new BlockPos(this.posX, this.posY, this.posZ)));
        }
    }

    public record RecentGraveHistory(UUID owner, Optional<GlobalPos> first, Optional<GlobalPos> second, Optional<GlobalPos> third) {
        public static final Codec<RecentGraveHistory> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                UUIDUtil.CODEC.fieldOf("owner").forGetter(RecentGraveHistory::owner),
                GlobalPos.CODEC.optionalFieldOf("first").forGetter(RecentGraveHistory::first),
                GlobalPos.CODEC.optionalFieldOf("second").forGetter(RecentGraveHistory::second),
                GlobalPos.CODEC.optionalFieldOf("third").forGetter(RecentGraveHistory::third)
        ).apply(builder, RecentGraveHistory::new));

        public List<GlobalPos> getList() {
            List<GlobalPos> list = new ArrayList<>();
            this.first.ifPresent(list::add);
            this.second.ifPresent(list::add);
            this.third.ifPresent(list::add);
            return list;
        }
    }
}
