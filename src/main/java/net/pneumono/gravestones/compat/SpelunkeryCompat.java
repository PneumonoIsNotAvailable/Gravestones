package net.pneumono.gravestones.compat;

import net.minecraft.item.Items;
import net.pneumono.gravestones.api.SkipItemCallback;

public class SpelunkeryCompat {
    public static void register() {
        SkipItemCallback.EVENT.register((player, itemStack, slot) -> itemStack.isOf(Items.RECOVERY_COMPASS));
    }
}
