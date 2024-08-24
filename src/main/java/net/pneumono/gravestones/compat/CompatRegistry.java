package net.pneumono.gravestones.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.pneumono.gravestones.api.GravestonesApi;

public class CompatRegistry {
    public static void registerCompat() {
        FabricLoader loader = FabricLoader.getInstance();
        if (loader.isModLoaded("trinkets")) {
            GravestonesApi.registerModSupport(new TrinketsSupport());
        }
        if (loader.isModLoaded("spelunkery")) {
            GravestonesApi.registerModSupport(new SpelunkerySupport());
        }
    }
}
