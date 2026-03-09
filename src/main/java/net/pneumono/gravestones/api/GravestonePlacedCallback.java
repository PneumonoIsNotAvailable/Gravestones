package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Callback after a gravestone is placed.
 *
 * <p>Called after all other gravestone processing. At this point the player is dead.
 *
 * <p>{@code placementPos} may differ from {@code deathPos},
 * as Gravestones will attempt to move graves to nearby free space when possible,
 * and the position may have been redirected by another mod.
 *
 * <p>The {@code ServerLevel} provided represents the dimension in which the player died,
 * which may not be the dimension in which the gravestone was placed.
 * {@link net.minecraft.server.MinecraftServer#getLevel(ResourceKey) MinecraftServer.getLevel}
 * can be used to get the level in which the gravestone was placed.
 *
 * <p>Should not be used for inserting data into graves. If you need to insert data, see {@link GravestoneDataType}
 *
 * <p>Does not have any way to cancel gravestone placement. If you need to cancel placement, see {@link CancelGravestonePlacementCallback}
 *
 * @see GravestoneDataType
 * @see CancelGravestonePlacementCallback
 */
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
