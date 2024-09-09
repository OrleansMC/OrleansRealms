package com.orleansmc.realms.managers.common;

import com.orleansmc.realms.managers.realm.RealmsManager;
import com.orleansmc.realms.models.data.RealmModel;
import me.lucko.helper.mongo.MongoProvider;
import me.lucko.helper.mongo.external.bson.Document;
import me.lucko.helper.mongo.external.mongodriver.client.MongoDatabase;
import me.lucko.helper.mongo.external.mongodriver.client.MongoIterable;
import me.lucko.helper.mongo.external.mongodriver.client.model.UpdateOptions;
import me.lucko.helper.redis.RedisProvider;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    public final MongoDatabase minecraftDatabase;
    public final MongoDatabase webDatabase;
    public final Jedis jedis;

    public DatabaseManager(RealmsManager realmsManager) {
        final MongoProvider mongoProvider = Bukkit.getServer().getServicesManager().load(MongoProvider.class);
        if (mongoProvider == null) {
            throw new RuntimeException("MongoProvider service not found");
        }

        this.minecraftDatabase = mongoProvider.getMongo().getDatabase("minecraft");
        this.webDatabase = mongoProvider.getMongo().getDatabase("web");

        final RedisProvider redisProvider = Bukkit.getServer().getServicesManager().load(RedisProvider.class);
        if (redisProvider == null) {
            throw new RuntimeException("RedisProvider service not found");
        }
        jedis = redisProvider.getRedis().getJedis();
    }

    public RealmModel getRealm(String playerName) {
        Document iterable = minecraftDatabase.getCollection("realms")
                .find(new Document("_id", playerName.toLowerCase())).first();

        if (iterable == null) return null;
        return RealmModel.fromDocument(iterable);
    }

    public List<RealmModel> getRealms() {
        List<RealmModel> realms = new ArrayList<>();
        MongoIterable<Document> iterable = minecraftDatabase.getCollection("realms").find();
        for (Document document : iterable) {
            realms.add(RealmModel.fromDocument(document));
        }
        return realms;
    }

    public void saveRealm(RealmModel realm) {
        minecraftDatabase.getCollection("realms").updateOne(
                new Document("_id", realm._id),
                new Document("$set", realm.toDocument()),
                new UpdateOptions().upsert(true)
        );
    }

    public void deleteRealm(String playerName) {
        minecraftDatabase.getCollection("realms").deleteOne(new Document("_id", playerName.toLowerCase()));
    }
}
