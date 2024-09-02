package com.orleansmc.realms.models.temporary;

import com.orleansmc.realms.enums.RealmState;

import java.util.concurrent.CountDownLatch;

public class PendingRealmModel {
    public CountDownLatch latch = new CountDownLatch(1);
    public RealmState state = RealmState.CREATE;

    public void setState(RealmState state) {
        this.state = state;
    }
}
