package net.pneumono.gravestones.common;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.block.TechnicalGravestoneBlock;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.util.TestsUtil;

@SuppressWarnings("unused")
public class PlacementTests {
    @GameTest(
            structure = "gravestones_test:spawns_at_death_pos"
    )
    public void graveSpawnsAtDeathPos(TestContext context) {
        BlockPos pos = TestsUtil.createPos(1);

        TestsUtil.createAndKillPlayer(context, pos);

        context.expectBlock(GravestonesRegistry.GRAVESTONE_TECHNICAL, pos);
        context.complete();
    }

    @GameTest(
            structure = "gravestones_test:adjusts_position"
    )
    public void graveAdjustsPosition(TestContext context) {
        BlockPos pos = TestsUtil.createPos(1);

        TestsUtil.createAndKillPlayer(context, pos);

        context.dontExpectBlock(GravestonesRegistry.GRAVESTONE_TECHNICAL, pos);
        context.complete();
    }

    @GameTest(
            structure = "gravestones_test:cannot_place"
    )
    public void graveCannotPlace(TestContext context) {
        BlockPos pos = TestsUtil.createPos(3);

        TestsUtil.createAndKillPlayer(context, pos);

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = 0; x < 5; ++x) for (int y = 0; y < 5; ++y) for (int z = 0; z < 5; ++z) {
            context.dontExpectBlock(GravestonesRegistry.GRAVESTONE_TECHNICAL, mutable.set(x, y, z));
        }
        context.complete();
    }

    @GameTest(
            structure = "gravestones_test:is_waterlogged"
    )
    public void graveIsWaterlogged(TestContext context) {
        BlockPos pos = TestsUtil.createPos(3);;

        TestsUtil.createAndKillPlayer(context, pos);

        context.checkBlockState(pos, state -> state.get(TechnicalGravestoneBlock.WATERLOGGED, false), state -> Text.literal("Expected Gravestone to be waterlogged! Instead found " + state.toString()));
        context.complete();
    }

    @GameTest(
            structure = "gravestones_test:can_find_shore"
    )
    public void graveCanFindShore(TestContext context) {
        BlockPos pos = new BlockPos(2, 1, 2);

        TestsUtil.createAndKillPlayer(context, pos);

        context.expectBlock(GravestonesRegistry.GRAVESTONE_TECHNICAL, pos.east(2).up());
        context.complete();
    }

    @GameTest(
            structure = "gravestones_test:must_replace"
    )
    public void graveMustReplace(TestContext context) {
        BlockPos pos = TestsUtil.createPos(3);

        TestsUtil.createAndKillPlayer(context, pos);

        context.expectBlock(GravestonesRegistry.GRAVESTONE_TECHNICAL, pos);
        context.expectEntity(EntityType.ITEM);
        context.complete();
    }

    @GameTest(
            structure = "gravestones_test:resists_explosion"
    )
    public void graveResistsExplosion(TestContext context) {
        ServerWorld world = context.getWorld();
        BlockPos pos = TestsUtil.createPos(1);

        PlayerEntity player = TestsUtil.createPlayer(context, pos);

        BlockPos absolutePos = context.getAbsolutePos(pos);
        world.createExplosion(null, absolutePos.getX(), absolutePos.getY(), absolutePos.getZ(), 5, World.ExplosionSourceType.NONE);

        context.expectBlock(GravestonesRegistry.GRAVESTONE_TECHNICAL, pos);
        context.dontExpectEntity(EntityType.ITEM);
        context.complete();
    }
}
