package net.pneumono.gravestones.util;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class TestsUtil {
    public static void createAndKillPlayer(TestContext context, BlockPos pos) {
        ServerWorld world = context.getWorld();
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        world.spawnEntity(player);
        player.refreshPositionAndAngles(context.getAbsolutePos(pos), 0, 0);
        player.kill(world);
    }
}
