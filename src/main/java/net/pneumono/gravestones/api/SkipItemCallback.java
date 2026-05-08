package net.pneumono.gravestones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pneumono.gravestones.api.event.GravestoneContentsEvents;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link GravestoneContentsEvents.SkipItem} instead.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated(forRemoval = true)
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

    boolean insertItem(Player player, ItemStack itemStack, @Nullable Identifier slot);
}
