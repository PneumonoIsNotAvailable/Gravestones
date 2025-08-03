package net.pneumono.gravestones.compat;

import com.google.gson.GsonBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.gravestones.GravestoneDataSaving;
import net.pneumono.gravestones.gravestones.RecentGraveHistory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BackwardsCompat {
    public static void convertOldFiles(MinecraftServer server) {
        File gravestoneFile = new File(server.getSavePath(WorldSavePath.ROOT).toString(), "gravestone_data.json");
        if (!gravestoneFile.exists()) return;

        Gravestones.LOGGER.info("Old data detected! Converting to new format...");

        try {
            Reader reader = Files.newBufferedReader(gravestoneFile.toPath());
            GravestoneData data = (new GsonBuilder().setPrettyPrinting().create()).fromJson(reader, GravestoneData.class);
            reader.close();

            GravestoneDataSaving.writeData(
                    server,
                    data.data().stream().map(
                            playerData -> new RecentGraveHistory(
                                    playerData.owner(),
                                    playerData.firstGrave().convert(),
                                    playerData.secondGrave().convert(),
                                    playerData.thirdGrave().convert()
                            )
                    ).toList()
            );

            if (!gravestoneFile.delete()) {
                Gravestones.LOGGER.info("Failed to delete old data file!");
            }

        } catch (IOException e) {
            Gravestones.LOGGER.info("Old data file could not be read!");
        }
    }

    private record GravestoneData(List<PlayerGravestoneData> data) {

    }

    private record PlayerGravestoneData(UUID owner, GravestonePosition firstGrave, GravestonePosition secondGrave, GravestonePosition thirdGrave) {

    }

    private record GravestonePosition(Identifier dimension, int posX, int posY, int posZ) {
        private Optional<GlobalPos> convert() {
            return Optional.of(GlobalPos.create(RegistryKey.of(RegistryKeys.WORLD, dimension()), new BlockPos(this.posX, this.posY, this.posZ)));
        }
    }
}
