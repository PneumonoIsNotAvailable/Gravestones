package net.pneumono.gravestones.compat;

import net.minecraft.world.item.Items;
import net.pneumono.gravestones.api.SkipItemCallback;

public class SpelunkeryCompat {
    public static void register() {
        SkipItemCallback.EVENT.register((player, itemStack, slot) -> itemStack.is(Items.RECOVERY_COMPASS));
    }
}
