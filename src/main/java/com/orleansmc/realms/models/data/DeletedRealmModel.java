package com.orleansmc.realms.models.data;

import com.orleansmc.realms.enums.RealmClimate;
import me.lucko.helper.mongo.external.bson.Document;

public class DeletedRealmModel {
    public final String owner;
    public final String region;
    public final String server;
    public final RealmClimate climate;
    public final String spawn;

    public DeletedRealmModel(String owner, String region, String server, RealmClimate climate, String spawn) {
        this.owner = owner;
        this.region = region;
        this.server = server;
        this.climate = climate;
        this.spawn = spawn;
    }

    public static DeletedRealmModel fromDocument(Document document) {
        return new DeletedRealmModel(
                document.getString("owner"),
                document.getString("region"),
                document.getString("server"),
                RealmClimate.valueOf(document.getString("climate")),
                document.getString("spawn")
        );
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("owner", owner);
        document.put("region", region);
        document.put("server", server);
        document.put("climate", climate.name());
        document.put("spawn", spawn);
        return document;
    }
}
