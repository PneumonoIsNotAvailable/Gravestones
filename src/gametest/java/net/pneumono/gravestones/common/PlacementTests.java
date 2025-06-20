package net.pneumono.gravestones.common;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.block.TechnicalGravestoneBlock;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.util.TestsUtil;

@SuppressWarnings("unused")
public class PlacementTests {
    @GameTest()
    public void gravestonesAppear(TestContext context) {
        ServerWorld world = context.getWorld();
        BlockPos pos = new BlockPos(0, 1, 0);

        context.setBlockState(pos.down(), Blocks.POLISHED_ANDESITE.getDefaultState());

        TestsUtil.createAndKillPlayer(context, pos);

        context.checkBlock(pos, block -> block instanceof TechnicalGravestoneBlock, block -> Text.literal("Expected Gravestone to have been placed! Instead found ").append(block.getName()));
        context.complete();
    }

    @GameTest()
    public void gravestonesAdjustPosition(TestContext context) {
        ServerWorld world = context.getWorld();

        BlockPos pos = new BlockPos(0, 1, 0);

        context.setBlockState(pos, Blocks.POLISHED_ANDESITE.getDefaultState());

        TestsUtil.createAndKillPlayer(context, pos);

        context.dontExpectBlock(GravestonesRegistry.GRAVESTONE_TECHNICAL, pos);
        context.complete();
    }
}
