package net.pneumono.gravestones.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.pneumono.gravestones.api.GravestonesApi;

public class CompatRegistry {
    public static void registerCompat() {
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            GravestonesApi.registerModSupport(new TrinketsSupport());
        }
    }
}
