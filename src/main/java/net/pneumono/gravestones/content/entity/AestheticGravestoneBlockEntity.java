package net.pneumono.gravestones.content.entity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pneumono.gravestones.content.GravestonesRegistry;

public class AestheticGravestoneBlockEntity extends AbstractGravestoneBlockEntity {
    public AestheticGravestoneBlockEntity(BlockPos pos, BlockState state) {
        super(GravestonesRegistry.AESTHETIC_GRAVESTONE_ENTITY, pos, state);
    }

    private final String[] lines = new String[]{"", "", "", ""};

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putString("line_0", lines[0]);
        nbt.putString("line_1", lines[1]);
        nbt.putString("line_2", lines[2]);
        nbt.putString("line_3", lines[3]);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("line_0", NbtElement.STRING_TYPE)) {
            lines[0] = nbt.getString("line_0");
        }
        if (nbt.contains("line_1", NbtElement.STRING_TYPE)) {
            lines[1] = nbt.getString("line_1");
        }
        if (nbt.contains("line_2", NbtElement.STRING_TYPE)) {
            lines[2] = nbt.getString("line_2");
        }
        if (nbt.contains("line_3", NbtElement.STRING_TYPE)) {
            lines[3] = nbt.getString("line_3");
        }
    }

    @Override
    public Direction getGravestoneDirection() {
        World world = getWorld();
        if (world != null) {
            BlockState state = world.getBlockState(getPos());
            if (state.getProperties().contains(Properties.HORIZONTAL_FACING)) {
                return state.get(Properties.HORIZONTAL_FACING);
            }
        }
        return Direction.NORTH;
    }

    @Override
    public String getGravestoneTextLine(int line) {
        return lines[line];
    }
}
