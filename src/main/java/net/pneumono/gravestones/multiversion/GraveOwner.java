package net.pneumono.gravestones.multiversion;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import net.minecraft.component.type.ProfileComponent;

import java.util.UUID;

public class GraveOwner {
    public static final Codec<GraveOwner> CODEC = ProfileComponent.CODEC.xmap(GraveOwner::new, GraveOwner::getProfileComponent);
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

    public ProfileComponent getProfileComponent() {
        return profile;
    }

    public void setProfileComponent(ProfileComponent profile) {
        this.profile = profile;
    }

    public UUID getUuid() {
        return VersionUtil.getId(VersionUtil.getGameProfile(profile));
    }

    public String getName() {
        return VersionUtil.getName(VersionUtil.getGameProfile(profile));
    }

    public String getNotNullName() {
        String name = getName();
        return name == null ? "???" : name;
    }
}
