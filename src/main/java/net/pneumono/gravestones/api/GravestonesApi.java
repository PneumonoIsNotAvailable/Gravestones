package net.pneumono.gravestones.api;

import java.util.ArrayList;
import java.util.List;

public class GravestonesApi {
    private static final List<ModSupport> modSupports = new ArrayList<>();

    public static List<ModSupport> getModSupports() {
        return modSupports;
    }

    /**
     * Registers a list of instructions (ModSupport instance) so that data from other mods can also be saved into gravestones if they are present.
     *
     * @param support ModSupport instance
     */
    @SuppressWarnings("unused")
    public static void registerModSupport(ModSupport support) {
        modSupports.add(support);
    }
}
