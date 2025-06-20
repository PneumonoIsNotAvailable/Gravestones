package net.pneumono.gravestones;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.pneumono.gravestones.block.TechnicalGravestoneBlock;
import net.pneumono.gravestones.content.GravestonesRegistry;

@SuppressWarnings("unused")
public class GravestonesGameTest {
    @GameTest()
    public void gravestonesAppear(TestContext context) {
        ServerWorld world = context.getWorld();

        BlockPos pos = new BlockPos(0, 1, 0);

        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        world.spawnEntity(player);
        context.setBlockState(pos.down(), Blocks.POLISHED_ANDESITE.getDefaultState());

        player.refreshPositionAndAngles(context.getAbsolutePos(pos), 0, 0);
        player.kill(world);
        context.checkBlock(pos, block -> block instanceof TechnicalGravestoneBlock, block -> Text.literal("Expected Gravestone to have been placed! Instead found ").append(block.getName()));
        context.complete();
    }

    @GameTest()
    public void gravestonesAdjustPosition(TestContext context) {
        ServerWorld world = context.getWorld();

        BlockPos pos = new BlockPos(0, 1, 0);

        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        world.spawnEntity(player);
        context.setBlockState(pos, Blocks.POLISHED_ANDESITE.getDefaultState());

        player.refreshPositionAndAngles(context.getAbsolutePos(pos), 0, 0);
        player.kill(world);
        context.dontExpectBlock(GravestonesRegistry.GRAVESTONE_TECHNICAL, pos);
        context.complete();
    }
}
