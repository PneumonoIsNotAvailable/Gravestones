package net.pneumono.gravestones.gravestones;

import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;

public abstract class GravestonesManager {
    public static void info(String string) {
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            Gravestones.LOGGER.info(string);
        }
    }

    public static void warn(String string) {
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            Gravestones.LOGGER.warn(string);
        }
    }

    public static void error(String string) {
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            Gravestones.LOGGER.error(string);
        }
    }

    public static void error(String string, Throwable t) {
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            Gravestones.LOGGER.error(string, t);
        }
    }
}
