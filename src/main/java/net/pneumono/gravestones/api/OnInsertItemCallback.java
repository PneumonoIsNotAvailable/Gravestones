package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pneumono.gravestones.api.event.GravestoneContentsEvents;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link GravestoneContentsEvents.InsertItem} instead.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated(forRemoval = true)
public interface OnInsertItemCallback {
    Event<OnInsertItemCallback> EVENT = EventFactory.createArrayBacked(OnInsertItemCallback.class,
            listeners -> (player, itemStack, slot) -> {
                for (OnInsertItemCallback listener : listeners) {
                    listener.insertItem(player, itemStack, slot);
                }
            }
    );

    void insertItem(Player player, ItemStack itemStack, @Nullable Identifier slot);
}
