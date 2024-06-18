package net.pneumono.gravestones.gravestones;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class GravestonePosition {
    public final Identifier dimension;
    public final int posX;
    public final int posY;
    public final int posZ;

    public GravestonePosition(Identifier dimension, BlockPos pos) {
        this.dimension = dimension;
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
    }

    public GravestonePosition() {
        this.dimension = Identifier.ofVanilla("overworld");
        this.posX = 0;
        this.posY = 0;
        this.posZ = 0;
    }

    public BlockPos asBlockPos() {
        return new BlockPos(posX, posY, posZ);
    }
}
