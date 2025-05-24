package net.pneumono.gravestones.gravestones;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Waterloggable;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.pneumono.gravestones.content.GravestonesRegistry;

public class GravestonePlacement {
    public static BlockPos placeGravestone(World world, BlockPos blockPos) {
        DimensionType dimension = world.getDimension();
        return placeGravestoneAtValidPos(world, blockPos.withY(MathHelper.clamp(blockPos.getY(), dimension.minY(), dimension.minY() + dimension.height())));
    }

    private static BlockPos placeGravestoneAtValidPos(World world, BlockPos blockPos) {
        if (hasNoIrreplaceableBlocks(world, blockPos)) {
            return placeGravestoneAtPos(world, blockPos);
        }

        int[] positionOrder = new int[]{0, 1, -1};
        for (int x : positionOrder) {
            for (int z : positionOrder) {
                for (int y : positionOrder) {
                    BlockPos newPos = blockPos.add(x, y, z);
                    if (hasNoIrreplaceableBlocks(world, newPos)) {
                        return placeGravestoneAtPos(world, newPos);
                    }
                }
            }
        }

        return placeGravestoneAtPos(world, blockPos);
    }

    private static BlockPos placeGravestoneAtPos(World world, BlockPos blockPos) {
        BlockState gravestoneBlock = GravestonesRegistry.GRAVESTONE_TECHNICAL.getDefaultState();
        BlockState replacedBlock = world.getBlockState(blockPos);
        if (replacedBlock.getFluidState().isIn(FluidTags.WATER) || (replacedBlock.getBlock() instanceof Waterloggable && replacedBlock.get(Properties.WATERLOGGED))) {
            gravestoneBlock = gravestoneBlock.with(Properties.WATERLOGGED, true);
        }

        world.breakBlock(blockPos, true);
        world.setBlockState(blockPos, gravestoneBlock);
        return blockPos;
    }

    private static boolean hasNoIrreplaceableBlocks(World world, BlockPos blockPos) {
        Block block = world.getBlockState(blockPos).getBlock();
        return !(block.getHardness() < 0 || block.getBlastResistance() >= 3600000) && block != Blocks.VOID_AIR;
    }
}
