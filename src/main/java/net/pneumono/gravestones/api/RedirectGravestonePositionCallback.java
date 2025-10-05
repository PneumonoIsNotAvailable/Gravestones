package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.GlobalPos;
import net.pneumono.gravestones.gravestones.GravestonePlacement;
import net.pneumono.gravestones.multiversion.VersionUtil;

/**
 * Callback for changing the placement position of gravestones.
 *
 * <p>Returning a {@linkplain GravestonePlacement#isInvalid(BlockState) valid} {@link GlobalPos}
 * stops further processing and places the gravestone at that position.
 * To fall back to further processing, return {@code null}.
 *
 * <p>This is ideal for mods that do not want items dropped at the player's death position.
 * For example, a roguelike dungeon mod would need to place the gravestone somewhere at the entrance of the dungeon,
 * so that items aren't lost when the dungeon is reset.
 *
 * <p>{@code GlobalPos} has a {@code dimension} field,
 * which should be used if the gravestone should be redirected to another dimension.
 * The {@code ServerWorld} provided represents the dimension in which the player died.
 *
 * <p>If you want to avoid breaking blocks at your target position,
 * {@link GravestonePlacement#getValidPos}
 * can be used to find the best nearby position.
 * Remember that it may return null if it does not find a valid position.
 * If the gravestone is placed at a position with a block, the block will still drop as an item when broken,
 * so overwriting blocks is acceptable (although not preferred).
 *
 * <p>Do not assume that this will always be called, as previous listeners may have returned a value.
 * If you want to do something when gravestones are placed, see {@link GravestonePlacedCallback}.
 *
 * <p>This event should not be used to cancel gravestone placement.
 * If you need to cancel placement, see {@link CancelGravestonePlacementCallback}.
 *
 * @see GravestonePlacedCallback
 * @see CancelGravestonePlacementCallback
 */
public interface RedirectGravestonePositionCallback {
    Event<RedirectGravestonePositionCallback> EVENT = EventFactory.createArrayBacked(RedirectGravestonePositionCallback.class,
            listeners -> (world, player, deathPos) -> {
                GlobalPos placementPos = null;

                for (RedirectGravestonePositionCallback listener : listeners) {
                    placementPos = listener.redirectPosition(world, player, deathPos);
                    if (GravestonePlacement.isInvalid(world.getBlockState(VersionUtil.getPos(placementPos)))) placementPos = null;
                    if (placementPos != null) break;
                }

                return placementPos;
            }
    );

    GlobalPos redirectPosition(ServerWorld world, PlayerEntity player, GlobalPos deathPos);
}
