package net.pneumono.gravestones.multiversion;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;

import java.util.UUID;

//? if >=1.20.6 {
import net.minecraft.component.type.ProfileComponent;
//?} else {
/*import net.minecraft.util.dynamic.Codecs;
*///?}

public class GraveOwner {
    //? if >=1.20.6 {
    public static final Codec<GraveOwner> CODEC = ProfileComponent.CODEC.xmap(GraveOwner::new, GraveOwner::getProfile);
    private ProfileComponent profile;

    public GraveOwner(ProfileComponent profile) {
        this.profile = profile;
    }


    public GraveOwner(GameProfile profile) {
        //? if >=1.21.9 {
        this(ProfileComponent.ofStatic(profile));
        //?} else {
        /*this(new ProfileComponent(profile));
        *///?}
    }

    public ProfileComponent getProfile() {
        return profile;
    }

    public void setProfile(ProfileComponent profile) {
        this.profile = profile;
    }

    public UUID getUuid() {
        return VersionUtil.getId(VersionUtil.getGameProfile(profile));
    }

    public String getName() {
        return VersionUtil.getName(VersionUtil.getGameProfile(profile));
    }

    //?} else {
    /*public static final Codec<GraveOwner> CODEC = Codecs./^? if >=1.20.2 {^/GAME_PROFILE_WITH_PROPERTIES/^?} else {^//^GAME_PROFILE^//^?}^/.xmap(GraveOwner::new, GraveOwner::getProfile);
    private GameProfile profile;

    public GraveOwner(GameProfile profile) {
        this.profile = profile;
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public void setProfile(GameProfile profile) {
        this.profile = profile;
    }

    public UUID getUuid() {
        return VersionUtil.getId(this.profile);
    }

    public String getName() {
        return VersionUtil.getName(this.profile);
    }
    *///?}

    public String getNotNullName() {
        String name = getName();
        return name == null ? "???" : name;
    }
}
