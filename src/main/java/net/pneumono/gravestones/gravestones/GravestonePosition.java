package net.pneumono.gravestones.gravestones;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class GravestonePosition {
    public final Identifier dimension;
    public final int posX;
    public final int posY;
    public final int posZ;

    public GravestonePosition(Identifier dimension, BlockPos pos) {
        if (dimension == null || pos == null) {
            this.dimension = new Identifier("overworld");
            this.posX = 0;
            this.posY = 0;
            this.posZ = 0;
        } else {
            this.dimension = dimension;
            this.posX = pos.getX();
            this.posY = pos.getY();
            this.posZ = pos.getZ();
        }
    }

    public GravestonePosition() {
        this.dimension = new Identifier("overworld");
        this.posX = 0;
        this.posY = 0;
        this.posZ = 0;
    }

    public BlockPos asBlockPos() {
        return new BlockPos(posX, posY, posZ);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof GravestonePosition pos) {
            return dimension.equals(pos.dimension) && posX == pos.posX && posY == pos.posY && posZ == pos.posZ;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, posX, posY, posZ);
    }
}
