package com.orleansmc.realms.configs.settings;

import com.orleansmc.bukkit.players.models.PlayerModel;
import com.orleansmc.realms.managers.LuckPermsManager;
import com.orleansmc.realms.models.data.RealmModel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Settings {
    public static String SERVER_NAME = "default";
    public static int SERVER_STATE_INTERVAL = 1;
    public static int SERVER_STATE_TIMEOUT = 3;
    public static int REALM_MEMBER_COUNT_INCREASE_PRICE = 250;
    public static int DEFAULT_REALM_MEMBER_COUNT = 3;
    public static int DEFAULT_REALM_RADIUS = 75;
    public static String WORLD_NAME = "world";
    public static String REALMS_WORLD_NAME = "realms";
    public static int REALM_RADIUS_INCREASE_150_PRICE = 2500;
    public static int REALM_RADIUS_INCREASE_250_PRICE = 3000;
    public static int REALM_RADIUS_INCREASE_500_PRICE = 15000;
    public static int BACK_TO_DEATH_LOCATION_PRICE = 100;

    public static void load(YamlConfiguration configFile) {
        SERVER_NAME = configFile.getString("server-name", SERVER_NAME);
        SERVER_STATE_INTERVAL = configFile.getInt("server-state-interval", SERVER_STATE_INTERVAL);
        SERVER_STATE_TIMEOUT = configFile.getInt("server-state-timeout", SERVER_STATE_TIMEOUT);
        REALM_MEMBER_COUNT_INCREASE_PRICE = configFile.getInt("realm-member-count.increase-price", REALM_MEMBER_COUNT_INCREASE_PRICE);
        DEFAULT_REALM_MEMBER_COUNT = configFile.getInt("realm-member-count.default", DEFAULT_REALM_MEMBER_COUNT);
        DEFAULT_REALM_RADIUS = configFile.getInt("realm-radius.default", DEFAULT_REALM_RADIUS);
        WORLD_NAME = configFile.getString("world-name", WORLD_NAME);
        REALMS_WORLD_NAME = configFile.getString("realms-world-name", REALMS_WORLD_NAME);
        REALM_RADIUS_INCREASE_150_PRICE = configFile.getInt("realm-radius.increase-150-price", REALM_RADIUS_INCREASE_150_PRICE);
        REALM_RADIUS_INCREASE_250_PRICE = configFile.getInt("realm-radius.increase-250-price", REALM_RADIUS_INCREASE_250_PRICE);
        REALM_RADIUS_INCREASE_500_PRICE = configFile.getInt("realm-radius.increase-500-price", REALM_RADIUS_INCREASE_500_PRICE);
        BACK_TO_DEATH_LOCATION_PRICE = configFile.getInt("back-to-death-location-price", BACK_TO_DEATH_LOCATION_PRICE);
    }

    public static int getAllowedMemberCount(RealmModel realm) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(realm.owner);
        int allowed = LuckPermsManager.getBenefitLevel(owner, "increase_member_count", 0);
        if (allowed == 0 && realm.allowed_member_count == 0) {
            return DEFAULT_REALM_MEMBER_COUNT;
        }
        if (realm.allowed_member_count == 0) {
            return allowed;
        }
        if (allowed == 0) {
            return realm.allowed_member_count;
        }
        return allowed + realm.allowed_member_count - DEFAULT_REALM_MEMBER_COUNT;
    }

    public static int getUnlockedRealmRadius(RealmModel realm) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(realm.owner);
        int allowed = LuckPermsManager.getBenefitLevel(owner, "unlocked_realm_radius", 0);
        return Math.max(realm.unlocked_radius, allowed);
    }
}