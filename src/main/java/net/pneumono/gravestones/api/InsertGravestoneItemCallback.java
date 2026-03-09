package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * @deprecated Use {@link SkipItemCallback} or {@link OnInsertItemCallback} instead
 */
@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public interface InsertGravestoneItemCallback {
    Event<InsertGravestoneItemCallback> EVENT = EventFactory.createArrayBacked(InsertGravestoneItemCallback.class,
        listeners -> (player, itemStack) -> {
            for (InsertGravestoneItemCallback listener : listeners) {
                if (listener.insertItem(player, itemStack)) {
                    return true;
                }
            }

            return false;
        }
    );

    boolean insertItem(Player player, ItemStack itemStack);
}
