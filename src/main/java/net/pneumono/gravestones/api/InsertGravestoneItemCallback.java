package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Callback for inserting items into gravestones. Checked for each item stack in the player's inventory on death.
 *
 * <p>Returning {@code true} cancels further processing,
 * and causes the gravestone to skip moving this item stack from the player's inventory to the gravestone.
 * Returning {@code false} falls back to further processing.
 *
 * <p>If an item stack is skipped, the gravestone ignores it completely,
 * and it is treated as it would be if gravestones was not installed (which is usually being dropped on the ground).
 *
 * <p>This is ideal for items with existing on-death functionality,
 * since they are still processed as normal after being skipped.
 * For example, items enchanted with Curse of Vanishing are skipped using this,
 * which leaves them to be processed by vanilla's death mechanics, which clear them from the inventory.
 *
 * <p>This can also be used to modify items on death, even if they should still be inserted into the gravestone.
 * For example, an item that decrements by 1 on death.
 * Items can also be removed similarly to Curse of Vanishing in this way.
 */
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

    boolean insertItem(PlayerEntity player, ItemStack itemStack);
}
