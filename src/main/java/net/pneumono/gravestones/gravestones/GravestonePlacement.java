package net.pneumono.gravestones.gravestones;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.pneumono.gravestones.api.RedirectGravestonePositionCallback;
import net.pneumono.gravestones.content.GravestonesRegistry;
import org.jetbrains.annotations.Nullable;

public class GravestonePlacement extends GravestonesManager {
    public static final int RADIUS = 2;

    protected static GlobalPos placeGravestone(ServerWorld world, PlayerEntity player, GlobalPos deathPos) {
        DimensionType dimension = world.getDimension();
        GlobalPos clampedDeathPos = new GlobalPos(deathPos.dimension(), deathPos.pos().withY(
                MathHelper.clamp(deathPos.pos().getY(), dimension.minY(), dimension.minY() + dimension.height())
        ));
        GlobalPos validPos = getRedirectableValidPos(world, player, clampedDeathPos);

        if (validPos == null) return null;

        World validWorld = world.getServer().getWorld(validPos.dimension());
        if (validWorld == null) return null;

        BlockState gravestoneBlock = GravestonesRegistry.GRAVESTONE_TECHNICAL.getDefaultState();
        BlockState replacedBlock = validWorld.getBlockState(validPos.pos());
        if (
                replacedBlock.getFluidState().isIn(FluidTags.WATER) ||
                (replacedBlock.getBlock() instanceof Waterloggable && replacedBlock.get(Properties.WATERLOGGED))
        ) {
            gravestoneBlock = gravestoneBlock.with(Properties.WATERLOGGED, true);
        }

        validWorld.breakBlock(validPos.pos(), true);
        validWorld.setBlockState(validPos.pos(), gravestoneBlock);
        return validPos;
    }

    /**
     * Returns the placement position for a gravestone.
     *
     * <p>Checks {@link RedirectGravestonePositionCallback} listeners, and if none return a redirected position,
     * simply uses the result from {@link #getValidPos}.
     *
     * <p>Should not be called by {@link RedirectGravestonePositionCallback} listeners, for obvious reasons.
     *
     * @see RedirectGravestonePositionCallback
     */
    public static GlobalPos getRedirectableValidPos(ServerWorld world, PlayerEntity player, GlobalPos deathPos) {
        GlobalPos redirected = RedirectGravestonePositionCallback.EVENT.invoker().redirectPosition(world, player, deathPos);
        if (redirected != null) return redirected;

        BlockPos validPos = getValidPos(world, deathPos.pos(), RADIUS);
        if (validPos == null) {
            return null;
        } else {
            return new GlobalPos(deathPos.dimension(), validPos);
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
        if (!state.isAir()) cost += state.isReplaceable() ? 0.5 : 5;
        if (world.getBlockState(newPos.down()).isAir()) cost += 1.5;

        cost += origin.getSquaredDistance(newPos);

        return cost;
    }
}
