package net.pneumono.gravestones.networking;

import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.Gravestones;
import net.minecraft.util.Identifier;

//? if >=1.20.6 {
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
//?} else {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
*///?}

//? if >=1.20.2 {
import net.minecraft.network.packet.CustomPayload;
 //?}

public record UpdateGravestoneC2SPayload(BlockPos pos, String line1, String line2, String line3, String line4) /*? if >=1.20.2 {*/implements CustomPayload/*?}*/ {
    public static final Identifier ID = Gravestones.id("update_gravestone");

    //? if >=1.20.6 {
    public static final CustomPayload.Id<UpdateGravestoneC2SPayload> PAYLOAD_ID = new Id<>(ID);
    public static final PacketCodec<RegistryByteBuf, UpdateGravestoneC2SPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            UpdateGravestoneC2SPayload::pos,
            PacketCodecs.STRING,
            UpdateGravestoneC2SPayload::line1,
            PacketCodecs.STRING,
            UpdateGravestoneC2SPayload::line2,
            PacketCodecs.STRING,
            UpdateGravestoneC2SPayload::line3,
            PacketCodecs.STRING,
            UpdateGravestoneC2SPayload::line4,
            UpdateGravestoneC2SPayload::new
    );
    //?} else {
    /*public static final Codec<UpdateGravestoneC2SPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(UpdateGravestoneC2SPayload::pos),
            Codec.STRING.fieldOf("line1").forGetter(UpdateGravestoneC2SPayload::line1),
            Codec.STRING.fieldOf("line2").forGetter(UpdateGravestoneC2SPayload::line2),
            Codec.STRING.fieldOf("line3").forGetter(UpdateGravestoneC2SPayload::line3),
            Codec.STRING.fieldOf("line4").forGetter(UpdateGravestoneC2SPayload::line4)
    ).apply(instance, UpdateGravestoneC2SPayload::new));
    *///?}

    public String[] getText() {
        return new String[]{this.line1, this.line2, this.line3, this.line4};
    }

    //? if >=1.20.6 {
    @Override
    public Id<? extends CustomPayload> getId() {
        return PAYLOAD_ID;
    }
    //?} else if <1.20.6 {
    /*public static UpdateGravestoneC2SPayload read(PacketByteBuf buf) {
        return new UpdateGravestoneC2SPayload(buf.readBlockPos(), buf.readString(), buf.readString(), buf.readString(), buf.readString());
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
        buf.writeString(this.line1);
        buf.writeString(this.line2);
        buf.writeString(this.line3);
        buf.writeString(this.line4);
    }

    //? if >=1.20.2 {
    @Override
    //?}
    public Identifier id() {
        return ID;
    }
    *///?}
}
