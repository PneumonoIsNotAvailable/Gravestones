package net.pneumono.gravestones.compat;

import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestonesApi;

public class ResourceBackpacksCompat {
    public static void register() {
        //? if resource_backpacks {
        GravestonesApi.registerDataType(Gravestones.id("resource_backpacks"), new ResourceBackpacksDataType());
        //?}
    }
}
