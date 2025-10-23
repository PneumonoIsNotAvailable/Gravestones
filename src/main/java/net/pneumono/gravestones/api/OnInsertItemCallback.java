package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Callback for each item before being inserted into a gravestone.
 * Called for each item stack, including stacks that will be skipped.
 *
 * <p>This is ideal for items with on-death functionality that should still be inserted into the gravestone.
 * For example, a version of a Totem Of Undying that can be used multiple times,
 * or an item that loses durability on death.
 *
 * <p>The Slot Identifier may be null,
 * if the item stack does not come from a specific slot (e.g. a bundle-like inventory).
 * The Slot Identifier will often need to be converted to another format to be used,
 * make sure to check how the identifier is created in the relevant {@link GravestoneDataType},
 * to be confident it's being parsed correctly.
 *
 * <p>For items with existing on-death functionality that should not be inserted into the gravestone,
 * it may be better to skip the item using tags (see {@link GravestonesApi}) or {@link SkipItemCallback},
 * so that code does not need to be duplicated and to prevent the risk of code running multiple times.
 *
 * @see SkipItemCallback
 */
public interface OnInsertItemCallback {
    Event<OnInsertItemCallback> EVENT = EventFactory.createArrayBacked(OnInsertItemCallback.class,
            listeners -> (player, itemStack, slot) -> {
                for (OnInsertItemCallback listener : listeners) {
                    listener.insertItem(player, itemStack, slot);
                }
            }
    );

    void insertItem(PlayerEntity player, ItemStack itemStack, Identifier slot);
}
