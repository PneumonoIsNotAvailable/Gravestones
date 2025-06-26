package net.pneumono.gravestones.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.pneumonocore.datagen.PneumonoCoreTranslationBuilder;

import java.util.concurrent.CompletableFuture;

public class GravestonesLolcatLangProvider extends FabricLanguageProvider {
    public GravestonesLolcatLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "lol_us", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        PneumonoCoreTranslationBuilder builder = new PneumonoCoreTranslationBuilder(translationBuilder);

        builder.add(GravestonesRegistry.GRAVESTONE, "Die box");
        builder.add(GravestonesRegistry.GRAVESTONE_CHIPPED, "Half brokn die box");
        builder.add(GravestonesRegistry.GRAVESTONE_DAMAGED, "Very brokn die box!!");
        builder.add(GravestonesRegistry.GRAVESTONE_TECHNICAL, "Die box");

        builder.add(GravestonesRegistry.GRAVESTONE.asItem(), "Die box");
        builder.add(GravestonesRegistry.GRAVESTONE_CHIPPED.asItem(), "Half brokn die box");
        builder.add(GravestonesRegistry.GRAVESTONE_DAMAGED.asItem(), "Very brokn die box!!");

        builder.add("gravestones.cannot_open_wrong_player", "Ownly %s kan open da die box!");
        builder.add("gravestones.cannot_open_no_owner", "Die box has no kitteh!");
        builder.add("gravestones.player_collected_grave", "%s got bak die box!!");
        builder.add("gravestones.player_collected_grave_at_coords", "%1$s got bak die box at %2$s!!");
        builder.add("gravestones.grave_spawned", "%1$s lefty die box att %2$s!");
        builder.add("gravestones.edit_text", "Chaenj die box mesag");
        builder.add("gravestones.position", "(%1$s,%2$s,%3$s) in %4$s");

        builder.add(GravestonesRegistry.GRAVESTONE_SKELETON_ENTITY_TYPE, "Die box Skeletun");

        builder.add(GravestonesRegistry.GRAVESTONES_COLLECTED.toTranslationKey("stat"), "Die boxz got bak");

        builder.addSubtitle(GravestonesRegistry.SOUND_BLOCK_WAXED_GRAVESTONE_INTERACT_FAIL, "Die box go thump");

        builder.add(GravestonesRegistry.ITEM_SKIPS_GRAVESTONES, "Not 4 die box!");
        builder.add(GravestonesRegistry.ENCHANTMENT_SKIPS_GRAVESTONES, "Not 4 die box!");

        builder.add("commands.gravestones.getdata.gravestone.all_data", "Die box waz made %1$s by %2$s (%3$s) dyin");
        builder.add("commands.gravestones.getdata.gravestone.no_grave_owner", "Die box waz made %s but no cat ownah!");
        builder.add("commands.gravestones.getdata.gravestone.no_gravestone", "No die box ther!!");
        builder.add("commands.gravestones.getdata.gravestone.contents_data", "Die box haz codey numbrz: %s");
        builder.add("commands.gravestones.getdata.player.grave_data", "%1$s haz die boxz arund: %2$s, %3$s, %4$s");
        builder.add("commands.gravestones.getdata.player.cannot_read", "Kitteh cant reed die box fiel!");
        builder.add("commands.gravestones.getdata.player.cannot_find", "Kitteh cant find die box fiel!");
        builder.add("commands.gravestones.deaths.view", "Itemz: %s");
        builder.add("commands.gravestones.deaths.recover", "Got stuffs bak");
        builder.add("commands.gravestones.getuuid", "%1$s haz cat ID %2$s");

        builder.addConfigScreenTitle(Gravestones.MOD_ID, "Die box settinz");
        builder.addConfig(GravestonesConfig.AESTHETIC_GRAVESTONES,
                "Pretteh die boxz",
                "If I can haz pretteh die boxz 4 dekrait littrbox"
        );
        builder.addConfig(GravestonesConfig.DECAY_WITH_TIME,
                "Die box gets old",
                "If die boxz die frm old"
        );
        builder.addConfig(GravestonesConfig.DECAY_WITH_DEATHS,
                "Die box ouchy when u die",
                "If die boxz ouchy more when u die agen"
        );
        builder.addConfig(GravestonesConfig.DECAY_TIME,
                "Olding r8",
                "How much long it takez 4 die box to old"
        );
        builder.addEnumConfig(GravestonesConfig.DECAY_TIME_TYPE,
                "how keep trak?" ,
                "If oldness mezurd by reel-life (bad), or by fleas and tiks (also bad). Die boxz still gets old when kittehs not near!",
                "Tiks",
                "Reel life"
        );
        builder.addConfig(GravestonesConfig.STORE_EXPERIENCE,
                "Rember levlz",
                "If die boxz has ur levlz in dem. If not ur expee wil go on floor!!"
        );
        builder.addConfig(GravestonesConfig.EXPERIENCE_CAP,
                "Kewl limit",
                "If die boxz only saiv some of ur levlz. Kewlnes limit is da same as norml dies (100 points)"
        );
        builder.addEnumConfig(GravestonesConfig.EXPERIENCE_KEPT,
                "Levl keeping amownt",
                "How ur levlz kept gets knowed. Kan use frakshuns or da normal numberz (7 * currnt lvelz)",
                "ALL OF DEM (100%)",
                "3/4 (75%)",
                "2/3 (66%)",
                "1/2 (50%)",
                "1/3 (33%)",
                "1/4 (25%)",
                "norml"
        );
        builder.addConfig(GravestonesConfig.EXPERIENCE_DECAY,
                "Expee gets old 2!??",
                "If die box olding takes away som of ur levz. Normly dropz 100%, but wen ouchied dropz 50%, and wen almost dyin dropz 33%)"
        );
        builder.addConfig(GravestonesConfig.GRAVESTONE_ACCESSIBLE_OWNER_ONLY,
                "STEALINGG",
                "If othr kittehz kan steel ur stuffs!!"
        );
        builder.addConfig(GravestonesConfig.SPAWN_GRAVESTONE_SKELETONS,
                "spuky skary skeletuns",
                "If die boxz make skeletun frends sometiems"
        );
        builder.addConfig(GravestonesConfig.BROADCAST_COLLECT_IN_CHAT,
                "evry1 sees collekt",
                "If gettin ur stuffs bak gets told 2 evry1"
        );
        builder.addConfig(GravestonesConfig.BROADCAST_COORDINATES_IN_CHAT,
                "evry1 sees die place",
                "If dyin means evry1 sees wher u dieded"
        );
        builder.addConfig(GravestonesConfig.CONSOLE_INFO,
                "Clevr knowings",
                "Putz smart kitteh knowings in ur consol. FOR CLEVR COED CATS ONLY!!"
        );
        builder.addEnumConfig(GravestonesConfig.TIME_FORMAT,
                "Tiem way",
                "Wat way u tel da tiem",
                "DD/MM/YYYY",
                "MM/DD/YYYY",
                "YYYY/MM/DD"
        );
        builder.add("configs.category.gravestones.decay", "Die box oldin");
        builder.add("configs.category.gravestones.experience", "Kewlness");
        builder.add("configs.category.gravestones.multiplayer", "Wiv othr catz");

        builder.add("modmenu.nameTranslation.gravestones", "Die boxz!!!");
        builder.add("modmenu.descriptionTranslation.gravestones", "Addz die boxz 2 stor ur stuffz wen u die!");
        builder.add("modmenu.summaryTranslation.gravestones", "Addz die boxz 2 stor ur stuffz wen u die!");
    }
}
