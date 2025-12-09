package net.pneumono.gravestones.networking;

import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.Gravestones;
import net.minecraft.util.Identifier;

//? if >=1.20.5 {
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
//?} else {
/*import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
*///?}

//? if >=1.20.2 {
import net.minecraft.network.packet.CustomPayload;
//?}

public record GravestoneEditorOpenS2CPayload(BlockPos pos) /*? if >=1.20.2 {*/implements CustomPayload/*?}*/ {
    public static final Identifier ID = Gravestones.id("gravestone_editor_open");

    //? if >=1.20.5 {
    public static final CustomPayload.Id<GravestoneEditorOpenS2CPayload> PAYLOAD_ID = new Id<>(ID);
    public static final PacketCodec<RegistryByteBuf, GravestoneEditorOpenS2CPayload> CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, GravestoneEditorOpenS2CPayload::pos, GravestoneEditorOpenS2CPayload::new);
    //?} else {
    /*public static final Codec<GravestoneEditorOpenS2CPayload> CODEC = BlockPos.CODEC.xmap(GravestoneEditorOpenS2CPayload::new, GravestoneEditorOpenS2CPayload::pos);
    *///?}

    //? if >=1.20.5 {
    @Override
    public Id<? extends CustomPayload> getId() {
        return PAYLOAD_ID;
    }
    //?} else if <1.20.5 {
    /*public static GravestoneEditorOpenS2CPayload read(PacketByteBuf buf) {
        return new GravestoneEditorOpenS2CPayload(buf.readBlockPos());
    }

    public PacketByteBuf write() {
        PacketByteBuf buf = PacketByteBufs.create();
        write(buf);
        return buf;
    }

    //? if >=1.20.2 {
    @Override
     //?}
    public void write(PacketByteBuf buf) {
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
