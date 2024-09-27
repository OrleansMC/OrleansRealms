package com.orleansmc.realms.listeners;

import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.configs.spawn.Spawn;
import com.orleansmc.realms.utils.Util;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainListener implements Listener {
    private final OrleansRealms plugin;
    private final List<String> replacedCustomBlockLocations = new CopyOnWriteArrayList<>();

    public MainListener(OrleansRealms plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking()) {
            event.setCancelled(true);
            player.performCommand("menu");
        }
    }

    @EventHandler
    public void onExplosionEvent(ExplosionPrimeEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Mob)) {
            entity.remove();
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
            entity.getWorld().spawnParticle(
                    Particle.EXPLOSION,
                    entity.getLocation(),
                    1
            );
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        String blockLocationString = block.getX() + ":" + block.getY() + ":" + block.getZ();
        if (replacedCustomBlockLocations.contains(blockLocationString)) {
            block.setType(Material.AIR);
            replacedCustomBlockLocations.remove(blockLocationString);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCustomBlockPlace(CustomBlockPlaceEvent event) {
        Location location = event.getBlock().getLocation().clone();
        Block upperBlock = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ());

        int highBlockY = location.getWorld().getHighestBlockAt(location.getBlockX(), location.getBlockZ()).getY();
        if (location.getBlockY() - 1 == highBlockY) {
            return;
        }

        if (upperBlock.getType().isAir()) {
            upperBlock.setType(Material.TRIPWIRE);
            String upperBlockLocationString = upperBlock.getX() + ":" + upperBlock.getY() + ":" + upperBlock.getZ();
            replacedCustomBlockLocations.add(upperBlockLocationString);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                removeReplacedBlock(upperBlock);
            }, 10L);
        } else if (upperBlock.getType().equals(Material.TRIPWIRE)) {
            event.setCancelled(true);
        }
    }

    private void removeReplacedBlock(Block upperBlock) {
        Block newBlock = upperBlock.getWorld().getBlockAt(upperBlock.getX(), upperBlock.getY(), upperBlock.getZ());
        String upperBlockLocationString = upperBlock.getX() + ":" + upperBlock.getY() + ":" + upperBlock.getZ();
        if (replacedCustomBlockLocations.contains(upperBlockLocationString)) {
            if (newBlock.getType().equals(Material.TRIPWIRE)) {
                newBlock.setType(Material.AIR);
            }
            replacedCustomBlockLocations.remove(upperBlockLocationString);
        }
    }

    @EventHandler
    public void onAchievement(PlayerAchievementAwardedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory != null && clickedInventory.getType().equals(InventoryType.MERCHANT)) {
            if (Util.hasCustomTexture(event.getCurrentItem())) {
                event.setCancelled(true);
            }
            if (Util.hasCustomTexture(event.getCursor())) {
                event.setCancelled(true);
            }
            ItemStack firstItem = clickedInventory.getItem(0);
            if (Util.hasCustomTexture(firstItem)) {
                clickedInventory.setItem(0, null);
                event.getWhoClicked().getInventory().addItem(firstItem);
                event.setCancelled(true);
                event.getWhoClicked().sendMessage("§cBu eşya ile takas yapamazsınız.");
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location spawn = Spawn.LOCATION;
        if (spawn == null) {
            return;
        }
        ServerType serverType = plugin.serversManager.getCurrentServerType();
        if (serverType == ServerType.REALMS_SPAWN) {
            Location to = event.getTo();
            if (
                    Math.abs(to.getBlockX() - spawn.getBlockX()) > 280 ||
                            Math.abs(to.getBlockZ() - spawn.getBlockZ()) > 280 ||
                            Math.abs(to.getBlockY() - spawn.getBlockY()) > 50) {
                event.getPlayer().teleport(spawn);
            }
        }
    }
}