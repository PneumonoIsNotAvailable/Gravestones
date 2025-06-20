package net.pneumono.gravestones.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TestsUtil {
    public static BlockPos createPos(int i) {
        return new BlockPos(i, i, i);
    }

    public static void createAndKillPlayer(TestContext context, BlockPos pos) {
        createAndKillPlayer(context, pos, UUID.randomUUID());
    }

    public static void createAndKillPlayer(TestContext context, BlockPos pos, UUID uuid) {
        PlayerEntity player = createPlayer(context, pos, uuid);
        player.kill(context.getWorld());
    }

    public static PlayerEntity createPlayer(TestContext context, BlockPos pos) {
        return createPlayer(context, pos, UUID.randomUUID());
    }

    public static PlayerEntity createPlayer(TestContext context, BlockPos pos, UUID uuid) {
        ServerWorld world = context.getWorld();

        PlayerEntity player = new PlayerEntity(world, new GameProfile(uuid, "test-mock-player")) {
            @NotNull
            @Override
            public GameMode getGameMode() {
                return GameMode.SURVIVAL;
            }

            @Override
            public boolean isControlledByPlayer() {
                return false;
            }
        };

        world.spawnEntity(player);
        player.refreshPositionAndAngles(context.getAbsolutePos(pos), 0, 0);

        return player;
    }
}
