package net.pneumono.gravestones.multiversion;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

//? if <1.21.5 {
/*import com.mojang.datafixers.util.Pair;
*///?}

//? if >1.20.6 {
import net.minecraft.component.type.ProfileComponent;
//?}

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

    //? if >=1.20.6 {
    public static GameProfile getGameProfile(ProfileComponent profile) {
        //? if >=1.21.9 {
        return profile.getGameProfile();
        //?} else {
        /*return profile.gameProfile();
        *///?}
    }
    //?}

    public static GlobalPos createGlobalPos(RegistryKey<World> dimension, BlockPos pos) {
        //? if >=1.20.6 {
        return new GlobalPos(dimension, pos);
        //?} else {
        /*return GlobalPos.create(dimension, pos);
        *///?}
    }

    public static BlockPos getPos(GlobalPos pos) {
        //? if >=1.20.6 {
        return pos.pos();
        //?} else {
        /*return pos.getPos();
        *///?}
    }

    public static RegistryKey<World> getDimension(GlobalPos pos) {
        //? if >=1.20.6 {
        return pos.dimension();
        //?} else {
        /*return pos.getDimension();
        *///?}
    }

    public static <T> void put(NbtCompound nbt, String key, Codec<T> codec, T object) {
        put(NbtOps.INSTANCE, nbt, key, codec, object);
    }

    @SuppressWarnings("unused")
    public static <T> void put(DynamicOps<NbtElement> ops, NbtCompound nbt, String key, Codec<T> codec, T object) {
        //? if >=1.21.5 {
        nbt.put(key, codec, object);
        //?} else {
        /*nbt.put(key, codec.encodeStart(ops, object).result().orElseThrow());
        *///?}
    }

    public static <T> Optional<T> get(NbtCompound nbt, String key, Codec<T> codec) {
        return get(NbtOps.INSTANCE, nbt, key, codec);
    }

    @SuppressWarnings("unused")
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
