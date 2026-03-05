package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Callback after a gravestone is collected.
 *
 * <p>Called after all other gravestone processing. At this point the contents have been returned to the player,
 * and the gravestone block itself has been broken.
 *
 * <p>Should not be used for collecting custom contents from graves.
 * If you need to add custom contents, see {@link GravestoneDataType}
 *
 * @see GravestoneDataType
 */
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
