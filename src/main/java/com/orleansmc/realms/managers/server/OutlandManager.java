package com.orleansmc.realms.managers.server;

import com.orleansmc.bukkit.players.models.PlayerAlertModel;
import com.orleansmc.bukkit.players.models.PlayerModel;
import com.orleansmc.bukkit.players.models.RecentDeathModel;
import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.configs.spawn.Spawn;
import com.orleansmc.realms.managers.common.WebhookManager;
import com.orleansmc.realms.utils.Util;
import dev.lone.itemsadder.api.CustomStack;
import dev.unnm3d.rediseconomy.currency.Currency;
import me.clip.placeholderapi.PlaceholderAPI;
import me.lucko.helper.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class OutlandManager implements Listener {
    OrleansRealms plugin;
    List<String> waitingForRandomTeleport = new ArrayList<>();
    List<String> newTeleported = new ArrayList<>();

    public OutlandManager(OrleansRealms plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupLastDeadLocationCommand();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ServerType serverType = plugin.serversManager.getCurrentServerType();
        Player player = event.getPlayer();
        if (serverType.equals(ServerType.REALMS_SPAWN)) {
            player.teleport(Spawn.LOCATION);
        } else if (serverType.equals(ServerType.REALMS_OUTLAND)) {
            if (!player.hasPermission("orleansmc.outland.access")) {
                player.sendMessage("§cHenüz vahşi dünyaya erişiminiz yok.");
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.kick(Component.text("§cHenüz vahşi dünyaya erişiminiz yok."));
                }, 20 * 5);
                return;
            }
            Location spawn = new Location(Bukkit.getWorld(Settings.WORLD_NAME), 0, 0, 0);
            spawn.setY(254);
            if (!spawn.getBlock().getType().equals(Material.BEDROCK)) {
                spawn.getBlock().setType(Material.BEDROCK);
            }
            spawn.setY(255);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.teleport(spawn);
                PotionEffect effect = PotionEffectType.BLINDNESS.createEffect(20 * 10, 10);
                effect.withIcon(false);
                effect.withParticles(false);
                player.addPotionEffect(effect);
                player.showTitle(
                        Title.title(
                                plugin.getComponent(Util.getExclamation() + "<white>Işınlanıyorsunuz..."),
                                Component.empty(),
                                Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(30000), Duration.ofMillis(200))
                        )
                );
                waitingForRandomTeleport.add(player.getName());
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!waitingForRandomTeleport.contains(player.getName())) {
                        return;
                    }
                    player.sendMessage(Util.getExclamation() + "Sizin için uygun bir yer aranıyor...");
                    Bukkit.getServer().dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "huskhomes:rtp " + player.getName() + " outland"
                    );
                }, 20 * 5);
            }, 1);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!plugin.serversManager.getCurrentServerType().equals(ServerType.REALMS_OUTLAND)) return;
        Player player = event.getPlayer();
        newTeleported.add(player.getName());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location playerHeadLocation = player.getLocation().clone().add(0, 1, 0);
            if (playerHeadLocation.getBlock().getType().isSolid() &&
                    !playerHeadLocation.getBlock().getType().equals(Material.BEDROCK) &&
                    !playerHeadLocation.getBlock().getType().equals(Material.CHEST)
            ) {
                playerHeadLocation.getBlock().setType(Material.AIR);
                playerHeadLocation.clone().add(0, -1, 0).getBlock().setType(Material.AIR);
            }
            Location playerFeetLocation = player.getLocation().clone().add(0, -1, 0);
            if (playerFeetLocation.getBlock().getType().isAir()) {
                playerFeetLocation.getBlock().setType(Material.GRASS_BLOCK);
            }
        }, 2);
        Bukkit.getScheduler().runTaskLater(plugin, () -> newTeleported.remove(player.getName()), 20 * 10);
        if (waitingForRandomTeleport.contains(player.getName())) {
            waitingForRandomTeleport.remove(player.getName());
            player.clearTitle();
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            plugin.playerDataManager.waitPlayerDataThenRun(player, playerModel -> {
                String alertKey = "outland_first_join";
                PlayerAlertModel alert = playerModel.alerts.stream().filter(a -> a.name.equals(alertKey)).findFirst().orElse(null);
                if (alert == null) {
                    playerModel.alerts.add(new PlayerAlertModel(alertKey, true, (int) Double.POSITIVE_INFINITY));
                    plugin.playerDataManager.savePlayerData(playerModel);
                    Bukkit.getServer().dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "allay-show-up " + player.getName() + " WELCOME_TO_OUTLAND"
                    );
                }
            });
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (waitingForRandomTeleport.contains(player.getName())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            if (waitingForRandomTeleport.contains(player.getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTargetToPlayerEvent(EntityTargetEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS_OUTLAND) return;
        if (!(event.getTarget() instanceof Player player)) return;
        if (newTeleported.contains(player.getName())) {
            event.setCancelled(true);
        }

        if (waitingForRandomTeleport.contains(player.getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(ItemSpawnEvent event) {
        ItemStack itemStack = event.getEntity().getItemStack();
        if (itemStack.getType().equals(Material.PAPER)) {
            if (itemStack.getItemMeta().getCustomModelData() == 10275 &&
                    plugin.getString(itemStack.displayName()).contains("Orleans Gem")) {
                ItemStack gem = CustomStack.getInstance("custom_ores:orleans_gem").getItemStack();
                gem.setAmount(itemStack.getAmount());
                event.getLocation().getWorld().dropItem(
                        event.getLocation(),
                        gem
                );
                event.setCancelled(true);
                event.getEntity().remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getEntity();
        if (waitingForRandomTeleport.contains(player.getName())) {
            event.setCancelled(true);
            return;
        }
        plugin.playerDataManager.waitPlayerDataThenRun(player, playerModel -> {
            RecentDeathModel lastDeath = playerModel.recentDeaths.stream()
                    .max(Comparator.comparing(o -> o.date))
                    .orElse(null);

            double multiplier = 0;
            if (lastDeath != null) {
                multiplier = lastDeath.backPriceMultiplier + 1;
                if (multiplier > 5) {
                    multiplier = 0;
                }
            }

            RecentDeathModel recentDeathModel = new RecentDeathModel(
                    Util.getStringFromLocation(player.getLocation().clone().add(0, 0.5, 0)),
                    Settings.SERVER_NAME,
                    new Date(),
                    multiplier
            );
            playerModel.recentDeaths.add(recentDeathModel);
            plugin.playerDataManager.savePlayerData(playerModel);

            String gemIcon = PlaceholderAPI.setPlaceholders(null, "%img_gem%");
            String message = "<bold><color:#AE89EB>" + (int) (multiplier * Settings.BACK_TO_DEATH_LOCATION_PRICE)
                    + "</color></bold><reset>" + gemIcon + " </reset><bold><gradient:#00FFFF:#1E90FF>karşılığında son öldüğünüz yere geri dönebilirsiniz.</gradient></bold> "
                    + "<click:run_command:/last-dead-location><hover:show_text:'<yellow>Geri Dön'>"
                    + "<bold><gradient:#32CD32:#7FFF00>Tıklayın ve geri dönün!</gradient></bold></hover></click>";

            player.sendMessage(
                    plugin.getComponent(message)
            );

            WebhookManager.sendPlayerDeathWebhook(
                    player, event.getDeathMessage(), player.getLocation(), event.getPlayer().getInventory()
            );
        });
    }

    private void setupLastDeadLocationCommand() {
        Commands.create()
                .assertPlayer()
                .handler(context -> {
                    Player player = context.sender();
                    if (waitingForRandomTeleport.contains(player.getName())) {
                        player.sendMessage(Util.getExclamation() + "Şu anda bir yere ışınlanıyorsunuz.");
                        return;
                    }
                    player.sendMessage(Util.getExclamation() + "Ölüm konumunuza dönülüyor... Lütfen bekleyin.");
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        PlayerModel playerModel = plugin.playerDataManager.fetchPlayerData(player.getName());
                        // get latest death location
                        RecentDeathModel recentDeath = playerModel.recentDeaths.stream()
                                .max(Comparator.comparing(o -> o.date))
                                .orElse(null);
                        if (recentDeath == null) {
                            player.sendMessage(Util.getExclamation() + "Ölüm konumunuz bulunamadı.");
                            return;
                        }
                        double multiplier = recentDeath.backPriceMultiplier;
                        double price = multiplier * Settings.BACK_TO_DEATH_LOCATION_PRICE;
                        Currency currency = plugin.getGemCurrency();
                        if (currency.getBalance(player) < price) {
                            player.sendMessage(Util.getExclamation() + "Ölüm konumunuza dönmek için mücevheriniz yetersiz.");
                            return;
                        }
                        if (price > 0) {
                            currency.withdrawPlayer(player, price);
                        }
                        plugin.serversManager.teleportPlayer(player, Util.getLocationFromString(recentDeath.location),
                                Util.getWorldNameFromLocationString(recentDeath.location), recentDeath.server, true);
                    });
                })
                .registerAndBind(plugin, "last-dead-location");
    }
}
