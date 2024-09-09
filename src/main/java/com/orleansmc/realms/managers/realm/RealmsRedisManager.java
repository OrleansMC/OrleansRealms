package com.orleansmc.realms.managers.realm;

import com.orleansmc.common.servers.ServerState;
import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.configs.texts.Texts;
import com.orleansmc.realms.enums.RealmClimate;
import com.orleansmc.realms.enums.RealmState;
import com.orleansmc.realms.enums.RealmTime;
import com.orleansmc.realms.managers.common.WebhookManager;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.models.messaging.RealmStateModel;
import com.orleansmc.realms.models.temporary.PendingRealmModel;
import com.orleansmc.realms.quests.objectives.CreateRealmObjective;
import com.orleansmc.realms.utils.Util;
import me.lucko.helper.messaging.Channel;
import me.lucko.helper.messaging.ChannelAgent;
import me.lucko.helper.messaging.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RealmsRedisManager {
    RealmsManager realmsManager;
    OrleansRealms plugin;
    public RealmModel pendingRealm;
    public final RealmsChunkyManager realmsChunkyManager;
    public final AtomicBoolean creationLock = new AtomicBoolean(false);
    public final ChannelAgent<RealmStateModel> realmStateChannelAgent;
    public final ChannelAgent<PendingRealmModel> pendingRealmsChannelAgent;
    public final ConcurrentHashMap<String, PendingRealmModel> pendingRealms = new ConcurrentHashMap<>();

    public RealmsRedisManager(RealmsManager realmsManager) {
        this.realmsManager = realmsManager;
        this.plugin = realmsManager.plugin;

        final Messenger messenger = plugin.getService(Messenger.class);

        final Channel<RealmStateModel> channel = messenger.getChannel("realms:channel", RealmStateModel.class);
        this.realmStateChannelAgent = channel.newAgent();

        final Channel<PendingRealmModel> pendingChannel = messenger.getChannel("realms:pending", PendingRealmModel.class);
        this.pendingRealmsChannelAgent = pendingChannel.newAgent();

        this.realmsChunkyManager = new RealmsChunkyManager(this);

        this.pendingRealmsChannelAgent.addListener((agent, message) -> {
            if (message.removed) {
                pendingRealms.remove(message.owner);
                return;
            }
            pendingRealms.put(message.owner, message);
        });

        this.realmStateChannelAgent.addListener((agent, message) -> {
            if (message.state == RealmState.CREATED) {
                handleCreatedRealm(message);
            } else if (message.state == RealmState.DELETE) {
                handleDeleteRealm(message);
            } else if (message.state == RealmState.DELETED) {
                handleDeletedRealm(message);
            } else if (message.state == RealmState.UPDATE) {
                handleUpdateRealm(message);
            }
        });

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int queueIndex = 0;
            for (PendingRealmModel pending : pendingRealms.values()) {
                if (pending.removed) {
                    continue;
                }
                if (pending.executed) {
                    continue;
                }
                queueIndex++;
                Player player = Bukkit.getPlayer(pending.owner);
                if (player == null) {
                    continue;
                }
                player.sendMessage(
                        plugin.getComponent(
                                Util.getExclamation() + "<color:#ff0000> Diyar oluşumu sırasında " + queueIndex + " kişi önünüzde."
                        )
                );
            }
        }, 0, 20L * 5);

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (creationLock.get()) {
                return;
            }
            if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) {
                return;
            }
            ServerState suitableServer = realmsManager.getSuitableRealmsServer();
            if (suitableServer == null) {
                return;
            }
            if (!suitableServer.name.equals(Settings.SERVER_NAME)) {
                return;
            }
            PendingRealmModel pending = pendingRealms.values().stream()
                    .filter(pendingRealmModel -> !pendingRealmModel.executed)
                    .findFirst().orElse(null);
            if (pending == null) {
                return;
            }
            creationLock.set(true);
            this.generateRealm(suitableServer.name, pending);
            pending.executed = true;
            pending.server = suitableServer.name;
            this.pendingRealmsChannelAgent.getChannel().sendMessage(pending);
        }, 0, 20L * 2);
    }

    public void changeRealmRadius(String playerName, int radius) {
        RealmModel realm = realmsManager.getRealm(playerName);
        if (realm == null) {
            return;
        }
        PendingRealmModel pending = pendingRealms.get(playerName);
        if (pending != null) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                player.sendMessage(
                        plugin.getComponent(
                                Util.getExclamation() + "<color:#ff0000> Zaten diyarınız güncelleniyor. Lütfen bekleyin."
                        )
                );
            }
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.pendingRealmsChannelAgent.getChannel().sendMessage(
                    new PendingRealmModel(playerName, realm.climate, PendingRealmModel.PendingType.RADIUS_CHANGE, radius)
            );
        });
    }

    public void createRealm(Player player, RealmClimate climate) {
        PendingRealmModel pending = pendingRealms.get(player.getName());
        if (pending != null) {
            player.sendMessage(
                    plugin.getComponent(
                            Util.getExclamation() + "<color:#ff0000> Zaten diyarınız oluşturuluyor. Lütfen bekleyin."
                    )
            );
            return;
        }
        player.sendMessage("§aDiyarınız oluşturuluyor, lütfen bekleyin...");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.pendingRealmsChannelAgent.getChannel().sendMessage(
                    new PendingRealmModel(player.getName(), climate, PendingRealmModel.PendingType.CREATE, Settings.DEFAULT_REALM_RADIUS)
            );
        });
    }

    public void handleCreatedRealm(RealmStateModel message) {
        RealmModel realm = message.realm;
        CreateRealmObjective.instance.onCreatedRealm(realm);

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            realmsManager.realms.put(realm.owner, realm);
            Player player = Bukkit.getPlayer(realm.owner);
            if (player != null) {
                Location spawn = Util.getLocationFromString(realm.spawn);
                realmsManager.messageManager.sendMessage(
                        player.getName(), Texts.REALM_CREATION_SUCCESS
                );
                plugin.serversManager.teleportPlayer(player, spawn, Settings.REALMS_WORLD_NAME, realm.server);
            }

            if (Settings.SERVER_NAME.equals(realm.server)) {
                realmsManager.showUpAllay(realm, 0);
                WebhookManager.sendRealmCreateWebhook(realm);
            }

            plugin.getLogger().info("Realm for " + realm.owner + " is created");
        }, 20 * 10);
    }

    public void handleDeleteRealm(RealmStateModel message) {
        RealmModel realm = message.realm;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (Objects.equals(realm.server, Settings.SERVER_NAME)) {
                Location center = RegionManager.getCenterLocation(
                        Integer.parseInt(realm.region.split(",")[0]),
                        Integer.parseInt(realm.region.split(",")[1])
                );
                plugin.getLogger().info("Deleting realm for " + realm.owner + " in region " + realm.region + " with climate " + realm.climate);

                RegionManager.unloadAndDeleteRegion(center.getWorld(), Integer.parseInt(realm.region.split(",")[0]), Integer.parseInt(realm.region.split(",")[1]), true);
                realmsManager.databaseManager.deleteRealm(realm.owner);
                WebhookManager.sendRealmDeleteWebhook(realm);
                realmStateChannelAgent.getChannel().sendMessage(
                        new RealmStateModel(realm, RealmState.DELETED)
                );
            } else {
                plugin.getLogger().info("Waiting realm deletion for " + realm.owner + " in region " + realm.region + " with climate " + realm.climate + " to " + realm.server);
            }
        });
    }

    public void handleDeletedRealm(RealmStateModel message) {
        RealmModel realm = message.realm;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            realmsManager.realms.remove(realm.owner);
            List<String> serverRemovedRegions = RegionManager.removedRegions.computeIfAbsent(Settings.SERVER_NAME, k -> new ArrayList<>());
            serverRemovedRegions.add(realm.region);
            RegionManager.removedRegions.put(Settings.SERVER_NAME, serverRemovedRegions);

            Player player = Bukkit.getPlayer(realm.owner);
            if (player != null) {
                realmsManager.messageManager.sendMessage(
                        player.getName(), Texts.REALM_DELETED_SUCCESS
                );
            }
            plugin.getLogger().info("Realm for " + realm.owner + " is deleted");
        });
    }

    public void handleUpdateRealm(RealmStateModel message) {
        RealmModel realm = message.realm;
        RealmModel oldRealm = realmsManager.realms.get(realm.owner);
        if (oldRealm == null) {
            plugin.getLogger().info("Player " + realm.owner + " does not have a realm");
            return;
        }
        if (oldRealm.radius != realm.radius) {
            Player player = Bukkit.getPlayer(realm.owner);
            if (player != null) {
                player.teleport(player.getLocation());
            }
        }
        plugin.getLogger().info("Updating realm for " + realm.owner);
        realmsManager.realms.put(realm.owner, realm);
    }

    public void generateRealm(String serverName, PendingRealmModel pending) {
        RealmModel oldRealm = realmsManager.getRealm(pending.owner);
        String worldName = Settings.WORLD_NAME;

        RealmModel realm = oldRealm != null ? oldRealm : new RealmModel(
                pending.owner,
                serverName,
                null,
                pending.climate,
                null,
                new Date(),
                new Date(),
                Settings.DEFAULT_REALM_RADIUS,
                new ArrayList<>(),
                new ArrayList<>(),
                true,
                0,
                Settings.DEFAULT_REALM_RADIUS,
                RealmTime.CYCLE,
                true,
                0,
                0
        );

        int realmsWhichHaveThisClimate = realmsManager.realms.values().stream().filter(
                realm1 -> realm1.climate.equals(realm.climate)
                        && realm1.server.equals(realm.server)
        ).toArray().length + 1;

        int[] regionCoordinates = realm.region != null ? Util.getRegionCoordinatesFromString(realm.region) :
                RegionManager.getRegionCoordinatesByClimate(realm.climate, realmsWhichHaveThisClimate);

        Location center = RegionManager.getCenterLocation(regionCoordinates[0], regionCoordinates[1]);
        realm.region = regionCoordinates[0] + "," + regionCoordinates[1];
        if (plugin.chunkyAPI.isRunning(worldName)) {
            plugin.getLogger().warning("CHUNKY API IS RUNNING FOR " + worldName + " BUT NO PENDING REALM CREATION");
            plugin.chunkyAPI.cancelTask(worldName);
            plugin.getLogger().warning("CANCELING CHUNKY API TASK FOR " + worldName);
        }
        plugin.getLogger().info("Chunky API task started for " + realm.owner + " in region " + realm.region + " with climate " + realm.climate);
        plugin.chunkyAPI.startTask(
                worldName,
                "squared",
                center.getBlockX(),
                center.getBlockZ(),
                pending.radius + 48,
                pending.radius + 48,
                "concentric"
        );
        pendingRealm = realm;
    }

    public void handleGeneratedRealm(String owner) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (pendingRealm == null) {
                return;
            }

            if (!pendingRealm.owner.equals(owner)) {
                plugin.getLogger().warning("PENDING REALM OWNER DOES NOT MATCH GENERATED REALM OWNER");
                pendingRealm = null;
                return;
            }

            PendingRealmModel pending = pendingRealms.get(owner);
            if (pending == null) {
                plugin.getLogger().warning("PENDING REALM NOT FOUND FOR " + owner);
                pendingRealm = null;
                return;
            }

            RealmModel realm = pendingRealm;
            realm.radius = pending.radius;
            int[] regionCoordinates = Util.getRegionCoordinatesFromString(realm.region);
            Location center = RegionManager.getCenterLocation(regionCoordinates[0], regionCoordinates[1]);
            Location spawn = center.clone();
            spawn.setY(
                    Objects.requireNonNull(center.getWorld()).getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ())
            );
            if (spawn.getY() < 55) {
                plugin.getLogger().warning("Realm spawn is below 55 for " + realm.owner);
                spawn.setY(55);
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                spawn.getBlock().setType(Material.BEDROCK);
            });
            Location finalSpawn = spawn.getBlock().getLocation().toCenterLocation();
            finalSpawn.setY(finalSpawn.getY() + 1.5);
            realm.spawn = Util.getStringFromLocation(finalSpawn);
            realm.start_level = 0;
            realmsManager.databaseManager.saveRealm(realm);
            pendingRealm = null;
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                this.realmStateChannelAgent.getChannel().sendMessage(
                        new RealmStateModel(
                                realm,
                                pending.type == PendingRealmModel.PendingType.RADIUS_CHANGE ?
                                        RealmState.UPDATE : RealmState.CREATED
                        ));
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                    pending.removed = true;
                    this.pendingRealmsChannelAgent.getChannel().sendMessage(pending);

                    Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                        creationLock.set(false);
                    }, 20 * 5);
                }, 20 * 5);
            }, 20 * 5);
        });
    }
}
