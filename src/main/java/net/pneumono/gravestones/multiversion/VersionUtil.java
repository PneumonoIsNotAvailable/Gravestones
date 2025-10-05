package net.pneumono.gravestones.multiversion;

import com.mojang.authlib.GameProfile;
import net.minecraft.component.type.ProfileComponent;

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
}
