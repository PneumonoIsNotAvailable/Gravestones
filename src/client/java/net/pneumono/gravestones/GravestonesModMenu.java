package net.pneumono.gravestones;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.pneumono.pneumonocore.config.ConfigOptionsScreen;
import net.pneumono.pneumonocore.config.Configs;

public class GravestonesModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return Configs.hasConfigs(Gravestones.MOD_ID) ? parent -> new ConfigOptionsScreen(parent, Gravestones.MOD_ID) : null;
    }
}
