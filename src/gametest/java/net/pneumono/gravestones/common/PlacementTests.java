package net.pneumono.gravestones.common;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.block.TechnicalGravestoneBlock;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.util.TestsUtil;

@SuppressWarnings("unused")
public class PlacementTests {
    @GameTest(
            structure = "gravestones_test:spawn_at_death_pos"
    )
    public void graveSpawnsAtDeathPos(TestContext context) {
        BlockPos pos = new BlockPos(1, 1, 1);

        TestsUtil.createAndKillPlayer(context, pos);

        context.checkBlock(pos, block -> block instanceof TechnicalGravestoneBlock, block -> Text.literal("Expected Gravestone to have been placed! Instead found ").append(block.getName()));
        context.complete();
    }

    @GameTest(
            structure = "gravestones_test:adjust_position"
    )
    public void graveAdjustsPosition(TestContext context) {
        BlockPos pos = new BlockPos(1, 1, 1);

        TestsUtil.createAndKillPlayer(context, pos);

        context.dontExpectBlock(GravestonesRegistry.GRAVESTONE_TECHNICAL, pos);
        context.complete();
    }

    @GameTest(
            structure = "gravestones_test:cannot_place"
    )
    public void graveCannotPlace(TestContext context) {
        BlockPos pos = new BlockPos(3, 3, 3);

        TestsUtil.createAndKillPlayer(context, pos);

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = 0; x < 5; ++x) for (int y = 0; y < 5; ++y) for (int z = 0; z < 5; ++z) {
            context.dontExpectBlock(GravestonesRegistry.GRAVESTONE_TECHNICAL, mutable.set(x, y, z));
        }
        context.complete();
    }
}
