package net.pneumono.gravestones.gravestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.pneumono.gravestones.api.DeprecatedEventHandler;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.event.GravestonePlacementEvents;
import net.pneumono.gravestones.multiversion.VersionUtil;
import org.jetbrains.annotations.Nullable;

// Everything here is a mess, and will be improved soon!
public class GravestonePlacement extends GravestoneManager {
    public static final int RADIUS = 2;

    public static GlobalPos getPlacementPos(ServerLevel level, GlobalPos deathPos) {
        DimensionType dimension = level.dimensionType();
        GlobalPos clampedDeathPos = VersionUtil.createGlobalPos(deathPos.dimension(), deathPos.pos().atY(
                Mth.clamp(deathPos.pos().getY(), dimension.minY(), dimension.minY() + dimension.height())
        ));
        GlobalPos validPos = getNewRedirectableValidPos(level, clampedDeathPos);

        if (validPos == null || level.getServer().getLevel(validPos.dimension()) == null) return null;

        return validPos;
    }

    @Deprecated
    public static GlobalPos getRedirectableValidPos(ServerLevel level, Player player, GlobalPos deathPos) {
        GlobalPos redirected = DeprecatedEventHandler.redirectGravestonePositionCallback(level, player, deathPos);
        if (redirected != null) return redirected;

        BlockPos validPos = getValidPos(level, deathPos.pos(), RADIUS);
        if (validPos == null) {
            return null;
        } else {
            return VersionUtil.createGlobalPos(deathPos.dimension(), validPos);
        }
    }

    public static GlobalPos getNewRedirectableValidPos(ServerLevel level, GlobalPos pos) {
        BlockPos validPos = getNewValidPos(level, pos.pos(), RADIUS);
        if (validPos == null) {
            return null;
        } else {
            return VersionUtil.createGlobalPos(pos.dimension(), validPos);
        }
    }

    @Nullable
    @Deprecated
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

    @Nullable
    public static BlockPos getNewValidPos(ServerLevel level, BlockPos originalPos, int radius) {
        if (getNewCost(level, originalPos, originalPos) == 0) {
            return originalPos;
        }

        double bestCost = -1;
        BlockPos bestPos = null;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; ++x) for (int y = -radius; y <= radius; ++y) for (int z = -radius; z <= radius; ++z) {
            mutable.set(originalPos.offset(x, y, z));

            double cost = getNewCost(level, mutable, originalPos);
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

    @Deprecated
    public static boolean isInvalid(BlockState state) {
        Block block = state.getBlock();
        return block.defaultDestroyTime() < 0 ||
                block.getExplosionResistance() >= 3600000 ||
                block == Blocks.VOID_AIR ||
                state.is(GravestonesApi.BLOCK_GRAVESTONE_IRREPLACEABLE);
    }

    @Deprecated
    private static double getCost(Level level, BlockPos newPos, BlockPos origin) {
        BlockState state = level.getBlockState(newPos);
        if (
                isInvalid(state) || !DeprecatedEventHandler.positionValidationCallback(level, state, newPos)
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

    private static double getNewCost(ServerLevel level, BlockPos newPos, BlockPos origin) {
        BlockState state = level.getBlockState(newPos);
        if (
                !GravestonePlacementEvents.runValidatePosition(level, state, origin) || !DeprecatedEventHandler.positionValidationCallback(level, state, newPos)
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
