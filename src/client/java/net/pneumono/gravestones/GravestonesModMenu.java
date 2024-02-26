package net.pneumono.gravestones;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.pneumono.pneumonocore.PneumonoCoreModMenu;

public class GravestonesModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return PneumonoCoreModMenu.getModConfigScreenFactory(Gravestones.MOD_ID);
    }
}
