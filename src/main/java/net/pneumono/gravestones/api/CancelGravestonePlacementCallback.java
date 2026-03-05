package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Callback for cancelling gravestone placement.
 *
 * <p>Called before any other gravestone processing.
 * If gravestone placement is cancelled, items will simply drop on the floor as in vanilla.
 *
 * <p>Returning {@code true} cancels further processing and prevents the gravestone from being placed.
 * Returning {@code false} falls back to further processing.
 *
 * <p>Ideal for situations where gravestones should not be placed.
 * For example, a custom dimension in which blocks cannot be placed or broken.
 * In some of these situations, it may be better to simply move the gravestone to another position instead.
 * To do this, use {@link RedirectGravestonePositionCallback}.
 *
 * @see RedirectGravestonePositionCallback
 */
public interface CancelGravestonePlacementCallback {
    Event<CancelGravestonePlacementCallback> EVENT = EventFactory.createArrayBacked(CancelGravestonePlacementCallback.class,
            listeners -> (level, player, deathPos) -> {
                for (CancelGravestonePlacementCallback listener : listeners) {
                    if (listener.shouldCancel(level, player, deathPos)) {
                        return true;
                    }
                }

                return false;
            }
    );

    boolean shouldCancel(ServerLevel level, Player player, GlobalPos deathPos);
}
