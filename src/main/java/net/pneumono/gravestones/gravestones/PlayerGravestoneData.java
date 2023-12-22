package net.pneumono.gravestones.gravestones;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerGravestoneData {
    public UUID owner;
    public GravestonePosition firstGrave;
    public GravestonePosition secondGrave;
    public GravestonePosition thirdGrave;

    public PlayerGravestoneData(UUID owner, GravestonePosition pos) {
        this.owner = owner;
        firstGrave = pos;
        secondGrave = new GravestonePosition();
        thirdGrave = new GravestonePosition();
    }

    public List<GravestonePosition> getPositionsAsList() {
        List<GravestonePosition> list = new ArrayList<>();
        list.add(firstGrave);
        list.add(secondGrave);
        list.add(thirdGrave);
        return list;
    }

    public UUID getOwner() {
        return owner;
    }

    public void shiftGraves(GravestonePosition pos) {
        thirdGrave = secondGrave;
        secondGrave = firstGrave;
        firstGrave = pos;
    }
}
