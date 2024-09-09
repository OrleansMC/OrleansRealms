package com.orleansmc.realms.managers.server;

import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.managers.common.WebhookManager;
import com.orleansmc.realms.utils.Util;
import com.orleansmc.realms.models.data.RealmMemberModel;
import com.orleansmc.realms.models.data.RealmModel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedstoneManager implements Listener {
    private final OrleansRealms plugin;
    private final Map<String, Integer> redstoneActivityMap = new HashMap<>();
    private final Map<String, Boolean> notifiedRealms = new HashMap<>();
    private final Map<String, Boolean> notifiedPlayers = new HashMap<>();
    private final List<String> blockedBlockCoordinates = new ArrayList<>();
    private static final int REDSTONE_ACTIVITY_THRESHOLD = 10000; // Eşik değeri
    private static final long RESET_INTERVAL = 20 * 5; // 5 dakika (20 tick = 1 saniye)
    private static final double TPS_LIMIT = 19.5;

    public RedstoneManager(OrleansRealms plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startActivityResetTask();
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) {
            event.setNewCurrent(0);
            return;
        }

        Block block = event.getBlock();
        RealmModel realm = plugin.realmsManager.getRealmByLocation(block.getLocation());
        if (realm == null) {
            event.setNewCurrent(0);
            return;
        }

        if (Bukkit.getServer().getTPS()[0] < TPS_LIMIT) {
            var mostActiveRealm = redstoneActivityMap.entrySet().stream().max(Map.Entry.comparingByValue());
            if (mostActiveRealm.isPresent() && mostActiveRealm.get().getKey().equals(realm.owner)) {
                event.setNewCurrent(0);
                return;
            }
        }

        // Redstone aktivitesini kaydet
        redstoneActivityMap.put(realm.owner, redstoneActivityMap.getOrDefault(realm.owner, 0) + 1);

        // Eğer aktivite eşik değerini aşarsa sinyali kes
        if (redstoneActivityMap.get(realm.owner) > REDSTONE_ACTIVITY_THRESHOLD) {
            if (block.getType().equals(Material.REDSTONE_TORCH)) {
                String locationString = Util.getStringFromLocation(block.getLocation());
                if (!blockedBlockCoordinates.contains(locationString)) {
                    blockedBlockCoordinates.add(locationString);
                }
                event.setNewCurrent(0);
            }

            if (!notifiedRealms.getOrDefault(realm.owner, false)) {
                notifiedRealms.put(realm.owner, true);
                WebhookManager.sendRedstoneExceedLimitWebhook(realm);
                for (RealmMemberModel member : realm.members) {
                    Player player = Bukkit.getPlayer(member.name);
                    if (player != null && !notifiedPlayers.getOrDefault(player.getName(), false) && player.getLocation().distance(block.getLocation()) < 150) {
                        plugin.getLogger().info("Redstone kısıtlaması uygulandı: " + realm.owner);
                        player.sendMessage(plugin.getComponent(
                                """
                                        <gray>---------- <gradient:#FF0000:#FFFFFF>OrleansMC Redstone Kısıtlaması</gradient> ----------</gray>
                                                                                   \s
                                        <red>Redstone aktiviteleriniz 60 saniyede 9600'den fazla olamaz!</red>
                                        <red>Kısıtlandınız! Uyarıları görmemek için sistemlerinizi yavaşlatın.</red>
                                        <gray>-------------------------------------------------</gray>"""
                        ));
                        notifiedPlayers.put(player.getName(), true);
                    }
                }
                notifiedPlayers.put(realm.owner, true);
            }
        }
    }

    public void startActivityResetTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                redstoneActivityMap.clear();
            }
        }.runTaskTimer(plugin, RESET_INTERVAL, RESET_INTERVAL);

        new BukkitRunnable() {
            @Override
            public void run() {
                notifiedRealms.clear();
                notifiedPlayers.clear();
            }
        }.runTaskTimer(plugin, 0, 20 * 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getServer().getTPS()[0] >= TPS_LIMIT) {
                    for (String locationString : blockedBlockCoordinates) {
                        Location location = Util.getLocationFromString(locationString);
                        if (location.isChunkLoaded()) {
                            Block block = location.getBlock();
                            if (block.getType().toString().contains("REDSTONE")) {
                                block.getState().update(true, false);
                            }
                        }
                    }
                }
                blockedBlockCoordinates.clear();
            }
        }.runTaskTimer(plugin, RESET_INTERVAL, RESET_INTERVAL * 2);
    }

    public void clearRedstoneActivity() {
        redstoneActivityMap.clear();
        notifiedRealms.clear();
        notifiedPlayers.clear();
        blockedBlockCoordinates.clear();
    }
}
