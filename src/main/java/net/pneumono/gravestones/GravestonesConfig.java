package net.pneumono.gravestones;

import net.pneumono.gravestones.gravestones.enums.DecayTimeType;
import net.pneumono.gravestones.gravestones.enums.ExperienceKeptCalculation;
import net.pneumono.gravestones.gravestones.enums.TimeFormat;
import net.pneumono.pneumonocore.config.*;

public class GravestonesConfig {
    public static final BooleanConfiguration AESTHETIC_GRAVESTONES = new BooleanConfiguration(Gravestones.MOD_ID, "aesthetic_gravestones", ConfigEnv.SERVER, true);
    public static final BooleanConfiguration DECAY_WITH_TIME = new BooleanConfiguration(Gravestones.MOD_ID, "decay_with_time", ConfigEnv.SERVER, true);
    public static final BooleanConfiguration DECAY_WITH_DEATHS = new BooleanConfiguration(Gravestones.MOD_ID, "decay_with_deaths", ConfigEnv.SERVER, true);
    public static final TimeConfiguration DECAY_TIME = new TimeConfiguration(Gravestones.MOD_ID, "decay_time", ConfigEnv.SERVER, 8L * TimeUnit.HOURS.getDivision());
    public static final EnumConfiguration<DecayTimeType> DECAY_TIME_TYPE = new EnumConfiguration<>(Gravestones.MOD_ID, "decay_time_type", ConfigEnv.SERVER, DecayTimeType.TICKS);
    public static final BooleanConfiguration STORE_EXPERIENCE = new BooleanConfiguration(Gravestones.MOD_ID, "store_experience", ConfigEnv.SERVER, true);
    public static final BooleanConfiguration EXPERIENCE_CAP = new BooleanConfiguration(Gravestones.MOD_ID, "experience_cap", ConfigEnv.SERVER, true);
    public static final EnumConfiguration<ExperienceKeptCalculation> EXPERIENCE_KEPT = new EnumConfiguration<>(Gravestones.MOD_ID, "experience_kept", ConfigEnv.SERVER, ExperienceKeptCalculation.VANILLA);
    public static final BooleanConfiguration EXPERIENCE_DECAY = new BooleanConfiguration(Gravestones.MOD_ID, "experience_decay", ConfigEnv.SERVER, false);
    public static final BooleanConfiguration GRAVESTONE_ACCESSIBLE_OWNER_ONLY = new BooleanConfiguration(Gravestones.MOD_ID, "gravestone_accessible_owner_only", ConfigEnv.SERVER, true);
    public static final BooleanConfiguration SPAWN_GRAVESTONE_SKELETONS = new BooleanConfiguration(Gravestones.MOD_ID, "spawn_gravestone_skeletons", ConfigEnv.SERVER, false);
    public static final BooleanConfiguration BROADCAST_COLLECT_IN_CHAT = new BooleanConfiguration(Gravestones.MOD_ID, "broadcast_collect_in_chat", ConfigEnv.SERVER, false);
    public static final BooleanConfiguration BROADCAST_COORDINATES_IN_CHAT = new BooleanConfiguration(Gravestones.MOD_ID, "broadcast_coordinates_in_chat", ConfigEnv.SERVER, false);
    public static final BooleanConfiguration CONSOLE_INFO = new BooleanConfiguration(Gravestones.MOD_ID, "console_info", ConfigEnv.CLIENT, false);
    public static final EnumConfiguration<TimeFormat> TIME_FORMAT = new EnumConfiguration<>(Gravestones.MOD_ID, "time_format", ConfigEnv.CLIENT, TimeFormat.MMDDYYYY);

    public static void registerGravestonesConfigs() {
        Configs.register(Gravestones.MOD_ID,
                AESTHETIC_GRAVESTONES,
                DECAY_WITH_TIME,
                DECAY_WITH_DEATHS,
                DECAY_TIME,
                DECAY_TIME_TYPE,
                STORE_EXPERIENCE,
                EXPERIENCE_CAP,
                EXPERIENCE_KEPT,
                EXPERIENCE_DECAY,
                GRAVESTONE_ACCESSIBLE_OWNER_ONLY,
                SPAWN_GRAVESTONE_SKELETONS,
                BROADCAST_COLLECT_IN_CHAT,
                BROADCAST_COORDINATES_IN_CHAT,
                CONSOLE_INFO,
                TIME_FORMAT
        );
        Configs.registerCategories(Gravestones.MOD_ID,
                new ConfigCategory(Gravestones.MOD_ID, "decay",
                        DECAY_WITH_TIME,
                        DECAY_WITH_DEATHS,
                        DECAY_TIME,
                        DECAY_TIME_TYPE
                ),
                new ConfigCategory(Gravestones.MOD_ID, "experience",
                        STORE_EXPERIENCE,
                        EXPERIENCE_CAP,
                        EXPERIENCE_KEPT,
                        EXPERIENCE_DECAY
                ),
                new ConfigCategory(Gravestones.MOD_ID, "multiplayer",
                        GRAVESTONE_ACCESSIBLE_OWNER_ONLY,
                        BROADCAST_COLLECT_IN_CHAT,
                        BROADCAST_COORDINATES_IN_CHAT
                )
        );
    }
}
