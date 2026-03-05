package net.pneumono.gravestones.compat;

import net.pneumono.gravestones.api.SkipItemCallback;

//? if galosphere {
/*import net.orcinus.galosphere.init.GDataComponents;
*///?}

public class GalosphereCompat {
    public static void register() {
        //? if galosphere {
        /*SkipItemCallback.EVENT.register((player, itemStack, slot) -> {
            //? if >=1.20.5 {
            return itemStack.has(GDataComponents.PRESERVED);
            //?} else {
            /^return itemStack.getNbt() != null && itemStack.getNbt().contains("Preserved");
            ^///?}
        });
        *///?}
    }
}
