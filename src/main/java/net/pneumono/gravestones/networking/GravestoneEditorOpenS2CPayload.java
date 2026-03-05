package net.pneumono.gravestones.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.pneumono.gravestones.Gravestones;

//? if >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
//?} else {
/*import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
*///?}

//? if >=1.20.2
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record GravestoneEditorOpenS2CPayload(BlockPos pos) /*? if >=1.20.2 {*/implements CustomPacketPayload/*?}*/ {
    public static final Identifier ID = Gravestones.id("gravestone_editor_open");

    //? if >=1.20.5 {
    public static final CustomPacketPayload.Type<GravestoneEditorOpenS2CPayload> PAYLOAD_ID = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, GravestoneEditorOpenS2CPayload> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, GravestoneEditorOpenS2CPayload::pos, GravestoneEditorOpenS2CPayload::new);
    //?} else {
    /*public static final Codec<GravestoneEditorOpenS2CPayload> CODEC = BlockPos.CODEC.xmap(GravestoneEditorOpenS2CPayload::new, GravestoneEditorOpenS2CPayload::pos);
    *///?}

    //? if >=1.20.5 {
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PAYLOAD_ID;
    }
    //?} else if <1.20.5 {
    /*public static GravestoneEditorOpenS2CPayload read(FriendlyByteBuf buf) {
        return new GravestoneEditorOpenS2CPayload(buf.readBlockPos());
    }

    public FriendlyByteBuf write() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        write(buf);
        return buf;
    }

    //? if >=1.20.2 {
    @Override
     //?}
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    //? if >=1.20.2 {
    @Override
     //?}
    public Identifier id() {
        return ID;
    }
    *///?}
}
