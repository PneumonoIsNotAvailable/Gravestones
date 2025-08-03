package net.pneumono.gravestones.gravestones;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.api.PositionValidationCallback;
import net.pneumono.gravestones.api.RedirectGravestonePositionCallback;
import net.pneumono.gravestones.content.GravestonesRegistry;
import org.jetbrains.annotations.Nullable;

public class GravestonePlacement extends GravestoneManager {
    public static final int RADIUS = 2;

    /**
     * Returns the placement position for a gravestone.
     *
     * <p>Checks {@link RedirectGravestonePositionCallback} listeners, and if none return a valid redirected position,
     * simply uses the result from {@link #getValidPos}.
     *
     * <p>Should not be called by {@link RedirectGravestonePositionCallback} listeners, for obvious reasons.
     *
     * @see RedirectGravestonePositionCallback
     */
    public static GlobalPos getRedirectableValidPos(ServerWorld world, PlayerEntity player, GlobalPos deathPos) {
        GlobalPos redirected = RedirectGravestonePositionCallback.EVENT.invoker().redirectPosition(world, player, deathPos);
        if (redirected != null) return redirected;

        BlockPos validPos = getValidPos(world, deathPos.getPos(), RADIUS);
        if (validPos == null) {
            return null;
        } else {
            return GlobalPos.create(deathPos.getDimension(), validPos);
        }
    }

    /**
     * Calculates and returns the best possible gravestone placement position within an area around the original position.
     *
     * <p>Searches positions in a cube of side length {@code (radius * 2) + 1}, centered on the {@code originalPos}.
     *
     * <p>May return null if it does not find a valid position,
     * such as if the original position is inside a bedrock cube.
     *
     * <p>Gravestones itself uses a radius of 2.
     */
    @Nullable
    public static BlockPos getValidPos(World world, BlockPos originalPos, int radius) {
        if (getCost(world, originalPos, originalPos) == 0) {
            return originalPos;
        }

        double bestCost = -1;
        BlockPos bestPos = null;

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = -radius; x <= radius; ++x) for (int y = -radius; y <= radius; ++y) for (int z = -radius; z <= radius; ++z) {
            mutable.set(originalPos.add(x, y, z));

            double cost = getCost(world, mutable, originalPos);
            if (cost < bestCost || (bestCost == -1 && cost != -1)) {
                bestCost = cost;
                bestPos = mutable.mutableCopy();
            }
        }

        if (bestCost == -1) {
            return null;
        } else {
            return bestPos;
        }
    }

    /**
     * Returns {@code true} if the block state cannot be replaced by gravestones. Does not check {@link PositionValidationCallback} listeners.
     */
    public static boolean isInvalid(BlockState state) {
        Block block = state.getBlock();
        return block.getHardness() < 0 ||
                block.getBlastResistance() >= 3600000 ||
                block == Blocks.VOID_AIR ||
                state.isIn(GravestonesRegistry.BLOCK_GRAVESTONE_IRREPLACEABLE);
    }

    private static double getCost(World world, BlockPos newPos, BlockPos origin) {
        BlockState state = world.getBlockState(newPos);
        if (
                isInvalid(state) || !PositionValidationCallback.EVENT.invoker().isPositionValid(world, state, newPos)
        ) {
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
        if (!state.isAir()) cost += state.isReplaceable() ? 0.5 : 5;
        if (world.getBlockState(newPos.down()).isAir()) cost += 1.5;

        cost += origin.getSquaredDistance(newPos);

        return cost;
    }
}
