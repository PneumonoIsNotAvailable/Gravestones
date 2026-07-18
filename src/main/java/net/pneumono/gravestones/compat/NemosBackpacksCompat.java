package net.pneumono.gravestones.compat;

import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestonesApi;

public class NemosBackpacksCompat {
    public static void register() {
        //? if nemos_backpacks {
        GravestonesApi.registerDataType(Gravestones.id("nemos_backpacks"), new NemosBackpacksDataType());
        //?}
    }
}
