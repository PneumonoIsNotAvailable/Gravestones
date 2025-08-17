package net.pneumono.gravestones.compat;

import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestonesApi;

public class AccessoriesCompat {
    public static void register() {
        Gravestones.LOGGER.info("Initializing Accessories compat");
        GravestonesApi.registerDataType(Gravestones.id("accessories"), new AccessoriesDataType());
    }
}
