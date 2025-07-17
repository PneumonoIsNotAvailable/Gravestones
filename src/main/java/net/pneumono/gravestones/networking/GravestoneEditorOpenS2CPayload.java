package net.pneumono.gravestones.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.Gravestones;

public record GravestoneEditorOpenS2CPayload(BlockPos pos) implements CustomPayload {
    public static final CustomPayload.Id<GravestoneEditorOpenS2CPayload> ID = new Id<>(Gravestones.id("gravestone_editor_open"));
    public static final PacketCodec<RegistryByteBuf, GravestoneEditorOpenS2CPayload> CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, GravestoneEditorOpenS2CPayload::pos, GravestoneEditorOpenS2CPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
