package net.pneumono.gravestones.compat;

import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.pneumonocore.PneumonoCore;

public class TrinketsCompat {
    public static void register() {
        GravestonesApi.registerDataType(PneumonoCore.identifier("trinkets"), new TrinketsDataType());
    }
}
