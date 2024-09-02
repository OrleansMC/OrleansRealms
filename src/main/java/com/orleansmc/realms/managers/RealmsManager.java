package com.orleansmc.realms.managers;

import com.orleansmc.common.servers.ServerState;
import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.models.messaging.RealmStateModel;
import com.orleansmc.realms.quests.objectives.CreateRealmObjective;
import com.orleansmc.realms.utils.Util;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.configs.texts.Texts;
import com.orleansmc.realms.models.temporary.PendingRealmModel;
import com.orleansmc.realms.models.data.RealmMemberModel;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.models.config.TextModel;
import com.orleansmc.realms.enums.RealmClimate;
import com.orleansmc.realms.enums.RealmState;
import com.orleansmc.realms.enums.RealmTime;
import me.lucko.helper.messaging.Channel;
import me.lucko.helper.messaging.ChannelAgent;
import me.lucko.helper.messaging.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class RealmsManager {
    public final DatabaseManager databaseManager;
    public final MessageManager messageManager;
    public final OrleansRealms plugin;
    public final HashMap<String, RealmModel> realms = new HashMap<>();
    public final ChannelAgent<RealmStateModel> channelAgent;
    private final HashMap<String, PendingRealmModel> pendingRealms = new HashMap<>();

    public RealmsManager(OrleansRealms plugin) {
        this.plugin = plugin;
        this.databaseManager = new DatabaseManager(this);
        this.messageManager = new MessageManager(this);

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

        final Messenger messenger = Bukkit.getServer().getServicesManager().load(Messenger.class);
        if (messenger == null) {
            throw new RuntimeException("Messenger service not found");
        }

        final Channel<RealmStateModel> channel = messenger.getChannel("realms:channel", RealmStateModel.class);
        this.channelAgent = channel.newAgent();

        this.channelAgent.addListener((agent, message) -> {
            if (message.state == RealmState.CREATE) {
                handleCreateRealm(message);
            } else if (message.state == RealmState.CREATED) {
                handleCreatedRealm(message.realm);
            } else if (message.state == RealmState.DELETE) {
                handleDeleteRealm(message.realm);
            } else if (message.state == RealmState.DELETED) {
                handleDeletedRealm(message.realm);
            } else if (message.state == RealmState.UPDATE) {
                handleUpdateRealm(message.realm);
            }
        });

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            String firstPending = pendingRealms.keySet().stream().findFirst().orElse(null);

            if (firstPending != null) {
                int index = 0;
                for (String playerName : pendingRealms.keySet()) {
                    if (Objects.equals(playerName, firstPending)) continue;
                    index++;
                    PendingRealmModel pendingRealmModel = pendingRealms.get(playerName);
                    TextModel textModel = Texts.REALM_QUEUE_REMAINING;
                    textModel.addReplacement("{index}", String.valueOf(index));
                    this.messageManager.sendMessage(
                            playerName, pendingRealmModel.state == RealmState.CREATE ? textModel :
                                    new TextModel(
                                            "<color:#00ff00>Realm update in queue: </color>" + index,
                                            "<color:#00ff00>Diyar güncelleme sırasında: </color>" + index
                                    )
                    );
                }
            }
        }, 0, 20 * 30);

        final AtomicInteger lastProgress = new AtomicInteger(-1);
        plugin.chunkyAPI.onGenerationProgress(event -> {
            if (!plugin.isEnabled()) return;

            int progress = (int) event.progress();
            if (progress == lastProgress.get()) return;
            lastProgress.set(progress);
            if (progress % 10 != 0) return;
            plugin.getLogger().info("Realm creation progress: " + event.progress());
            String playerName = pendingRealms.keySet().stream().findFirst().orElse(null);
            if (playerName == null) {
                plugin.getLogger().warning("CHUNKY API IS RUNNING BUT NO PENDING REALM CREATION");
            }
            PendingRealmModel pendingRealmModel = pendingRealms.get(playerName);

            TextModel textModel = new TextModel(
                    Texts.REALM_CREATION_PROGRESS.en,
                    Texts.REALM_CREATION_PROGRESS.tr
            );
            textModel.addReplacement("{progress}", String.valueOf(progress));
            this.messageManager.sendMessage(
                    playerName, pendingRealmModel.state == RealmState.CREATE ? textModel :
                            new TextModel(
                                    "<color:#00ff00>Realm update progress: </color> %" + progress,
                                    "<color:#00ff00>Diyar güncelleme ilerlemesi: </color> %" + progress
                            )
            );
        });

        plugin.chunkyAPI.onGenerationComplete(event -> Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (!plugin.isEnabled()) return;
            lastProgress.set(-1);
            String playerName = pendingRealms.keySet().stream().findFirst().orElse(null);
            PendingRealmModel pendingRealmModel = pendingRealms.get(playerName);
            CountDownLatch countDownLatch = pendingRealmModel.latch;

            if (playerName == null) {
                plugin.getLogger().warning("CHUNKY API GENERATION COMPLETED BUT NO PENDING REALM CREATION");
                return;
            }

            if (pendingRealmModel.state == RealmState.CREATE) {
                plugin.getLogger().info("Realm creation for " + playerName + " is completed");
            } else if (pendingRealmModel.state == RealmState.UPDATE) {
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    Location location = player.getLocation();
                    RealmModel realm = getRealmByLocation(location);
                    if (realm != null && realm.owner.equals(playerName)) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.teleport(location);
                        });
                    }
                }
                messageManager.sendMessage(
                        playerName, new TextModel(
                                "<color:#00ff00>Realm update is completed",
                                "<color:#00ff00>Diyar güncellemesi tamamlandı"
                        )
                );
                plugin.getLogger().info("Realm update for " + playerName + " is completed");
            }
            countDownLatch.countDown();
        }, 20 * 2));

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            var players = Bukkit.getServer().getOnlinePlayers();
            realms.values().forEach(realm -> {
                if (players.stream().anyMatch(player -> {
                    RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(player.getName())).findFirst().orElse(null);
                    return member != null && member.rank != null;
                })) {
                    int[] regionCoordinates = Util.getRegionCoordinatesFromString(realm.region);
                    int realmLevel = RegionManager.getRealmLevelFromRegion(regionCoordinates[0], regionCoordinates[1]) - realm.start_level;
                    if (realmLevel < 0) {
                        realmLevel = 0;
                    }

                    if (realm.level != realmLevel) {
                        realm.level = realmLevel;
                        saveRealm(realm);
                    }
                }
            });
        }, 0, 20 * 60 * 5);
    }

    public RealmModel getRealm(String playerName) {
        return realms.get(playerName);
    }

    public void createRealm(String playerName, RealmClimate climateType) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PendingRealmModel pendingRealmModel = pendingRealms.get(playerName);
            if (pendingRealmModel != null) {
                plugin.getLogger().info("Realm creation for " + playerName + " is already pending");
                this.messageManager.sendMessage(
                        playerName, Texts.REALM_CREATION_PENDING
                );
                return;
            }
            RealmModel realm = new RealmModel(
                    playerName,
                    getAvailableRealmServer().name,
                    null,
                    climateType,
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

            this.messageManager.sendMessage(
                    playerName, new TextModel(
                            "<color:#00ff00>Realm is being created for you. Please wait...",
                            "<color:#00ff00>Sizin için bir diyar oluşturuluyor. Lütfen bekleyin..."
                    )
            );
            this.channelAgent.getChannel().sendMessage(new RealmStateModel(realm, RealmState.CREATE));
        });
    }

    public void deleteRealm(String playerName) {
        RealmModel realm = realms.get(playerName);
        if (realm == null) {
            plugin.getLogger().info("Player " + playerName + " does not have a realm");
            return;
        }

        this.channelAgent.getChannel().sendMessage(new RealmStateModel(realm, RealmState.DELETE));
    }

    public void changeRealmRadius(String playerName, int radius) {
        RealmModel realm = realms.get(playerName);
        if (realm == null) {
            plugin.getLogger().info("Player " + playerName + " does not have a realm");
            return;
        }

        realm.radius = radius;
        saveRealm(realm);
    }

    public void saveRealm(RealmModel realm) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            RealmModel oldRealm = databaseManager.getRealm(realm.owner);
            databaseManager.saveRealm(realm);
            realms.put(realm.owner, oldRealm);
            this.channelAgent.getChannel().sendMessage(new RealmStateModel(realm, RealmState.UPDATE));
        });
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

    private ServerState getAvailableRealmServer() {
        return plugin.serversManager.getServerStates().values().stream()
                .filter(serverState -> ServerType.valueOf(serverState.type) == ServerType.REALMS)
                .min((serverState1, serverState2) -> Integer.compare(
                        realms.values().stream().filter(realm -> realm.server.equals(serverState1.name)).toArray().length,
                        realms.values().stream().filter(realm -> realm.server.equals(serverState2.name)).toArray().length
                )).orElse(null);
    }

    public RealmModel getRealmByRegionCoordinates(int regionX, int regionZ) {
        for (RealmModel realm : realms.values()) {
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

    private void handleCreateRealm(RealmStateModel realmState) {
        int newRadius;
        if (realmState.state == RealmState.UPDATE) {
            newRadius = realmState.realm.radius;
            realmState.realm.radius = Settings.DEFAULT_REALM_RADIUS;
            realms.put(realmState.realm.owner, realmState.realm);
        } else {
            newRadius = 0;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String worldName = Settings.WORLD_NAME;
            RealmModel realm = realmState.realm;

            if (!pendingRealms.containsKey(realm.owner)) {
                PendingRealmModel pendingRealmModel = new PendingRealmModel();
                pendingRealmModel.state = realmState.state;
                pendingRealms.put(realm.owner, pendingRealmModel);
            }

            if (Objects.equals(realm.server, Settings.SERVER_NAME)) {
                String firstPending = pendingRealms.keySet().stream().findFirst().orElse(null);
                PendingRealmModel pendingRealmModel = pendingRealms.get(firstPending);
                CountDownLatch countDownLatch = pendingRealmModel.latch;

                if (plugin.chunkyAPI.isRunning(worldName) && !firstPending.equals(realm.owner)) {
                    plugin.getLogger().info("Another realm is pending");

                    this.messageManager.sendMessage(
                            realm.owner, pendingRealmModel.state == RealmState.CREATE ? Texts.REALM_CREATION_PENDING : new TextModel(
                                    "<color:#00ff00>Realm update is pending",
                                    "<color:#00ff00>Diyar güncellemesi bekleniyor"
                            )
                    );
                    try {
                        countDownLatch.await();
                        this.handleCreateRealm(realmState);
                        return;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else if (plugin.chunkyAPI.isRunning(worldName)) {
                    plugin.getLogger().warning("CHUNKY API IS RUNNING FOR " + worldName + " BUT NO PENDING REALM CREATION");
                    plugin.chunkyAPI.cancelTask(worldName);
                    plugin.getLogger().warning("CANCELING CHUNKY API TASK FOR " + worldName);
                }

                plugin.getLogger().info("Loading realm for " + realm.owner + " in region " + realm.region + " with climate " + realm.climate);

                int realmsWhichHaveThisClimate = realms.values().stream().filter(
                        realm1 -> realm1.climate.equals(realm.climate)
                                && realm1.server.equals(realm.server)
                ).toArray().length + 1;

                int[] regionCoordinates = realm.region != null ? Util.getRegionCoordinatesFromString(realm.region) :
                        RegionManager.getRegionCoordinatesByClimate(realm.climate, realmsWhichHaveThisClimate);

                Location center = RegionManager.getCenterLocation(regionCoordinates[0], regionCoordinates[1]);
                realm.region = regionCoordinates[0] + "," + regionCoordinates[1];
                plugin.getLogger().info("Chunky API task started for " + realm.owner + " in region " + realm.region + " with climate " + realm.climate);
                plugin.chunkyAPI.startTask(
                        worldName,
                        "squared",
                        center.getBlockX(),
                        center.getBlockZ(),
                        realm.radius + 48,
                        realm.radius + 48,
                        "concentric"
                );

                try {
                    countDownLatch.await();
                    if (newRadius != 0) {
                        plugin.getLogger().info("Updating realm radius for " + realm.owner);
                        realm.radius = newRadius;
                        realms.put(realm.owner, realm);
                    }
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
                    realm.start_level = RegionManager.getRealmLevelFromRegion(regionCoordinates[0], regionCoordinates[1]);
                    databaseManager.saveRealm(realm);
                    this.channelAgent.getChannel().sendMessage(new RealmStateModel(realm, realmState.state == RealmState.UPDATE ? RealmState.UPDATE : RealmState.CREATED));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                plugin.getLogger().info("Waiting realm update for " + realm.owner + " in region " + realm.region + " with climate " + realm.climate + " to " + realm.server);
            }
        });
    }

    private void showUpAllay(RealmModel realm, int tryIndex) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = Bukkit.getPlayer(realm.owner);
            if (tryIndex > 80) {
                plugin.getLogger().warning("Allay is not shown up for " + realm.owner);
                return;
            }
            if (player == null) {
                showUpAllay(realm, tryIndex + 1);
                return;
            }
            Bukkit.getServer().dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "allay-show-up " + player.getName() + " WELCOME_TO_REALM"
            );
        }, 20);
    }

    private void handleCreatedRealm(RealmModel realm) {
        CreateRealmObjective.instance.onCreatedRealm(realm);

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            final PendingRealmModel pendingRealmModel = pendingRealms.get(realm.owner);
            if (pendingRealmModel != null) pendingRealmModel.latch.countDown();

            pendingRealms.remove(realm.owner);
            realms.put(realm.owner, realm);

            Player player = Bukkit.getPlayer(realm.owner);
            if (player != null) {
                Location spawn = Util.getLocationFromString(realm.spawn);
                this.messageManager.sendMessage(
                        player.getName(), Texts.REALM_CREATION_SUCCESS
                );
                plugin.serversManager.teleportPlayer(player, spawn, Settings.REALMS_WORLD_NAME, realm.server);
            }

            if (Settings.SERVER_NAME.equals(realm.server)) {
                showUpAllay(realm, 0);
            }

            plugin.getLogger().info("Realm for " + realm.owner + " is created");
        }, 20 * 2);
    }

    private void handleDeleteRealm(RealmModel realm) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (Objects.equals(realm.server, Settings.SERVER_NAME)) {
                Location center = RegionManager.getCenterLocation(
                        Integer.parseInt(realm.region.split(",")[0]),
                        Integer.parseInt(realm.region.split(",")[1])
                );
                plugin.getLogger().info("Deleting realm for " + realm.owner + " in region " + realm.region + " with climate " + realm.climate);

                RegionManager.unloadAndDeleteRegion(center.getWorld(), Integer.parseInt(realm.region.split(",")[0]), Integer.parseInt(realm.region.split(",")[1]), true);
                databaseManager.deleteRealm(realm.owner);
                channelAgent.getChannel().sendMessage(new RealmStateModel(realm, RealmState.DELETED));
            } else {
                plugin.getLogger().info("Waiting realm deletion for " + realm.owner + " in region " + realm.region + " with climate " + realm.climate + " to " + realm.server);
            }
        });
    }

    private void handleDeletedRealm(RealmModel realm) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            realms.remove(realm.owner);
            List<String> serverRemovedRegions = RegionManager.removedRegions.computeIfAbsent(Settings.SERVER_NAME, k -> new ArrayList<>());
            serverRemovedRegions.add(realm.region);
            RegionManager.removedRegions.put(Settings.SERVER_NAME, serverRemovedRegions);

            Player player = Bukkit.getPlayer(realm.owner);
            if (player != null) {
                this.messageManager.sendMessage(
                        player.getName(), Texts.REALM_DELETED_SUCCESS
                );
            }
            plugin.getLogger().info("Realm for " + realm.owner + " is deleted");
        });
    }

    public void handleUpdateRealm(RealmModel realm) {
        RealmModel oldRealm = realms.get(realm.owner);
        if (oldRealm == null) {
            plugin.getLogger().info("Player " + realm.owner + " does not have a realm");
            return;
        }
        plugin.getLogger().info("Updating realm for " + realm.owner);
        if (Objects.equals(realm.server, Settings.SERVER_NAME)) {
            if (oldRealm.radius != realm.radius) {
                plugin.getLogger().info("Updating realm radius for " + realm.owner);
                messageManager.sendMessage(
                        realm.owner, new TextModel(
                                "<color:#00ff00>Realm size is being updated to " + realm.radius * 2 + "x" + realm.radius * 2 + "...",
                                "<color:#00ff00>Diyar genişliği " + realm.radius * 2 + "x" + realm.radius * 2 + " olarak güncelleniyor..."
                        )
                );
                handleCreateRealm(new RealmStateModel(realm, RealmState.UPDATE));
            }
        }
        realms.put(realm.owner, realm);
        channelAgent.getChannel().sendMessage(new RealmStateModel(realm, RealmState.UPDATED));
    }
}
