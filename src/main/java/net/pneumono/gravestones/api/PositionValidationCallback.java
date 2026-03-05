package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Callback for checking if a position is valid for gravestone placement.
 *
 * <p>The {@code gravestones:gravestone_irreplaceable} block tag should be used instead of this where possible.
 *
 * <p>Called for each position Gravestones checks, if it has not already been deemed invalid due to any of the following:
 * <ul>
 *     <li>The block at the position has a hardness < 0
 *     <li>The block at the position has a blast resistance 3600000
 *     <li>The block at the position is {@link net.minecraft.world.level.block.Blocks#VOID_AIR VOID_AIR}
 *     <li>The block at the position is in the tag {@code gravestones:gravestone:irreplaceable}
 * </ul>
 *
 * <p>Returning {@code true} falls back to further processing.
 * Returning {@code false} cancels further processing,
 * and prevents the gravestone from being placed at the position.
 *
 * <p>If you want to prevent gravestones being placed in a larger area,
 * you may want to redirect the gravestone placement position using {@link RedirectGravestonePositionCallback} instead.
 *
 * @see RedirectGravestonePositionCallback
 */
public interface PositionValidationCallback {
    Event<PositionValidationCallback> EVENT = EventFactory.createArrayBacked(PositionValidationCallback.class,
            listeners -> (level, state, deathPos) -> {
                for (PositionValidationCallback listener : listeners) {
                    if (!listener.isPositionValid(level, state, deathPos)) {
                        return false;
                    }
                }

                return true;
            }
    );

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isPositionValid(Level level, BlockState state, BlockPos deathPos);
}
