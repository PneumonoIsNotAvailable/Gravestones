package net.pneumono.gravestones.gravestones;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.pneumono.gravestones.GravestonesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GravestoneManager {
    protected static final Logger LOGGER = LoggerFactory.getLogger("Gravestones Debug");

    public static void info(String string) {
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            LOGGER.info(string);
        }
    }

    public static void warn(String string) {
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            LOGGER.warn(string);
        }
    }

    public static void error(String string) {
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            LOGGER.error(string);
        }
    }

    public static void error(String string, Throwable t) {
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            LOGGER.error(string, t);
        }
    }

    public static String posToString(BlockPos pos) {
        return "(" + pos.toShortString() + ")";
    }

    public static Text posToText(GlobalPos global) {
        BlockPos pos = global.pos();
        return Text.translatable("gravestones.position", pos.getX(), pos.getY(), pos.getZ(), global.dimension().getValue().toString());
    }
}
