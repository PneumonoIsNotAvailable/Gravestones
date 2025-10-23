package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Callback for each item to check if it should be inserted into a gravestone.
 * Called for each item stack on death.
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
 * For example, items enchanted with Curse of Vanishing are skipped using this event,
 * which leaves them to be processed by vanilla's death mechanics, which clear them from the inventory.
 *
 * <p>The Slot Identifier may be null,
 * if the item stack does not come from a specific slot (e.g. a bundle-like inventory).
 * The Slot Identifier will often need to be converted to another format to be used,
 * make sure to check how the identifier is created in the relevant {@link GravestoneDataType},
 * to be confident it's being parsed correctly.
 *
 * <p>This should not be used to add on-death functionality,
 * as it may not be called if a previous listener already cancelled insertion.
 * To add on-death functionality, use {@link OnInsertItemCallback}
 *
 * @see OnInsertItemCallback
 */
public interface SkipItemCallback {
    Event<SkipItemCallback> EVENT = EventFactory.createArrayBacked(SkipItemCallback.class,
            listeners -> (player, itemStack, slot) -> {
                for (SkipItemCallback listener : listeners) {
                    if (listener.insertItem(player, itemStack, slot)) {
                        return true;
                    }
                }

                return false;
            }
    );

    boolean insertItem(PlayerEntity player, ItemStack itemStack, @Nullable Identifier slot);
}
