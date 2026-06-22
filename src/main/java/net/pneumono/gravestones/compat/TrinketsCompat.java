package net.pneumono.gravestones.compat;

import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestonesApi;

public class TrinketsCompat {
    public static void register() {
        GravestonesApi.registerDataType(Gravestones.id("trinkets"), new TrinketsDataType());
    }
}
