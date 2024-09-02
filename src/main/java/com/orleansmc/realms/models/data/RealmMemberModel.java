package com.orleansmc.realms.models.data;

import com.orleansmc.realms.enums.RealmMember;
import me.lucko.helper.mongo.external.bson.Document;

public class RealmMemberModel {
    public final String name;
    public RealmMember rank;

    public RealmMemberModel(String name, RealmMember rank) {
        this.name = name;
        this.rank = rank;
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("name", name);
        document.put("role", rank.name());
        return document;
    }

    public static RealmMemberModel fromDocument(Document document) {
        return new RealmMemberModel(
                document.getString("name"),
                RealmMember.valueOf(document.getString("role"))
        );
    }
}
