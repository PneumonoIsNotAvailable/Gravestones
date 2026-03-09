package net.pneumono.gravestones.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.pneumono.gravestones.Gravestones;

//? if >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
//?} else {
/*import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
*///?}

//? if >=1.20.2
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateGravestoneC2SPayload(BlockPos pos, String line1, String line2, String line3, String line4) /*? if >=1.20.2 {*/implements CustomPacketPayload/*?}*/ {
    public static final Identifier ID = Gravestones.id("update_gravestone");

    //? if >=1.20.5 {
    public static final CustomPacketPayload.Type<UpdateGravestoneC2SPayload> PAYLOAD_ID = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateGravestoneC2SPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateGravestoneC2SPayload::pos,
            ByteBufCodecs.STRING_UTF8,
            UpdateGravestoneC2SPayload::line1,
            ByteBufCodecs.STRING_UTF8,
            UpdateGravestoneC2SPayload::line2,
            ByteBufCodecs.STRING_UTF8,
            UpdateGravestoneC2SPayload::line3,
            ByteBufCodecs.STRING_UTF8,
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

    //? if >=1.20.5 {
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PAYLOAD_ID;
    }
    //?} else if <1.20.5 {
    /*public static UpdateGravestoneC2SPayload read(FriendlyByteBuf buf) {
        return new UpdateGravestoneC2SPayload(buf.readBlockPos(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf());
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
        buf.writeUtf(this.line1);
        buf.writeUtf(this.line2);
        buf.writeUtf(this.line3);
        buf.writeUtf(this.line4);
    }

    //? if >=1.20.2 {
    @Override
    //?}
    public Identifier id() {
        return ID;
    }
    *///?}
}
