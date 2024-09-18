package com.orleansmc.realms.managers.server;

import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.managers.common.LuckPermsManager;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.utils.Util;
import eu.decentsoftware.holograms.api.DHAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.Quests;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.storage.implementation.sql.BukkitQuesterSqlStorage;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LeaderboardManager {
    OrleansRealms plugin;
    public static final BukkitQuestsPlugin questsPlugin = (BukkitQuestsPlugin) Objects.requireNonNull(
            Bukkit.getServer().getPluginManager().getPlugin("Quests")
    );

    public LeaderboardManager(OrleansRealms plugin) {
        this.plugin = plugin;

        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                updateRealmLeaderboard();
                updateMoneyLeaderboard();
                updateQuestLeaderboard();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, 20 * 5, 20 * 60);
    }

    public void updateRealmLeaderboard() throws InterruptedException {
        List<RealmModel> top3Realm = plugin.realmsManager.realms.values().stream().sorted(
                (r1, r2) -> Double.compare(r2.level, r1.level)
        ).toList().subList(0, 3);
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc.getName().startsWith("realm_")) {
                int index = Integer.parseInt(npc.getName().split("_")[1]
                        .replace("th", "")) - 1;
                RealmModel realm = top3Realm.get(index);

                if (npc.isSpawned() && npc.getEntity() instanceof SkinnableEntity) {
                    SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
                    // Yeni skin ismiyle skin değiştir
                    skinTrait.setSkinName(realm.owner);
                }

                String hologramKey = npc.getName() + "_hologram";
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(realm.owner);

                DHAPI.removeHologram(hologramKey);
                DHAPI.removeHologram(hologramKey + "_level");

                Thread.sleep(100);

                DHAPI.createHologram(hologramKey, npc.getEntity().getLocation().clone().add(0, 2.62, 0), false, List.of(
                        getPrefixColor(offlinePlayer) + offlinePlayer.getName()

                ));
                DHAPI.createHologram(hologramKey + "_level", npc.getEntity().getLocation().clone().add(0, 2.30, 0), false, List.of(
                        Util.getStringFromMiniMessageString(
                                "<color:#50c9eb><bold>Diyar Seviyesi " + (int) realm.level
                        )
                ));
            }
        }
    }


    public void updateMoneyLeaderboard() throws InterruptedException {
        plugin.redisEconomyAPI.getDefaultCurrency().getOrderedAccounts(3).thenAccept(accounts -> {
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (npc.getName().startsWith("money_")) {
                    int index = Integer.parseInt(npc.getName().split("_")[1]
                            .replace("th", "")) - 1;
                    String playerName = plugin.redisEconomyAPI.getUsernameFromUUIDCache(UUID.fromString(accounts.get(index).getValue()));
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

                    if (npc.isSpawned() && npc.getEntity() instanceof SkinnableEntity) {
                        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
                        // Yeni skin ismiyle skin değiştir
                        skinTrait.setSkinName(offlinePlayer.getName());
                    }

                    String hologramKey = npc.getName() + "_hologram";

                    DHAPI.removeHologram(hologramKey);
                    DHAPI.removeHologram(hologramKey + "_level");

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    DHAPI.createHologram(hologramKey, npc.getEntity().getLocation().clone().add(0, 2.62, 0), false, List.of(
                            getPrefixColor(offlinePlayer) + offlinePlayer.getName()

                    ));
                    DHAPI.createHologram(hologramKey + "_level", npc.getEntity().getLocation().clone().add(0, 2.30, 0), false, List.of(
                            Util.getStringFromMiniMessageString(
                                    "<color:#9d80ff><bold>" + Util.prettyNumber((long) accounts.get(index).getScore()) + "<reset>" + plugin.redisEconomyAPI.getDefaultCurrency().getCurrencySingular()
                            )
                    ));
                }
            }
        });
    }

    public void updateQuestLeaderboard() throws SQLException {
        ConcurrentHashMap<String, Integer> questerAmountsCompleted = getQuesterAmountsCompleted();
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc.getName().startsWith("quest_")) {
                int index = Integer.parseInt(npc.getName().split("_")[1]
                        .replace("th", "")) - 1;
                String playerName = questerAmountsCompleted.keySet().stream().skip(index).findFirst().orElse(null);
                int completedQuests = questerAmountsCompleted.get(playerName) / 10;

                if (npc.isSpawned() && npc.getEntity() instanceof SkinnableEntity) {
                    SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
                    // Yeni skin ismiyle skin değiştir
                    skinTrait.setSkinName(playerName);
                }

                String hologramKey = npc.getName() + "_hologram";

                DHAPI.removeHologram(hologramKey);
                DHAPI.removeHologram(hologramKey + "_level");

                DHAPI.createHologram(hologramKey, npc.getStoredLocation().clone().add(0, 2.62, 0), false, List.of(
                        getPrefixColor(Bukkit.getOfflinePlayer(playerName)) + playerName
                ));
                DHAPI.createHologram(hologramKey + "_level", npc.getStoredLocation().clone().add(0, 2.30, 0), false, List.of(
                        Util.getStringFromMiniMessageString(
                                "<color:#ffbb80><bold>" + completedQuests + " Görev"
                        )
                ));
            }
        }
    }

    public String getPrefixColor(OfflinePlayer player) {
        String vaultPrefix = PlaceholderAPI.setPlaceholders(player, "%vault_prefix%");
        String hexColor = Util.getFirstHexColor(vaultPrefix);
        if (LuckPermsManager.hasPermission(player, "orleansmc.realms.rgb_name") && !player.isOp()) {
            if (hexColor == null) {
                return "#f0f01c";
            }
            return vaultPrefix.replace(hexColor, "#f0f01c");
        }
        if (hexColor == null) {
            return "#f0f01c";
        }
        return hexColor;
    }

    public ConcurrentHashMap<String, Integer> getQuesterAmountsCompleted() throws SQLException {
        BukkitQuesterSqlStorage storage = (BukkitQuesterSqlStorage) questsPlugin.getStorage().getImplementation();
        final ConcurrentHashMap<String, Integer> amountsCompleted = new ConcurrentHashMap<>();
        try (final Connection c = storage.getConnectionFactory().getConnection()) {
            try (final PreparedStatement ps = c.prepareStatement(storage.getStatementProcessor().apply(
                    "SELECT uuid, lastknownname, questpoints " +
                            "FROM '{prefix}players' " +
                            "ORDER BY questpoints DESC " +
                            "LIMIT 3;"
            ))) {
                try (final ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        amountsCompleted.put(rs.getString("lastknownname"), rs.getInt("questpoints"));
                    }
                }
            }
        }
        return amountsCompleted;
    }
}
