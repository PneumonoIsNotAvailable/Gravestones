package net.pneumono.gravestones.networking;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.Gravestones;

public record UpdateGravestoneC2SPayload(BlockPos pos, String line1, String line2, String line3, String line4) {
    public static final Identifier ID = Gravestones.id("update_gravestone");
    public static final Codec<UpdateGravestoneC2SPayload> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(UpdateGravestoneC2SPayload::pos),
            Codec.STRING.fieldOf("line1").forGetter(UpdateGravestoneC2SPayload::line1),
            Codec.STRING.fieldOf("line2").forGetter(UpdateGravestoneC2SPayload::line2),
            Codec.STRING.fieldOf("line3").forGetter(UpdateGravestoneC2SPayload::line3),
            Codec.STRING.fieldOf("line4").forGetter(UpdateGravestoneC2SPayload::line4)
    ).apply(builder, UpdateGravestoneC2SPayload::new));

    public String[] getText() {
        return new String[]{this.line1, this.line2, this.line3, this.line4};
    }

    public PacketByteBuf toBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        NbtElement element = CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElse(new NbtCompound());
        buf.writeNbt((NbtCompound) element);
        return buf;
    }

    public static UpdateGravestoneC2SPayload fromBuf(PacketByteBuf buf) {
        NbtCompound compound = buf.readNbt();
        DataResult<Pair<UpdateGravestoneC2SPayload, NbtElement>> result = CODEC.decode(NbtOps.INSTANCE, compound);
        if (result.result().isPresent()) {
            return result.result().get().getFirst();
        } else {
            return null;
        }
    }
}
