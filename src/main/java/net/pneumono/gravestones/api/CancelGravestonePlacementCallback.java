package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.pneumono.gravestones.api.event.GravestonePlacementEvents;

/**
 * @deprecated Use {@link GravestonePlacementEvents.CancelPlace} instead.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated(forRemoval = true)
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
