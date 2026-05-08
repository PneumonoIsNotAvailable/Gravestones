package net.pneumono.gravestones.compat;

import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestonesApi;

public class BackpackedCompat {
    public static void register() {
        //? if backpacked {
        GravestonesApi.registerDataType(Gravestones.id("backpacked"), new BackpackedDataType());
        //?}
    }
}
