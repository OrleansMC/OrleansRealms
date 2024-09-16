package com.orleansmc.realms.listeners;

import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.models.data.RealmMemberModel;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.quests.objectives.CreateRealmObjective;
import com.orleansmc.realms.quests.objectives.IncreaseRealmLevelObjective;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                final int oldLevel = (int) realm.level;
                realm.level = entry.getValue();
                for (RealmMemberModel member : realm.members) {
                    Player player = Bukkit.getPlayer(member.name);
                    if (player != null) {
                        List<RealmModel> realms = plugin.realmsManager.realms.values().stream()
                                .filter(r -> r.members.stream().anyMatch(m -> m.name.equals(player.getName())))
                                .toList();
                        int maxLevel = (int) realms.stream().min((r1, r2) -> Double.compare(r2.level, r1.level)).get().level;
                        if (maxLevel < realm.level) {
                            IncreaseRealmLevelObjective.instance.onRealmLevelChange(
                                    player, ((int) realm.level)
                            );
                        }
                    }
                }
                plugin.realmsManager.saveRealm(realm);
            }
            newLevels.clear();
        }, 20 * 60, 20 * 60);
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        blockEffects.put(Material.EMERALD_BLOCK, 0.5);
        blockEffects.put(Material.DIAMOND_BLOCK, 0.4);
        blockEffects.put(Material.NETHERITE_BLOCK, 0.8);
        blockEffects.put(Material.AMETHYST_BLOCK, 0.1);
        blockEffects.put(Material.ANCIENT_DEBRIS, 0.6);
        blockEffects.put(Material.BEACON, 0.6);
        blockEffects.put(Material.SPONGE, 0.5);
        blockEffects.put(Material.DRAGON_EGG, 0.8);
        blockEffects.put(Material.END_STONE_BRICKS, 0.3);
        blockEffects.put(Material.CONDUIT, 0.6);
        blockEffects.put(Material.SHULKER_BOX, 0.3);
        blockEffects.put(Material.ENCHANTING_TABLE, 0.7);
        blockEffects.put(Material.GOLD_BLOCK, 0.3);
        blockEffects.put(Material.NETHER_STAR, 0.7);
        blockEffects.put(Material.TOTEM_OF_UNDYING, 0.6);
        blockEffects.put(Material.ENDER_CHEST, 0.3);
        blockEffects.put(Material.GILDED_BLACKSTONE, 0.4);
        blockEffects.put(Material.LODESTONE, 0.5);
        blockEffects.put(Material.HEART_OF_THE_SEA, 0.5);
        blockEffects.put(Material.ELYTRA, 0.7);
        blockEffects.put(Material.NETHERITE_INGOT, 0.6);
        blockEffects.put(Material.NETHERITE_SCRAP, 0.5);
        blockEffects.put(Material.MUSIC_DISC_PIGSTEP, 0.4);
        blockEffects.put(Material.TRIDENT, 0.5);
        blockEffects.put(Material.BELL, 0.08);
        blockEffects.put(Material.BRAIN_CORAL_BLOCK, 0.3);
        blockEffects.put(Material.TURTLE_EGG, 0.4);
        blockEffects.put(Material.ENCHANTED_GOLDEN_APPLE, 0.7);
        blockEffects.put(Material.WITHER_SKELETON_SKULL, 0.5);
        blockEffects.put(Material.OBSIDIAN, 0.3);
        blockEffects.put(Material.CRYING_OBSIDIAN, 0.4);
        blockEffects.put(Material.SHULKER_SHELL, 0.3);
        blockEffects.put(Material.PURPUR_BLOCK, 0.6);
        blockEffects.put(Material.GHAST_TEAR, 0.5);
        blockEffects.put(Material.BLACKSTONE, 0.08);
        blockEffects.put(Material.IRON_BLOCK, 0.3);
        blockEffects.put(Material.REDSTONE_BLOCK, 0.08);
        blockEffects.put(Material.QUARTZ_BLOCK, 0.3);
        blockEffects.put(Material.LAPIS_BLOCK, 0.3);
        blockEffects.put(Material.NETHER_BRICKS, 0.4);
        blockEffects.put(Material.PRISMARINE, 0.2);
        blockEffects.put(Material.PRISMARINE_BRICKS, 0.3);
        blockEffects.put(Material.DARK_PRISMARINE, 0.3);
        blockEffects.put(Material.SEA_LANTERN, 0.3);
        blockEffects.put(Material.POLISHED_BLACKSTONE, 0.08);
        blockEffects.put(Material.CHISELED_QUARTZ_BLOCK, 0.3);
        blockEffects.put(Material.POLISHED_DIORITE, 0.08);
        blockEffects.put(Material.POLISHED_GRANITE, 0.08);
        blockEffects.put(Material.POLISHED_ANDESITE, 0.08);
        blockEffects.put(Material.SMOOTH_QUARTZ, 0.3);
        blockEffects.put(Material.SMOOTH_RED_SANDSTONE, 0.08);
        blockEffects.put(Material.SMOOTH_SANDSTONE, 0.08);
        blockEffects.put(Material.SMOOTH_STONE, 0.08);
        blockEffects.put(Material.BASALT, 0.08);
        blockEffects.put(Material.POLISHED_BASALT, 0.08);
        blockEffects.put(Material.GLOWSTONE, 0.1);
        blockEffects.put(Material.END_STONE, 0.3);
        blockEffects.put(Material.BONE_BLOCK, 0.1);
        blockEffects.put(Material.HONEY_BLOCK, 0.1);
        blockEffects.put(Material.HONEYCOMB_BLOCK, 0.1);
        blockEffects.put(Material.MAGMA_BLOCK, 0.2);
        blockEffects.put(Material.RED_NETHER_BRICKS, 0.3);
        blockEffects.put(Material.CHISELED_RED_SANDSTONE, 0.08);
        blockEffects.put(Material.CHISELED_SANDSTONE, 0.08);
        blockEffects.put(Material.CHISELED_STONE_BRICKS, 0.08);
        blockEffects.put(Material.CRACKED_STONE_BRICKS, 0.08);
        blockEffects.put(Material.MOSSY_STONE_BRICKS, 0.08);
        blockEffects.put(Material.MOSSY_COBBLESTONE, 0.08);
        blockEffects.put(Material.CLAY, 0.08);
        blockEffects.put(Material.BRICKS, 0.08);
        blockEffects.put(Material.TERRACOTTA, 0.08);
        blockEffects.put(Material.WHITE_TERRACOTTA, 0.08);
        blockEffects.put(Material.ORANGE_TERRACOTTA, 0.08);
        blockEffects.put(Material.MAGENTA_TERRACOTTA, 0.08);
        blockEffects.put(Material.LIGHT_BLUE_TERRACOTTA, 0.08);
        blockEffects.put(Material.YELLOW_TERRACOTTA, 0.08);
        blockEffects.put(Material.LIME_TERRACOTTA, 0.08);
        blockEffects.put(Material.PINK_TERRACOTTA, 0.08);
        blockEffects.put(Material.GRAY_TERRACOTTA, 0.08);
        blockEffects.put(Material.LIGHT_GRAY_TERRACOTTA, 0.08);
        blockEffects.put(Material.CYAN_TERRACOTTA, 0.08);
        blockEffects.put(Material.PURPLE_TERRACOTTA, 0.08);
        blockEffects.put(Material.BLUE_TERRACOTTA, 0.08);
        blockEffects.put(Material.BROWN_TERRACOTTA, 0.08);
        blockEffects.put(Material.GREEN_TERRACOTTA, 0.08);
        blockEffects.put(Material.RED_TERRACOTTA, 0.08);
        blockEffects.put(Material.BLACK_TERRACOTTA, 0.08);
        blockEffects.put(Material.WHITE_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.ORANGE_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.MAGENTA_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.LIGHT_BLUE_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.YELLOW_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.LIME_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.PINK_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.GRAY_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.LIGHT_GRAY_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.CYAN_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.PURPLE_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.BLUE_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.BROWN_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.GREEN_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.RED_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.BLACK_GLAZED_TERRACOTTA, 0.08);
        blockEffects.put(Material.WHITE_BANNER, 0.08);
        blockEffects.put(Material.BLACK_STAINED_GLASS, 0.02);
        blockEffects.put(Material.BLUE_STAINED_GLASS, 0.02);
        blockEffects.put(Material.BROWN_STAINED_GLASS, 0.02);
        blockEffects.put(Material.CYAN_STAINED_GLASS, 0.02);
        blockEffects.put(Material.GRAY_STAINED_GLASS, 0.02);
        blockEffects.put(Material.GREEN_STAINED_GLASS, 0.02);
        blockEffects.put(Material.LIGHT_BLUE_STAINED_GLASS, 0.02);
        blockEffects.put(Material.LIGHT_GRAY_STAINED_GLASS, 0.02);
        blockEffects.put(Material.LIME_STAINED_GLASS, 0.02);
        blockEffects.put(Material.MAGENTA_STAINED_GLASS, 0.02);
        blockEffects.put(Material.ORANGE_STAINED_GLASS, 0.02);
        blockEffects.put(Material.PINK_STAINED_GLASS, 0.02);
        blockEffects.put(Material.PURPLE_STAINED_GLASS, 0.02);
        blockEffects.put(Material.RED_STAINED_GLASS, 0.02);
        blockEffects.put(Material.WHITE_STAINED_GLASS, 0.02);
        blockEffects.put(Material.YELLOW_STAINED_GLASS, 0.02);
        blockEffects.put(Material.NOTE_BLOCK, 0.15);
        blockEffects.put(Material.JUKEBOX, 0.15);
        blockEffects.put(Material.BLAST_FURNACE, 0.08);
        blockEffects.put(Material.SMOKER, 0.08);
        blockEffects.put(Material.BREWING_STAND, 0.08);
        blockEffects.put(Material.CARTOGRAPHY_TABLE, 0.08);
        blockEffects.put(Material.COMPOSTER, 0.08);
        blockEffects.put(Material.GRINDSTONE, 0.08);
        blockEffects.put(Material.LOOM, 0.08);
        blockEffects.put(Material.SMITHING_TABLE, 0.08);
        blockEffects.put(Material.STONECUTTER, 0.08);
        blockEffects.put(Material.FLETCHING_TABLE, 0.08);
        blockEffects.put(Material.BARREL, 0.08);
        blockEffects.put(Material.BEEHIVE, 0.08);
        //logs
        blockEffects.put(Material.OAK_LOG, 0.01);
        blockEffects.put(Material.SPRUCE_LOG, 0.01);
        blockEffects.put(Material.CHERRY_LOG, 0.01);
        blockEffects.put(Material.MANGROVE_LOG, 0.01);
        blockEffects.put(Material.BIRCH_LOG, 0.01);
        blockEffects.put(Material.JUNGLE_LOG, 0.01);
        blockEffects.put(Material.ACACIA_LOG, 0.01);
        blockEffects.put(Material.DARK_OAK_LOG, 0.01);
        blockEffects.put(Material.CRIMSON_STEM, 0.01);
        blockEffects.put(Material.WARPED_STEM, 0.01);
        //planks
        blockEffects.put(Material.OAK_PLANKS, 0.005);
        blockEffects.put(Material.SPRUCE_PLANKS, 0.005);
        blockEffects.put(Material.BIRCH_PLANKS, 0.005);
        blockEffects.put(Material.JUNGLE_PLANKS, 0.005);
        blockEffects.put(Material.ACACIA_PLANKS, 0.005);
        blockEffects.put(Material.DARK_OAK_PLANKS, 0.005);
        blockEffects.put(Material.CRIMSON_PLANKS, 0.005);
        blockEffects.put(Material.WARPED_PLANKS, 0.005);
        blockEffects.put(Material.CHERRY_PLANKS, 0.005);
        blockEffects.put(Material.MANGROVE_PLANKS, 0.005);
        //blocks
        blockEffects.put(Material.COBBLESTONE, 0.005);
        blockEffects.put(Material.STONE, 0.005);
        blockEffects.put(Material.SANDSTONE, 0.005);
        blockEffects.put(Material.RED_SANDSTONE, 0.005);
        blockEffects.put(Material.NETHERRACK, 0.005);
        blockEffects.put(Material.END_STONE, 0.005);
        blockEffects.put(Material.PURPUR_BLOCK, 0.005);
        blockEffects.put(Material.QUARTZ_BLOCK, 0.005);
        blockEffects.put(Material.SMOOTH_STONE, 0.005);
        blockEffects.put(Material.SMOOTH_SANDSTONE, 0.005);
        blockEffects.put(Material.SMOOTH_RED_SANDSTONE, 0.005);
        blockEffects.put(Material.SMOOTH_QUARTZ, 0.005);
        blockEffects.put(Material.SMOOTH_STONE_SLAB, 0.005);
        blockEffects.put(Material.SMOOTH_SANDSTONE_SLAB, 0.005);
        blockEffects.put(Material.SMOOTH_RED_SANDSTONE_SLAB, 0.005);
        blockEffects.put(Material.SMOOTH_QUARTZ_SLAB, 0.005);
        blockEffects.put(Material.STONE_SLAB, 0.005);
        blockEffects.put(Material.SANDSTONE_SLAB, 0.005);
        blockEffects.put(Material.RED_SANDSTONE_SLAB, 0.005);
        blockEffects.put(Material.QUARTZ_SLAB, 0.005);
        blockEffects.put(Material.STONE_BRICKS, 0.005);
        blockEffects.put(Material.MOSSY_STONE_BRICKS, 0.005);
        blockEffects.put(Material.CRACKED_STONE_BRICKS, 0.005);
        blockEffects.put(Material.CHISELED_STONE_BRICKS, 0.005);
        blockEffects.put(Material.BRICKS, 0.005);
        blockEffects.put(Material.NETHER_BRICKS, 0.005);
        blockEffects.put(Material.RED_NETHER_BRICKS, 0.005);
        blockEffects.put(Material.END_STONE_BRICKS, 0.005);
        blockEffects.put(Material.PRISMARINE, 0.005);
        blockEffects.put(Material.PRISMARINE_BRICKS, 0.005);
        blockEffects.put(Material.DARK_PRISMARINE, 0.005);
        blockEffects.put(Material.PURPUR_BLOCK, 0.005);
        blockEffects.put(Material.POLISHED_BLACKSTONE, 0.005);
        blockEffects.put(Material.POLISHED_BLACKSTONE_BRICKS, 0.005);
        blockEffects.put(Material.BLACKSTONE, 0.005);
        blockEffects.put(Material.POLISHED_BLACKSTONE, 0.005);
        blockEffects.put(Material.POLISHED_BLACKSTONE_BRICKS, 0.005);
        blockEffects.put(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS, 0.005);
        blockEffects.put(Material.CHISELED_POLISHED_BLACKSTONE, 0.005);
        blockEffects.put(Material.GILDED_BLACKSTONE, 0.005);
        blockEffects.put(Material.BASALT, 0.005);
        blockEffects.put(Material.POLISHED_BASALT, 0.005);
        blockEffects.put(Material.BLACKSTONE, 0.005);
        blockEffects.put(Material.POLISHED_BLACKSTONE, 0.005);
        blockEffects.put(Material.POLISHED_BLACKSTONE_BRICKS, 0.005);
        blockEffects.put(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS, 0.005);
        blockEffects.put(Material.CHISELED_POLISHED_BLACKSTONE, 0.005);
        blockEffects.put(Material.GILDED_BLACKSTONE, 0.005);
        blockEffects.put(Material.BASALT, 0.005);
        blockEffects.put(Material.POLISHED_BASALT, 0.005);
        blockEffects.put(Material.BLACKSTONE, 0.005);
        blockEffects.put(Material.POLISHED_BLACKSTONE, 0.005);
        blockEffects.put(Material.POLISHED_BLACKSTONE_BRICKS, 0.005);
        blockEffects.put(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS, 0.005);
        blockEffects.put(Material.CHISELED_POLISHED_BLACKSTONE, 0.005);
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
        } else {
            double level = newLevels.getOrDefault(realm.owner, realm.level);
            level += 0.001;
            newLevels.put(realm.owner, level);
        }
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
        } else {
            double level = newLevels.getOrDefault(realm.owner, realm.level);
            level -= 0.001;
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
            level += 0.2 / 10;
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
            level -= 0.2 / 10;
            if (level <= 0) return;
            newLevels.put(realm.owner, level);
        }
    }
}