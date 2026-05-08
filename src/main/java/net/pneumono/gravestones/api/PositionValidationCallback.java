package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.pneumono.gravestones.api.event.GravestonePlacementEvents;

/**
 * @deprecated Use {@link GravestonePlacementEvents.ValidatePosition} instead.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated(forRemoval = true)
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
