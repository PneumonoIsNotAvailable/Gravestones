package net.pneumono.gravestones.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.Gravestones;

public record UpdateGravestoneC2SPayload(BlockPos pos, String line1, String line2, String line3, String line4) implements CustomPayload {
    public static final CustomPayload.Id<UpdateGravestoneC2SPayload> ID = new Id<>(Gravestones.identifier("update_gravestone"));
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

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public String[] getText() {
        return new String[]{this.line1, this.line2, this.line3, this.line4};
    }
}
