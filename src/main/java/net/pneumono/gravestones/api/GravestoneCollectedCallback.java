package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.pneumono.gravestones.api.event.GravestoneCollectionEvents;

/**
 * @deprecated Use {@link GravestoneCollectionEvents.AfterCollect} instead.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated(forRemoval = true)
public interface GravestoneCollectedCallback {
    Event<GravestoneCollectedCallback> EVENT = EventFactory.createArrayBacked(GravestoneCollectedCallback.class,
            listeners -> (level, player, gravePos) -> {
                for (GravestoneCollectedCallback listener : listeners) {
                    listener.afterGravestoneCollect(level, player, gravePos);
                }
            }
    );

    void afterGravestoneCollect(ServerLevel level, Player player, BlockPos gravePos);
}
