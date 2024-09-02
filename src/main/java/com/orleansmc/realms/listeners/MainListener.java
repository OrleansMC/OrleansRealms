package com.orleansmc.realms.listeners;

import com.orleansmc.realms.OrleansRealms;
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
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MainListener implements Listener {
    private final OrleansRealms plugin;
    private final List<String> replacedCustomBlockLocations = new ArrayList<>();

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
        if (replacedCustomBlockLocations.contains(Util.getStringFromLocation(block.getLocation()))) {
            block.setType(Material.AIR);
            replacedCustomBlockLocations.remove(
                    Util.getStringFromLocation(block.getLocation())
            );
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCustomBlockPlace(CustomBlockPlaceEvent event) {
        Location location = event.getBlock().getLocation().clone();
        Block upperBlock = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ());

        if (upperBlock.getType().isAir()) {
            upperBlock.setType(Material.TRIPWIRE);
            replacedCustomBlockLocations.add(
                    Util.getStringFromLocation(upperBlock.getLocation())
            );
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                replacedCustomBlockLocations.remove(
                        Util.getStringFromLocation(upperBlock.getLocation())
                );
                upperBlock.setType(Material.AIR);
            }, 4L);
        }
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
}
