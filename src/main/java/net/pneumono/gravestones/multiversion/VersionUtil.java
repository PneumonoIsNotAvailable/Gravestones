package net.pneumono.gravestones.multiversion;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import java.util.Optional;
import java.util.UUID;

//? if >=1.20.5
import net.minecraft.world.item.component.ResolvableProfile;

public class VersionUtil {
    public static Identifier createId(String namespace, String path) {
        //? if >=1.21 {
        return Identifier.fromNamespaceAndPath(namespace, path);
         //?} else {
        /*return Identifier.tryBuild(namespace, path);
        *///?}
    }

    public static UUID getId(GameProfile profile) {
        //? if >=1.21.9 {
        return profile.id();
        //?} else {
        /*return profile.getId();
        *///?}
    }

    public static String getName(GameProfile profile) {
        //? if >=1.21.9 {
        return profile.name();
        //?} else {
        /*return profile.getName();
         *///?}
    }

    //? if >=1.20.5 {
    public static GameProfile getGameProfile(ResolvableProfile profile) {
        //? if >=1.21.9 {
        return profile.partialProfile();
        //?} else {
        /*return profile.gameProfile();
        *///?}
    }
    //?}

    public static GlobalPos createGlobalPos(ResourceKey<Level> dimension, BlockPos pos) {
        //? if >=1.20.5 {
        return new GlobalPos(dimension, pos);
        //?} else {
        /*return GlobalPos.of(dimension, pos);
        *///?}
    }

    @Deprecated(forRemoval = true)
    public static BlockPos getPos(GlobalPos pos) {
        //? if >=1.20.5 {
        return pos.pos();
        //?} else {
        /*return pos.pos();
        *///?}
    }

    @Deprecated(forRemoval = true)
    public static ResourceKey<Level> getDimension(GlobalPos pos) {
        //? if >=1.20.5 {
        return pos.dimension();
        //?} else {
        /*return pos.dimension();
        *///?}
    }

    public static Identifier getId(ResourceKey<?> key) {
        //? if >=1.21.11 {
        return key.identifier();
        //?} else {
        /*return key.location();
        *///?}
    }

    public static <T> void put(CompoundTag tag, String key, Codec<T> codec, T object) {
        put(NbtOps.INSTANCE, tag, key, codec, object);
    }

    @SuppressWarnings("unused")
    public static <T> void put(DynamicOps<Tag> ops, CompoundTag tag, String key, Codec<T> codec, T object) {
        //? if >=1.21.5 {
        tag.store(key, codec, ops, object);
        //?} else {
        /*tag.put(key, codec.encodeStart(ops, object).result().orElseThrow());
        *///?}
    }

    public static <T> Optional<T> get(CompoundTag tag, String key, Codec<T> codec) {
        return get(NbtOps.INSTANCE, tag, key, codec);
    }

    @SuppressWarnings("unused")
    public static <T> Optional<T> get(DynamicOps<Tag> ops, CompoundTag tag, String key, Codec<T> codec) {
        //? if >=1.21.5 {
        return tag.read(key, codec, ops);
        //?} else {
        /*return codec.decode(ops, tag.get(key)).result().map(Pair::getFirst);
        *///?}
    }

    public static CompoundTag getCompoundOrEmpty(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        return tag.getCompoundOrEmpty(key);
        //?} else {
        /*return tag.getCompound(key);
        *///?}
    }

    public static ListTag getCompoundListOrEmpty(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        return tag.getListOrEmpty(key);
        //?} else {
        /*return tag.getList(key, Tag.TAG_COMPOUND);
        *///?}
    }
}
