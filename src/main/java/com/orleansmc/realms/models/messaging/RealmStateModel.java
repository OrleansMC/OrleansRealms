package com.orleansmc.realms.models.messaging;

import com.orleansmc.realms.models.data.RealmModel;

public class RealmStateModel {
    public final RealmModel realm;
    public final com.orleansmc.realms.enums.RealmState state;

    public RealmStateModel(RealmModel realm, com.orleansmc.realms.enums.RealmState state) {
        this.realm = realm;
        this.state = state;
    }
}
