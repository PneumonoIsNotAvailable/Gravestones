package net.pneumono.gravestones.compat;

import net.minecraft.world.item.Items;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.event.GravestoneContentsEvents;

public class SpelunkeryCompat {
    public static void register() {
        GravestoneContentsEvents.registerSkipItem(Gravestones.id("spelunkery_recovery_compass"), (player, itemStack, slot) -> itemStack.is(Items.RECOVERY_COMPASS));
    }
}
