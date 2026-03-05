package net.pneumono.gravestones.gravestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.PositionValidationCallback;
import net.pneumono.gravestones.api.RedirectGravestonePositionCallback;
import net.pneumono.gravestones.multiversion.VersionUtil;
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
    public static GlobalPos getRedirectableValidPos(ServerLevel level, Player player, GlobalPos deathPos) {
        GlobalPos redirected = RedirectGravestonePositionCallback.EVENT.invoker().redirectPosition(level, player, deathPos);
        if (redirected != null) return redirected;

        BlockPos validPos = getValidPos(level, deathPos.pos(), RADIUS);
        if (validPos == null) {
            return null;
        } else {
            return VersionUtil.createGlobalPos(deathPos.dimension(), validPos);
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
    public static BlockPos getValidPos(Level level, BlockPos originalPos, int radius) {
        if (getCost(level, originalPos, originalPos) == 0) {
            return originalPos;
        }

        double bestCost = -1;
        BlockPos bestPos = null;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; ++x) for (int y = -radius; y <= radius; ++y) for (int z = -radius; z <= radius; ++z) {
            mutable.set(originalPos.offset(x, y, z));

            double cost = getCost(level, mutable, originalPos);
            if (cost < bestCost || (bestCost == -1 && cost != -1)) {
                bestCost = cost;
                bestPos = mutable.mutable();
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
        return block.defaultDestroyTime() < 0 ||
                block.getExplosionResistance() >= 3600000 ||
                block == Blocks.VOID_AIR ||
                state.is(GravestonesApi.BLOCK_GRAVESTONE_IRREPLACEABLE);
    }

    private static double getCost(Level level, BlockPos newPos, BlockPos origin) {
        BlockState state = level.getBlockState(newPos);
        if (
                isInvalid(state) || !PositionValidationCallback.EVENT.invoker().isPositionValid(level, state, newPos)
        ) {
            return -1;
        }

        double cost = 0;
        if (!state.getFluidState().isEmpty()) {
            if (state.getFluidState().is(FluidTags.WATER)) {
                cost += 1.5;
            } else {
                cost += 5;
            }
        }
        if (!state.isAir()) cost += state.canBeReplaced() ? 0.5 : 5;
        if (level.getBlockState(newPos.below()).isAir()) cost += 1.5;

        cost += origin.distSqr(newPos);

        return cost;
    }
}
