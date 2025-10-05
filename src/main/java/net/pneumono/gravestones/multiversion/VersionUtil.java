package net.pneumono.gravestones.multiversion;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;

import java.util.Optional;
import java.util.UUID;

public class VersionUtil {
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

    public static GameProfile getGameProfile(ProfileComponent profile) {
        //? if >=1.21.9 {
        return profile.getGameProfile();
        //?} else {
        /*return profile.gameProfile();
        *///?}
    }

    public static <T> void put(NbtCompound nbt, String key, Codec<T> codec, T object) {
        put(NbtOps.INSTANCE, nbt, key, codec, object);
    }

    public static <T> void put(DynamicOps<NbtElement> ops, NbtCompound nbt, String key, Codec<T> codec, T object) {
        //? if >=1.21.5 {
        nbt.put(key, codec, object);
        //?} else {
        /*nbt.put(key, codec.encodeStart(ops, object).getOrThrow());
        *///?}
    }

    public static <T> Optional<T> get(NbtCompound nbt, String key, Codec<T> codec) {
        return get(NbtOps.INSTANCE, nbt, key, codec);
    }

    public static <T> Optional<T> get(DynamicOps<NbtElement> ops, NbtCompound nbt, String key, Codec<T> codec) {
        //? if >=1.21.5 {
        return nbt.get(key, codec);
        //?} else {
        /*return codec.decode(ops, nbt.get(key)).result().map(Pair::getFirst);
        *///?}
    }

    public static NbtCompound getCompoundOrEmpty(NbtCompound nbt, String key) {
        //? if >=1.21.5 {
        return nbt.getCompoundOrEmpty(key);
         //?} else {
        /*return nbt.getCompound(key);
        *///?}
    }

    public static NbtList getCompoundListOrEmpty(NbtCompound nbt, String key) {
        //? if >=1.21.5 {
        return nbt.getListOrEmpty(key);
         //?} else {
        /*return nbt.getList(key, NbtElement.COMPOUND_TYPE);
        *///?}
    }
}
