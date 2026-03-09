package net.pneumono.gravestones.gravestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.multiversion.VersionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GravestoneManager {
    protected static final Logger LOGGER = LoggerFactory.getLogger("Gravestones Debug");
    private static boolean consoleInfo = false;

    public static void checkConsoleInfoConfig() {
        consoleInfo = GravestonesConfig.CONSOLE_INFO.getValue();
    }

    public static boolean isUsingDebug() {
        return consoleInfo;
    }

    public static void info(String string) {
        if (consoleInfo) {
            LOGGER.info(string);
        }
    }

    public static void info(String string, Object... objects) {
        if (consoleInfo) {
            LOGGER.info(string, objects);
        }
    }

    public static void warn(String string) {
        if (consoleInfo) {
            LOGGER.warn(string);
        }
    }

    public static void warn(String string, Object... objects) {
        if (consoleInfo) {
            LOGGER.warn(string, objects);
        }
    }

    public static void error(String string) {
        if (consoleInfo) {
            LOGGER.error(string);
        }
    }

    public static void error(String string, Object... objects) {
        if (consoleInfo) {
            LOGGER.error(string, objects);
        }
    }

    public static void error(String string, Throwable t) {
        if (consoleInfo) {
            LOGGER.error(string, t);
        }
    }

    public static String posToString(BlockPos pos) {
        return "(" + pos.toShortString() + ")";
    }

    public static String posToString(GlobalPos global) {
        return posToString(global.pos()) + " in " + VersionUtil.getId(global.dimension());
    }

    public static Component posToText(GlobalPos global) {
        BlockPos pos = global.pos();
        return Component.translatable("gravestones.position", pos.getX(), pos.getY(), pos.getZ(), VersionUtil.getId(global.dimension()).toString());
    }
}
