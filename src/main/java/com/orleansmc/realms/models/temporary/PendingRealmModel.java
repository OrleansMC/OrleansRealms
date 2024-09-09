package com.orleansmc.realms.models.temporary;

import com.orleansmc.realms.enums.RealmClimate;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class PendingRealmModel {
    public static enum PendingType {
        CREATE,
        RADIUS_CHANGE,
    }
    public final String owner;
    public final RealmClimate climate;
    public final PendingType type;
    public final Date created = new Date();
    public int radius = 0;
    public boolean executed = false;
    public boolean removed = false;
    @Nullable
    public String server;

    public PendingRealmModel(
            String owner,
            RealmClimate climate,
            PendingType type,
            int radius
    ) {
        this.owner = owner;
        this.climate = climate;
        this.type = type;
        this.radius = radius;
    }
}
