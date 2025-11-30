package net.pneumono.gravestones.compat;

import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestonesApi;

public class AccessoriesCompat {
    public static void register() {
        InventoryModManager.INSTANCE.registerInventoryMod(new AccessoriesDataType(), Gravestones.id("accessories"));
    }
}
