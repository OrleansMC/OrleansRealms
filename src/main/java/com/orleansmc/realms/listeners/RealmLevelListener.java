package com.orleansmc.realms.listeners;

import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.models.data.RealmMemberModel;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.quests.objectives.IncreaseRealmLevelObjective;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealmLevelListener implements Listener {
    private final OrleansRealms plugin;
    private final Map<Material, Double> blockEffects = new HashMap<>();
    private final Map<String, Double> newLevels = new HashMap<>();

    public RealmLevelListener(OrleansRealms plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            var entries = newLevels.entrySet().stream().sorted(Map.Entry.comparingByValue());
            for (Map.Entry<String, Double> entry : entries.toList()) {
                RealmModel realm = plugin.realmsManager.getRealm(entry.getKey());
                if (realm == null) continue;
                final double oldLevel = realm.level;
                realm.level = entry.getValue();
                for (RealmMemberModel member : realm.members) {
                    Player player = Bukkit.getPlayer(member.name);
                    if (player != null) {
                        List<RealmModel> realms = plugin.realmsManager.realms.values().stream()
                                .filter(r -> r.members.stream().anyMatch(m -> m.name.equals(player.getName())))
                                .toList();
                        double maxLevel = realms.stream().min((r1, r2) -> Double.compare(r2.level, r1.level)).get().level;
                        if (maxLevel <= realm.level) {
                            if (Math.floor(oldLevel) != Math.floor(realm.level)) {
                                IncreaseRealmLevelObjective.instance.onRealmLevelChange(
                                        player, (int) (Math.floor(oldLevel) - Math.floor(realm.level))
                                );
                            }
                        }
                    }
                }
                plugin.realmsManager.saveRealm(realm);
            }
            newLevels.clear();
        }, 20 * 60, 20 * 60);
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        blockEffects.put(Material.NETHERITE_BLOCK, 0.8);
        blockEffects.put(Material.AMETHYST_BLOCK, 0.01);
        blockEffects.put(Material.ANCIENT_DEBRIS, 0.01);
        blockEffects.put(Material.BEACON, 0.2);
        blockEffects.put(Material.SPONGE, 0.5);
        blockEffects.put(Material.DRAGON_EGG, 0.8);
        blockEffects.put(Material.END_STONE_BRICKS, 0.01);
        blockEffects.put(Material.CONDUIT, 0.2);
        blockEffects.put(Material.SHULKER_BOX, 0.3);
        blockEffects.put(Material.ENCHANTING_TABLE, 0.1);
        blockEffects.put(Material.NETHER_STAR, 0.2);
        blockEffects.put(Material.ENDER_CHEST, 0.1);

        blockEffects.put(Material.OAK_LOG, 0.02);
        blockEffects.put(Material.SPRUCE_LOG, 0.02);
        blockEffects.put(Material.BIRCH_LOG, 0.02);
        blockEffects.put(Material.JUNGLE_LOG, 0.02);
        blockEffects.put(Material.ACACIA_LOG, 0.02);
        blockEffects.put(Material.DARK_OAK_LOG, 0.02);
        blockEffects.put(Material.MANGROVE_LOG, 0.02);
        blockEffects.put(Material.CHERRY_LOG, 0.02);
        blockEffects.put(Material.CRIMSON_STEM, 0.05);
        blockEffects.put(Material.WARPED_STEM, 0.05);
        blockEffects.put(Material.BRICKS, 0.01);
        blockEffects.put(Material.STONE_BRICKS, 0.01);
        blockEffects.put(Material.NETHER_BRICKS, 0.01);
        blockEffects.put(Material.RED_NETHER_BRICKS, 0.01);
        blockEffects.put(Material.END_STONE_BRICKS, 0.01);
        blockEffects.put(Material.POLISHED_ANDESITE, 0.01);
        blockEffects.put(Material.POLISHED_DIORITE, 0.01);
        blockEffects.put(Material.POLISHED_GRANITE, 0.01);
        blockEffects.put(Material.QUARTZ_BLOCK, 0.05);
        blockEffects.put(Material.PRISMARINE, 0.05);
        blockEffects.put(Material.PRISMARINE_BRICKS, 0.05);
        blockEffects.put(Material.DARK_PRISMARINE, 0.05);
        blockEffects.put(Material.PURPUR_BLOCK, 0.05);
        blockEffects.put(Material.PURPUR_PILLAR, 0.05);
        blockEffects.put(Material.PURPUR_STAIRS, 0.05);
        blockEffects.put(Material.PURPUR_SLAB, 0.05);
        blockEffects.put(Material.END_STONE, 0.01);
        blockEffects.put(Material.END_STONE_BRICKS, 0.01);
        blockEffects.put(Material.END_STONE_BRICK_SLAB, 0.01);
        blockEffects.put(Material.END_STONE_BRICK_STAIRS, 0.01);
        blockEffects.put(Material.END_STONE_BRICK_WALL, 0.01);
        blockEffects.put(Material.END_STONE_BRICKS, 0.01);
        blockEffects.put(Material.END_STONE_BRICK_SLAB, 0.01);
        blockEffects.put(Material.END_STONE_BRICK_STAIRS, 0.01);
        blockEffects.put(Material.END_STONE_BRICK_WALL, 0.01);

        // Leaves
        blockEffects.put(Material.OAK_LEAVES, 0.01);
        blockEffects.put(Material.SPRUCE_LEAVES, 0.01);
        blockEffects.put(Material.BIRCH_LEAVES, 0.01);
        blockEffects.put(Material.JUNGLE_LEAVES, 0.01);
        blockEffects.put(Material.ACACIA_LEAVES, 0.01);
        blockEffects.put(Material.DARK_OAK_LEAVES, 0.01);
        blockEffects.put(Material.MANGROVE_LEAVES, 0.01);
        blockEffects.put(Material.CHERRY_LEAVES, 0.01);
        blockEffects.put(Material.CRIMSON_FUNGUS, 0.01);
        blockEffects.put(Material.WARPED_FUNGUS, 0.01);
        blockEffects.put(Material.VINE, 0.01);
        blockEffects.put(Material.TWISTING_VINES, 0.01);
        blockEffects.put(Material.WEEPING_VINES, 0.01);
        blockEffects.put(Material.SUGAR_CANE, 0.005);
        blockEffects.put(Material.CACTUS, 0.005);
        blockEffects.put(Material.BAMBOO, 0.005);
        blockEffects.put(Material.KELP, 0.005);
        blockEffects.put(Material.KELP_PLANT, 0.005);

        // glass
        blockEffects.put(Material.GLASS, 0.01);
        blockEffects.put(Material.WHITE_STAINED_GLASS, 0.01);
        blockEffects.put(Material.ORANGE_STAINED_GLASS, 0.01);
        blockEffects.put(Material.MAGENTA_STAINED_GLASS, 0.01);
        blockEffects.put(Material.LIGHT_BLUE_STAINED_GLASS, 0.01);
        blockEffects.put(Material.YELLOW_STAINED_GLASS, 0.01);
        blockEffects.put(Material.LIME_STAINED_GLASS, 0.01);
        blockEffects.put(Material.PINK_STAINED_GLASS, 0.01);
        blockEffects.put(Material.GRAY_STAINED_GLASS, 0.01);
        blockEffects.put(Material.LIGHT_GRAY_STAINED_GLASS, 0.01);
        blockEffects.put(Material.CYAN_STAINED_GLASS, 0.01);
        blockEffects.put(Material.PURPLE_STAINED_GLASS, 0.01);
        blockEffects.put(Material.BLUE_STAINED_GLASS, 0.01);
        blockEffects.put(Material.BROWN_STAINED_GLASS, 0.01);
        blockEffects.put(Material.GREEN_STAINED_GLASS, 0.01);
        blockEffects.put(Material.RED_STAINED_GLASS, 0.01);
        blockEffects.put(Material.BLACK_STAINED_GLASS, 0.01);
        blockEffects.put(Material.GLASS_PANE, 0.01);

        // wool
        blockEffects.put(Material.WHITE_WOOL, 0.01);
        blockEffects.put(Material.ORANGE_WOOL, 0.01);
        blockEffects.put(Material.MAGENTA_WOOL, 0.01);
        blockEffects.put(Material.LIGHT_BLUE_WOOL, 0.01);
        blockEffects.put(Material.YELLOW_WOOL, 0.01);
        blockEffects.put(Material.LIME_WOOL, 0.01);
        blockEffects.put(Material.PINK_WOOL, 0.01);
        blockEffects.put(Material.GRAY_WOOL, 0.01);
        blockEffects.put(Material.LIGHT_GRAY_WOOL, 0.01);
        blockEffects.put(Material.CYAN_WOOL, 0.01);
        blockEffects.put(Material.PURPLE_WOOL, 0.01);
        blockEffects.put(Material.BLUE_WOOL, 0.01);
        blockEffects.put(Material.BROWN_WOOL, 0.01);
        blockEffects.put(Material.GREEN_WOOL, 0.01);
        blockEffects.put(Material.RED_WOOL, 0.01);
        blockEffects.put(Material.BLACK_WOOL, 0.01);

        // terracotta
        blockEffects.put(Material.WHITE_TERRACOTTA, 0.01);
        blockEffects.put(Material.ORANGE_TERRACOTTA, 0.01);
        blockEffects.put(Material.MAGENTA_TERRACOTTA, 0.01);
        blockEffects.put(Material.LIGHT_BLUE_TERRACOTTA, 0.01);
        blockEffects.put(Material.YELLOW_TERRACOTTA, 0.01);
        blockEffects.put(Material.LIME_TERRACOTTA, 0.01);
        blockEffects.put(Material.PINK_TERRACOTTA, 0.01);
        blockEffects.put(Material.GRAY_TERRACOTTA, 0.01);
        blockEffects.put(Material.LIGHT_GRAY_TERRACOTTA, 0.01);
        blockEffects.put(Material.CYAN_TERRACOTTA, 0.01);
        blockEffects.put(Material.PURPLE_TERRACOTTA, 0.01);
        blockEffects.put(Material.BLUE_TERRACOTTA, 0.01);
        blockEffects.put(Material.BROWN_TERRACOTTA, 0.01);

        // concrete
        blockEffects.put(Material.WHITE_CONCRETE, 0.01);
        blockEffects.put(Material.ORANGE_CONCRETE, 0.01);
        blockEffects.put(Material.MAGENTA_CONCRETE, 0.01);
        blockEffects.put(Material.LIGHT_BLUE_CONCRETE, 0.01);
        blockEffects.put(Material.YELLOW_CONCRETE, 0.01);
        blockEffects.put(Material.LIME_CONCRETE, 0.01);
        blockEffects.put(Material.PINK_CONCRETE, 0.01);
        blockEffects.put(Material.GRAY_CONCRETE, 0.01);
        blockEffects.put(Material.LIGHT_GRAY_CONCRETE, 0.01);
        blockEffects.put(Material.CYAN_CONCRETE, 0.01);
        blockEffects.put(Material.PURPLE_CONCRETE, 0.01);
        blockEffects.put(Material.BLUE_CONCRETE, 0.01);
        blockEffects.put(Material.BROWN_CONCRETE, 0.01);
        blockEffects.put(Material.GREEN_CONCRETE, 0.01);
        blockEffects.put(Material.RED_CONCRETE, 0.01);
        blockEffects.put(Material.BLACK_CONCRETE, 0.01);
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getBlock().getLocation());
        if (realm == null) return;

        Block block = event.getBlock();
        if (block.getY() <= 60) return;

        if (blockEffects.containsKey(block.getType())) {
            double level = newLevels.getOrDefault(realm.owner, realm.level);
            level += blockEffects.get(block.getType());
            newLevels.put(realm.owner, level);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        if (event.isCancelled()) return;
        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getLocation());
        if (realm == null) return;

        double increase;
        Entity entity = event.getEntity();
        if (
                entity.getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG &&
                        entity.getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.CURED
        ) {
            return;
        }

        if (entity instanceof Cow) {
            increase = 1;
        } else if (entity instanceof Sheep) {
            increase = 1;
        } else if (entity instanceof Villager) {
            increase = 0.2;
        } else if (entity instanceof Wolf) {
            increase = 1;
        } else if (entity instanceof Cat) {
            increase = 1;
        } else if (entity instanceof Parrot) {
            increase = 1;
        } else return;
        double level = newLevels.getOrDefault(realm.owner, realm.level);
        level += increase;
        newLevels.put(realm.owner, level);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getBlock().getLocation());
        if (realm == null) return;

        Block block = event.getBlock();
        if (block.getY() <= 60) return;

        if (blockEffects.containsKey(block.getType())) {
            double level = newLevels.getOrDefault(realm.owner, realm.level);
            level -= blockEffects.get(block.getType());
            if (level <= 0) return;
            newLevels.put(realm.owner, level);
        }
    }

    @EventHandler
    public void onCustomBlockPlace(CustomBlockPlaceEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getBlock().getLocation());
        if (realm == null) return;

        Block block = event.getBlock();
        if (block.getY() <= 60) return;

        ItemStack pot = CustomStack.getInstance("customcrops:dry_pot").getItemStack();
        if (pot.isSimilar(event.getCustomBlockItem())) {
            double level = newLevels.getOrDefault(realm.owner, realm.level);
            level += 0.02;
            newLevels.put(realm.owner, level);
        }
    }

    @EventHandler
    public void onCustomBlockBreak(CustomBlockBreakEvent event) {
        if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) return;
        RealmModel realm = plugin.realmsManager.getRealmByLocation(event.getBlock().getLocation());
        if (realm == null) return;

        Block block = event.getBlock();
        if (block.getY() <= 60) return;
        ItemStack pot = CustomStack.getInstance("customcrops:dry_pot").getItemStack();
        ItemStack pot2 = CustomStack.getInstance("customcrops:wet_pot").getItemStack();
        if (pot.isSimilar(event.getCustomBlockItem()) || pot2.isSimilar(event.getCustomBlockItem())) {
            double level = newLevels.getOrDefault(realm.owner, realm.level);
            level -= 0.02;
            if (level <= 0) return;
            newLevels.put(realm.owner, level);
        }
    }
}