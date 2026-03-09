package net.pneumono.gravestones.multiversion;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;

import java.util.UUID;

//? if >=1.20.5
import net.minecraft.world.item.component.ResolvableProfile;

//? if <1.20.5
//import net.minecraft.util.ExtraCodecs;

public class GraveOwner {
    //? if >=1.20.5 {
    public static final Codec<GraveOwner> CODEC = ResolvableProfile.CODEC.xmap(GraveOwner::new, GraveOwner::getProfile);
    private ResolvableProfile profile;

    public GraveOwner(ResolvableProfile profile) {
        this.profile = profile;
    }


    public GraveOwner(GameProfile profile) {
        //? if >=1.21.9 {
        this(ResolvableProfile.createResolved(profile));
        //?} else {
        /*this(new ResolvableProfile(profile));
        *///?}
    }

    public ResolvableProfile getProfile() {
        return profile;
    }

    public void setProfile(ResolvableProfile profile) {
        this.profile = profile;
    }

    public UUID getUuid() {
        return VersionUtil.getId(VersionUtil.getGameProfile(profile));
    }

    public String getName() {
        return VersionUtil.getName(VersionUtil.getGameProfile(profile));
    }

    //?} else {
    /*public static final Codec<GraveOwner> CODEC = ExtraCodecs./^? if >=1.20.2 {^/GAME_PROFILE/^?} else {^//^GAME_PROFILE^//^?}^/.xmap(GraveOwner::new, GraveOwner::getProfile);
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
