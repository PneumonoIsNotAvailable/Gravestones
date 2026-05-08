package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.pneumono.gravestones.api.event.GravestonePlacementEvents;
import net.pneumono.gravestones.gravestones.GravestonePlacement;

/**
 * @deprecated Use {@link GravestonePlacementEvents.RedirectPosition} instead.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated(forRemoval = true)
public interface RedirectGravestonePositionCallback {
    Event<RedirectGravestonePositionCallback> EVENT = EventFactory.createArrayBacked(RedirectGravestonePositionCallback.class,
            listeners -> (level, player, deathPos) -> {
                GlobalPos placementPos = null;

                for (RedirectGravestonePositionCallback listener : listeners) {
                    placementPos = listener.redirectPosition(level, player, deathPos);
                    if (GravestonePlacement.isInvalid(level.getBlockState(placementPos.pos()))) placementPos = null;
                    if (placementPos != null) break;
                }

                return placementPos;
            }
    );

    GlobalPos redirectPosition(ServerLevel level, Player player, GlobalPos deathPos);
}
