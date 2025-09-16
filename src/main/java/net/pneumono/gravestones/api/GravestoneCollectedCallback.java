package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

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
            listeners -> (world, player, gravePos) -> {
                for (GravestoneCollectedCallback listener : listeners) {
                    listener.afterGravestoneCollect(world, player, gravePos);
                }
            }
    );

    void afterGravestoneCollect(ServerWorld world, PlayerEntity player, BlockPos gravePos);
}
