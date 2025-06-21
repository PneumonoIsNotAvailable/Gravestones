package net.pneumono.gravestones.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.pneumonocore.datagen.PneumonoCoreTranslationBuilder;

import java.util.concurrent.CompletableFuture;

public class GravestonesEnglishLangProvider extends FabricLanguageProvider {
    public GravestonesEnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        PneumonoCoreTranslationBuilder builder = new PneumonoCoreTranslationBuilder(translationBuilder);

        builder.add(GravestonesRegistry.GRAVESTONE, "Gravestone");
        builder.add(GravestonesRegistry.GRAVESTONE_CHIPPED, "Chipped Gravestone");
        builder.add(GravestonesRegistry.GRAVESTONE_DAMAGED, "Damaged Gravestone");
        builder.add(GravestonesRegistry.GRAVESTONE_TECHNICAL, "Gravestone");

        builder.add(GravestonesRegistry.GRAVESTONE.asItem(), "Gravestone");
        builder.add(GravestonesRegistry.GRAVESTONE_CHIPPED.asItem(), "Chipped Gravestone");
        builder.add(GravestonesRegistry.GRAVESTONE_DAMAGED.asItem(), "Damaged Gravestone");

        builder.add("gravestones.cannot_open_wrong_player", "This gravestone can only be interacted with by its owner, %s!");
        builder.add("gravestones.cannot_open_no_owner", "This gravestone has no owner!");
        builder.add("gravestones.player_collected_grave", "%s has found their grave!");
        builder.add("gravestones.player_collected_grave_at_coords", "%1$s has found their grave at %2$s!");
        builder.add("gravestones.grave_spawned", "%1$s's grave is at %2$s");
        builder.add("gravestones.edit_text", "Edit Gravestone Message");
        builder.add("gravestones.position", "(%1$s,%2$s,%3$s) in %4$s");

        builder.add(GravestonesRegistry.GRAVESTONE_SKELETON_ENTITY_TYPE, "Gravestone Skeleton");

        builder.add(GravestonesRegistry.GRAVESTONES_COLLECTED.toTranslationKey("stat"), "Gravestones Collected");

        builder.addSubtitle(GravestonesRegistry.SOUND_BLOCK_WAXED_GRAVESTONE_INTERACT_FAIL, "Gravestone thumps");

        builder.add(GravestonesRegistry.ITEM_SKIPS_GRAVESTONES, "Skips Gravestone Processing");
        builder.add(GravestonesRegistry.ENCHANTMENT_SKIPS_GRAVESTONES, "Skips Gravestone Processing");

        builder.add("commands.gravestones.getdata.gravestone.all_data", "Gravestone has a spawnDate of %1$s and a graveOwner of %2$s (%3$s)");
        builder.add("commands.gravestones.getdata.gravestone.no_grave_owner", "Gravestone has a spawnDate of %s and no graveOwner!");
        builder.add("commands.gravestones.getdata.gravestone.no_gravestone", "No gravestone at that position!");
        builder.add("commands.gravestones.getdata.gravestone.contents_data", "Gravestone has the following contents data: %s");
        builder.add("commands.gravestones.getdata.player.grave_data", "%1$s has graves at the following locations: %2$s, %3$s, %4$s");
        builder.add("commands.gravestones.getdata.player.cannot_read", "Could not read gravestones data file!");
        builder.add("commands.gravestones.getdata.player.cannot_find", "Could not find gravestones data file!");
        builder.add("commands.gravestones.deaths.view", "Inventory: %s");
        builder.add("commands.gravestones.deaths.recover", "Recovered inventory from death");
        builder.add("commands.gravestones.getuuid", "%1$s has UUID %2$s");

        builder.addConfigScreenTitle(Gravestones.MOD_ID, "Gravestones Configs");
        builder.addConfig(GravestonesConfig.AESTHETIC_GRAVESTONES,
                "Aesthetic Gravestones",
                "Whether or not aesthetic gravestones can be crafted for building purposes"
        );
        builder.addConfig(GravestonesConfig.DECAY_WITH_TIME,
                "Time Decay",
                "Whether or not gravestones are damaged over time"
        );
        builder.addConfig(GravestonesConfig.DECAY_WITH_DEATHS,
                "Death Decay",
                "Whether or not gravestones are damaged from further deaths"
        );
        builder.addConfig(GravestonesConfig.DECAY_TIME,
                "Time to Decay",
                "The amount of time it takes for gravestones to be damaged by time (if enabled)"
        );
        builder.addEnumConfig(GravestonesConfig.DECAY_TIME_TYPE,
                "Decay Time Measurement" ,
                "Whether decay time is based on real-world time (Real Time), or time the server is active (Ticks). Gravestone decay will occur regardless of whether chunks are loaded",
                "Ticks",
                "Real Time"
        );
        builder.addConfig(GravestonesConfig.STORE_EXPERIENCE,
                "Store Experience",
                "Whether or not graves store experience. If not, experience is dropped on the ground on death"
        );
        builder.addConfig(GravestonesConfig.EXPERIENCE_CAP,
                "Experience Cap",
                "Whether or not graves have a limit to how much experience they can store. This limit is the same as with vanilla deaths (100 points)"
        );
        builder.addEnumConfig(GravestonesConfig.EXPERIENCE_KEPT,
                "Experience Kept",
                "How experience kept on death is calculated. Either no experience is lost, a fraction is kept, or it uses the vanilla calculation (7 * current level)",
                "All (100%)",
                "3/4 (75%)",
                "2/3 (66%)",
                "1/2 (50%)",
                "1/3 (33%)",
                "1/4 (25%)",
                "Vanilla"
        );
        builder.addConfig(GravestonesConfig.EXPERIENCE_DECAY,
                "Experience Decay",
                "Whether or not grave decay affects the experience stored in the grave (if enabled). If so, experience dropped is divided by the decay stage (stage 1 drops 100%, stage 2 drops 50%, and stage 3 drops 33%)"
        );
        builder.addConfig(GravestonesConfig.GRAVESTONE_ACCESSIBLE_OWNER_ONLY,
                "Owner-Only Access",
                "Whether or not players who aren't the owner are prevented from accessing a grave's items"
        );
        builder.addConfig(GravestonesConfig.SPAWN_GRAVESTONE_SKELETONS,
                "Spawn Skeletons",
                "Whether or not gravestones spawn skeletons when their owner is near"
        );
        builder.addConfig(GravestonesConfig.BROADCAST_COLLECT_IN_CHAT,
                "Broadcast Collection",
                "Whether or not grave collection is broadcasted in chat"
        );
        builder.addConfig(GravestonesConfig.BROADCAST_COORDINATES_IN_CHAT,
                "Broadcast Coordinates",
                "Whether or not the coordinates of placed graves are broadcasted in chat"
        );
        builder.addConfig(GravestonesConfig.CONSOLE_INFO,
                "Console Info",
                "Whether or not highly detailed information about graves is recorded in console. This is mostly just for debugging!"
        );
        builder.addEnumConfig(GravestonesConfig.TIME_FORMAT,
                "Time Format",
                "The format of the death time shown on gravestones",
                "DD/MM/YYYY",
                "MM/DD/YYYY",
                "YYYY/MM/DD"
        );
        builder.add("configs.category.gravestones.decay", "Gravestone Decay");
        builder.add("configs.category.gravestones.experience", "Experience Storage");
        builder.add("configs.category.gravestones.multiplayer", "Multiplayer");

        builder.add("modmenu.nameTranslation.gravestones", "Gravestones");
        builder.add("modmenu.descriptionTranslation.gravestones", "Adds Gravestones that store items after death.");
        builder.add("modmenu.summaryTranslation.gravestones", "Adds Gravestones that store items after death.");
    }
}
