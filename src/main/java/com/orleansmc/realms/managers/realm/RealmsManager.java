package com.orleansmc.realms.managers.realm;

import com.orleansmc.common.servers.ServerState;
import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.enums.RealmClimate;
import com.orleansmc.realms.enums.RealmState;
import com.orleansmc.realms.managers.common.DatabaseManager;
import com.orleansmc.realms.managers.common.MessageManager;
import com.orleansmc.realms.models.data.RealmMemberModel;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.models.messaging.RealmStateModel;
import com.orleansmc.realms.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.*;

public class RealmsManager {
    public final DatabaseManager databaseManager;
    public final MessageManager messageManager;
    public final RealmsRedisManager realmsRedisManager;
    public final OrleansRealms plugin;
    public final HashMap<String, RealmModel> realms = new HashMap<>();

    public RealmsManager(OrleansRealms plugin) {
        this.plugin = plugin;
        this.databaseManager = new DatabaseManager(this);
        this.messageManager = new MessageManager(this);
        this.realmsRedisManager = new RealmsRedisManager(this);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> serverRemovedRegions = RegionManager.removedRegions.computeIfAbsent(Settings.SERVER_NAME, k -> new ArrayList<>());
            for (RealmModel realm : databaseManager.getRealms()) {
                if (realm.server.equals(Settings.SERVER_NAME)) {
                    serverRemovedRegions.add(realm.region);
                }
                realms.put(realm.owner, realm);
            }
            RegionManager.removedRegions.put(Settings.SERVER_NAME, serverRemovedRegions);
        });
    }

    public RealmModel getRealm(String owner) {
        return realms.get(owner);
    }

    public void createRealm(Player player, RealmClimate climate) {
        RealmModel currentRealm = getRealm(player.getName());
        if (currentRealm != null) {
            throw new RuntimeException("Player already has a realm");
        }
        realmsRedisManager.createRealm(player, climate);
    }

    public ServerState getSuitableRealmsServer() {
        return plugin.serversManager.getServerStates().values().stream()
                .filter(serverState -> ServerType.valueOf(serverState.type) == ServerType.REALMS).min((server1, server2) -> {
                    int server1RealmsCount = (int) (realms.values().stream().filter(realm -> realm.server.equals(server1.name)).count()
                            + realmsRedisManager.pendingRealms.values().stream().filter(realm -> {
                        if (realm.server == null) return false;
                        return realm.server.equals(server1.name);
                    }).count());
                    int server2RealmsCount = (int) (realms.values().stream().filter(realm -> realm.server.equals(server2.name)).count()
                            + realmsRedisManager.pendingRealms.values().stream().filter(realm -> {
                        if (realm.server == null) return false;
                        return realm.server.equals(server2.name);
                    }).count());
                    return Integer.compare(server1RealmsCount, server2RealmsCount);
                })
                .orElse(null);
    }

    public void showUpAllay(RealmModel realm, int tryIndex) {
        Player player = Bukkit.getPlayer(realm.owner);
        if (tryIndex > 80) {
            plugin.getLogger().warning("Allay is not shown up for " + realm.owner);
            return;
        }
        if (player == null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                showUpAllay(realm, tryIndex + 1);
            }, 20 * 3);
            return;
        }
        Bukkit.getServer().dispatchCommand(
                Bukkit.getConsoleSender(),
                "allay-show-up " + player.getName() + " WELCOME_TO_REALM"
        );
    }

    public RealmModel getRealmByRegionCoordinates(int regionX, int regionZ) {
        for (RealmModel realm : realms.values()) {
            if (!realm.server.equals(Settings.SERVER_NAME)) {
                continue;
            }
            String[] region = realm.region.split(",");
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (Objects.equals(region[0], String.valueOf(regionX + x)) && Objects.equals(region[1], String.valueOf(regionZ + z))) {
                        return realm;
                    }
                }
            }
        }
        return null;
    }

    public RealmModel getRealmByLocation(Location location) {
        int[] region = RegionManager.getRegionFromLocation(location.getBlockX(), location.getBlockZ());
        return getRealmByRegionCoordinates(region[0], region[1]);
    }


    public void saveRealm(RealmModel realm) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            RealmModel oldRealm = databaseManager.getRealm(realm.owner);
            databaseManager.saveRealm(realm);
            realms.put(realm.owner, oldRealm);
            realmsRedisManager.realmStateChannelAgent.getChannel().sendMessage(
                    new RealmStateModel(realm, RealmState.UPDATE)
            );
        });
    }

    public void teleportPlayerToRealm(Player player, RealmModel realm) {
        plugin.serversManager.teleportPlayer(player, Util.getLocationFromString(realm.spawn), Settings.REALMS_WORLD_NAME, realm.server);
    }

    public void deleteRealm(String playerName) {
        RealmModel realm = realms.get(playerName);
        if (realm == null) {
            plugin.getLogger().info("Player " + playerName + " does not have a realm");
            return;
        }
        realmsRedisManager.realmStateChannelAgent.getChannel().sendMessage(
                new RealmStateModel(realm, RealmState.DELETE)
        );
    }

    public void removeMobs(RealmModel realm) {
        Location realmSpawn = Util.getLocationFromString(realm.spawn);
        for (Chunk chunk : realmSpawn.getWorld().getLoadedChunks()) {
            RealmModel chunkRealm = getRealmByLocation(chunk.getBlock(0, 0, 0).getLocation());
            if (chunkRealm != null && chunkRealm.owner.equals(realm.owner)) {
                for (Entity entity : chunk.getEntities()) {
                    if (!(entity instanceof Mob mob)) continue;
                    mob.remove();
                }
            }
        }
    }

    public void kickVisitors(RealmModel realm) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("orleansmc.realms.staff")) continue;

                if (player.getWorld().getName().equals(Settings.WORLD_NAME)) {
                    if (Objects.equals(realm.owner, player.getName())) continue;
                    if (realm.members.stream().noneMatch(member -> member.name.equals(player.getName()))) {
                        RealmModel playerCurrentRealm = getRealmByLocation(player.getLocation());
                        if (playerCurrentRealm != null && playerCurrentRealm.owner.equals(realm.owner)) {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                player.sendMessage("§cDiyar sahibi tarafından ziyaretçilerin atılması sağlandı.");
                                player.performCommand("spawn");
                            });
                        }
                    }
                }
            }
        });
    }

    public RealmMemberModel getRealmMember(String realmOwnerName, String memberName) {
        RealmModel realm = realms.get(realmOwnerName);
        if (realm == null) {
            return null;
        }

        return realm.members.stream().filter(member -> member.name.equals(memberName)).findFirst().orElse(null);
    }
}