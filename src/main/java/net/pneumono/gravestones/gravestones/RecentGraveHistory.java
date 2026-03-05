package net.pneumono.gravestones.gravestones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;

public record RecentGraveHistory(UUID owner, Optional<GlobalPos> first, Optional<GlobalPos> second, Optional<GlobalPos> third) {
    public static final Codec<RecentGraveHistory> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(RecentGraveHistory::owner),
            GlobalPos.CODEC.optionalFieldOf("first").forGetter(RecentGraveHistory::first),
            GlobalPos.CODEC.optionalFieldOf("second").forGetter(RecentGraveHistory::second),
            GlobalPos.CODEC.optionalFieldOf("third").forGetter(RecentGraveHistory::third)
    ).apply(builder, RecentGraveHistory::new));

    public RecentGraveHistory(UUID owner, @Nullable GlobalPos first) {
        this(owner, Optional.ofNullable(first), Optional.empty(), Optional.empty());
    }

    public RecentGraveHistory(UUID owner) {
        this(owner, Optional.empty(), Optional.empty(), Optional.empty());
    }

    public List<GlobalPos> getList() {
        List<GlobalPos> list = new ArrayList<>();
        this.first.ifPresent(list::add);
        this.second.ifPresent(list::add);
        this.third.ifPresent(list::add);
        return list;
    }

    public RecentGraveHistory getShifted(@Nullable GlobalPos newPos) {
        return new RecentGraveHistory(this.owner, Optional.ofNullable(newPos), this.first, this.second);
    }
}
