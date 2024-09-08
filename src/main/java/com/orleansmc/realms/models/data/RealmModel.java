package com.orleansmc.realms.models.data;

import com.orleansmc.realms.enums.RealmClimate;
import com.orleansmc.realms.enums.RealmMember;
import com.orleansmc.realms.enums.RealmTime;
import me.lucko.helper.mongo.external.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RealmModel {
    public final String _id;
    public final String owner;
    public final String server;
    public String region;
    public int radius;
    public String spawn;
    public final RealmClimate climate;
    public Date created;
    public Date last_active;
    public List<RealmMemberModel> members;
    public List<String> banned_players;
    public boolean allow_visitors;
    public double level;
    public int unlocked_radius;
    public RealmTime time;
    public boolean monster_spawn;
    public int allowed_member_count;
    public int start_level;

    public RealmModel(
            String owner,
            String server,
            String region,
            RealmClimate climate,
            String spawn,
            Date created,
            Date lastActive,
            int radius,
            ArrayList<RealmMemberModel> members,
            ArrayList<String> bannedPlayers,
            boolean allowVisitors,
            double level,
            int unlockedRadius,
            RealmTime time,
            boolean monsterSpawn,
            int allowedMemberCount,
            int startLevel
    ) {
        this._id = owner.toLowerCase();
        this.owner = owner;
        this.server = server;
        this.radius = radius;
        this.region = region;
        this.spawn = spawn;
        this.climate = climate;
        this.created = created;
        this.last_active = lastActive;
        this.members = members;
        this.banned_players = bannedPlayers;
        this.allow_visitors = allowVisitors;
        this.level = level;
        this.unlocked_radius = unlockedRadius;
        this.time = time;
        this.monster_spawn = monsterSpawn;
        this.allowed_member_count = allowedMemberCount;
        this.start_level = startLevel;

        if (this.members.stream().noneMatch(member -> member.name.equalsIgnoreCase(owner))) {
            this.members.add(new RealmMemberModel(owner, RealmMember.MANAGER));
        }
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("owner", owner);
        document.put("server", server);
        document.put("region", region);
        document.put("climate", climate.name());
        document.put("spawn", spawn);
        document.put("created", created);
        document.put("last_active", last_active);
        document.put("radius", radius);
        document.put("members", members.stream().map(RealmMemberModel::toDocument).toList());
        document.put("banned_players", banned_players);
        document.put("allow_visitors", allow_visitors);
        document.put("level", level);
        document.put("unlocked_radius", unlocked_radius);
        document.put("time", time.name());
        document.put("monster_spawn", monster_spawn);
        document.put("allowed_member_count", allowed_member_count);
        document.put("start_level", start_level);
        return document;
    }

    @SuppressWarnings("unchecked")
    public static RealmModel fromDocument(Document document) {
        return new RealmModel(
                document.getString("owner"),
                document.getString("server"),
                document.getString("region"),
                RealmClimate.valueOf(document.getString("climate")),
                document.getString("spawn"),
                document.getDate("created"),
                document.getDate("last_active"),
                document.getInteger("radius"),
                new ArrayList<RealmMemberModel>(document.get("members", List.class).stream().map(o -> RealmMemberModel.fromDocument((Document) o)).toList()),
                new ArrayList<String>(document.get("banned_players", List.class)),
                document.getBoolean("allow_visitors"),
                document.getDouble("level"),
                document.getInteger("unlocked_radius"),
                RealmTime.valueOf(document.getString("time")),
                document.getBoolean("monster_spawn", true),
                document.getInteger("allowed_member_count", 3),
                document.getInteger("start_level", 1)
        );
    }
}
