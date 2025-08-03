package net.pneumono.gravestones.networking;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.Gravestones;

public record GravestoneEditorOpenS2CPayload(BlockPos pos) {
    public static final Identifier ID = Gravestones.id("gravestone_editor_open");

    public static PacketByteBuf create(BlockPos pos) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        return buf;
    }
}
