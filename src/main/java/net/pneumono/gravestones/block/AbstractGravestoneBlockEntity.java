package net.pneumono.gravestones.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractGravestoneBlockEntity extends BlockEntity {
    public AbstractGravestoneBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    //? if >=1.20.5 {
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return this.saveCustomOnly(registryLookup);
    }
    //?} else {
    /*public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
    *///?}

    public abstract Direction getGravestoneDirection();
}
