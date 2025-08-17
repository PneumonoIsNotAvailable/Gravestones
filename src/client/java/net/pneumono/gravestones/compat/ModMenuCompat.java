package net.pneumono.gravestones.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.pneumonocore.PneumonoCoreModMenu;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return PneumonoCoreModMenu.getModConfigScreenFactory(Gravestones.MOD_ID);
    }
}
