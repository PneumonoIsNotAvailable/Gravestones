package net.pneumono.gravestones.gravestones;

import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;

import java.util.ArrayList;
import java.util.List;

public class GravestoneHistory {
    public static final Codec<GravestoneHistory> CODEC = GlobalPos.CODEC.listOf()
            .xmap(GravestoneHistory::new, GravestoneHistory::getPositions);

    private final List<GlobalPos> positions;

    public GravestoneHistory(List<GlobalPos> positions) {
        this.positions = new ArrayList<>(positions);
    }

    public GravestoneHistory() {
        this(new ArrayList<>());
    }

    public List<GlobalPos> getPositions() {
        return this.positions;
    }
}
