package net.pneumono.gravestones.compat;

import net.pneumono.gravestones.api.SkipItemCallback;

//? if galosphere {
/*import net.orcinus.galosphere.init.GDataComponents;
 *///?}

public class GalosphereCompat {
    public static void register() {
        SkipItemCallback.EVENT.register((player, itemStack, slot) -> {
            //? if >=1.20.5 {
            //? if galosphere {
            /*return itemStack.contains(GDataComponents.PRESERVED);
             *///?} else {
            return false;
            //?}
            //?} else {
            /*return itemStack.getNbt() != null && itemStack.getNbt().contains("Preserved");
             *///?}
        });
    }
}
