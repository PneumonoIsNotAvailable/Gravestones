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

    private static BlockPos placeGravestoneAtValidPos(World world, BlockPos origin) {
        if (getCost(world, origin, origin) == 0) {
            return placeGravestoneAtPos(world, origin);
        }

        double bestCost = -1;
        BlockPos bestPos = null;

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = -2; x <= 2; ++x) for (int y = -2; y <= 2; ++y) for (int z = -2; z <= 2; ++z) {
            mutable.set(origin.add(x, y, z));

            double cost = getCost(world, mutable, origin);
            if (cost < bestCost || (bestCost == -1 && cost != -1)) {
                bestCost = cost;
                bestPos = mutable.mutableCopy();
            }
        }

        if (bestCost == -1) {
            return null;
        } else {
            return placeGravestoneAtPos(world, bestPos);
        }
    }

    private static double getCost(World world, BlockPos newPos, BlockPos origin) {
        BlockState state = world.getBlockState(newPos);
        Block block = state.getBlock();
        if (block.getHardness() < 0 || block.getBlastResistance() >= 3600000 || block == Blocks.VOID_AIR) {
            return -1;
        }

        double cost = 0;
        if (!state.getFluidState().isEmpty()) {
            if (state.getFluidState().isIn(FluidTags.WATER)) {
                cost += 1.5;
            } else {
                cost += 5;
            }
        }
        if (!state.isAir()) cost += state.isReplaceable() ? 1.25 : 5;
        if (world.getBlockState(newPos.down()).isAir()) cost += 1.5;

        cost += origin.getSquaredDistance(newPos);

        return cost;
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
}
