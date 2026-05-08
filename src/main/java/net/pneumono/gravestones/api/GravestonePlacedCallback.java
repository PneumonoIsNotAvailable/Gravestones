package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.pneumono.gravestones.api.event.GravestonePlacementEvents;

/**
 * @deprecated Use {@link GravestonePlacementEvents.AfterPlace} instead.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated(forRemoval = true)
public interface GravestonePlacedCallback {
    Event<GravestonePlacedCallback> EVENT = EventFactory.createArrayBacked(GravestonePlacedCallback.class,
            listeners -> (level, player, deathPos, placementPos) -> {
                for (GravestonePlacedCallback listener : listeners) {
                    listener.afterGravestonePlace(level, player, deathPos, placementPos);
                }
            }
    );

    void afterGravestonePlace(ServerLevel level, Player player, GlobalPos deathPos, GlobalPos placementPos);
}
