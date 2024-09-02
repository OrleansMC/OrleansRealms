package com.orleansmc.realms.models.messaging;

public class InviteModel {
    public final String realm;
    public final String inviter;
    public final String member;
    public final String rank;

    public InviteModel(String realm, String inviter, String member, String rank) {
        this.realm = realm;
        this.inviter = inviter;
        this.member = member;
        this.rank = rank;
    }
}
