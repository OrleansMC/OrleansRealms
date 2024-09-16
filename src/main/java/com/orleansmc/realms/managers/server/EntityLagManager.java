package com.orleansmc.realms.managers.server;

import com.orleansmc.common.servers.ServerState;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.models.config.TextModel;
import com.orleansmc.realms.utils.Util;
import com.orleansmc.common.redis.RedisProvider;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.stream.Collectors;

public class EntityLagManager implements Listener {
    private final RedisProvider redis;
    private final OrleansRealms plugin;
    // 10 minutes
    private final int ITEM_CLEANUP_INTERVAL = 1000 * 60 * 10;
    public final World world;

    public EntityLagManager(OrleansRealms plugin) {
        plugin.getLogger().info("EntityLagManager loaded.");
        this.plugin = plugin;
        this.redis = plugin.getService(RedisProvider.class);
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (plugin.getServer().getTPS()[0] < 18.5) {
                plugin.getLogger().warning("Server TPS is below 18.5. Removing entities.");
                removeEntities();
            }
        }, 0, 20 * 2);

        plugin.getServer().getScheduler().runTaskTimer(plugin, this::checkItemCleanUp, 0, 20 * 10);
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::detectEntities, 0, 20 * 20);

        world = Bukkit.getWorld(Settings.WORLD_NAME);
        if (world == null) {
            throw new RuntimeException("World " + Settings.WORLD_NAME + " not found");
        }
    }

    public void checkItemCleanUp() {
        this.world.setTime(
                plugin.serversManager.serversProvider
                        .getAvailableServerByType(
                                plugin.serversManager.getCurrentServerType()
                        ).time
        );
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ServerState anyServer = plugin.serversManager.serversProvider.getAnyServer();
            if (anyServer == null) {
                plugin.getLogger().warning("Any server not found.");
                return;
            }
            String lastCleanUpDate = redis.get("last_item_cleanup");
            if (lastCleanUpDate == null || Long.parseLong(lastCleanUpDate) < System.currentTimeMillis() - (ITEM_CLEANUP_INTERVAL + 10 * 1000)) {
                if (anyServer.name.equals(Settings.SERVER_NAME)) {
                    plugin.getLogger().info("The last item cleanup was more than 10 minutes ago. New value forced.");
                    redis.set("last_item_cleanup", String.valueOf(System.currentTimeMillis()));
                    return;
                }
            }

            if (anyServer.name.equals(Settings.SERVER_NAME)) {
                if (Long.parseLong(lastCleanUpDate) > System.currentTimeMillis() - ITEM_CLEANUP_INTERVAL) {
                    long timeLeft = ITEM_CLEANUP_INTERVAL - (System.currentTimeMillis() - Long.parseLong(lastCleanUpDate));
                    if (timeLeft / 1000 == 120 || timeLeft / 1000 == 30) {
                        String formattedTime = Util.formatTime(timeLeft);
                        String message = Util.getExclamation() + "Yerdeki eşyaların temizlenmesine <color:#ff0000>" + formattedTime + "</color> kaldı.";
                        plugin.realmsManager.messageManager.sendMessage(
                                "all",
                                new TextModel(message, message)
                        );
                    }
                    return;
                }

                plugin.getLogger().info("Cleaning up items.");
                String message = Util.getExclamation() + "<color:#ff0000>Yerdeki eşyalar temizlendi!</color>";
                plugin.realmsManager.messageManager.sendMessage(
                        "all",
                        new TextModel(message, message)
                );
                redis.set("last_item_cleanup", String.valueOf(System.currentTimeMillis()));
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof Item item) {
                        if (item.getTicksLived() > 20 * 60) {
                            item.remove();
                        }
                    }
                }
            });
        });
    }

    public void detectEntities() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Chunk chunk : world.getLoadedChunks()) {
                Entity[] monsters = Arrays.stream(chunk.getEntities())
                        .filter(entity -> entity instanceof Monster)
                        .toArray(Entity[]::new);

                int monstersCount = monsters.length;
                if (monstersCount > 15) {
                    plugin.getLogger().warning("Chunk " + chunk.getX() + ", " + chunk.getZ() + " has " + monstersCount + " monsters.");

                    // Tüm entity'leri bir defada saymak
                    Map<EntityType, Long> entityCount = Arrays.stream(monsters)
                            .collect(Collectors.groupingBy(Entity::getType, Collectors.counting()));

                    List<Entity> entitiesToRemove = new ArrayList<>();
                    plugin.getLogger().warning("Removing " + (monstersCount - 15) + " monsters.");
                    for (int i = 0; i < monstersCount - 15; i++) {
                        // En fazla sayıda bulunan entity tipini belirleme
                        EntityType entityTypeToRemove = entityCount.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse(null);

                        if (entityTypeToRemove == null) {
                            plugin.getLogger().warning("No entity type found to remove.");
                            return;
                        }

                        for (Entity entity : monsters) {
                            if (entity.getType() == entityTypeToRemove) {
                                entitiesToRemove.add(entity);
                                entityCount.put(entityTypeToRemove, entityCount.get(entityTypeToRemove) - 1);
                                if (entityCount.get(entityTypeToRemove) <= 0) {
                                    entityCount.remove(entityTypeToRemove);
                                }
                                monstersCount--;
                                if (monstersCount <= 15) break;
                            }
                        }
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (Entity entity : entitiesToRemove) {
                            entity.remove();
                        }
                    });
                }

                Entity[] animals = Arrays.stream(chunk.getEntities())
                        .filter(entity -> entity instanceof Animals)
                        .toArray(Entity[]::new);

                int animalsCount = animals.length;
                if (animalsCount > 10) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (int i = 0; i < animalsCount - 10; i++) {
                            if (animals[i] instanceof Animals animal) {
                                animal.setAI(false);
                            }
                        }
                    });
                }

                Entity[] armorStands = Arrays.stream(chunk.getEntities())
                        .filter(entity -> (entity instanceof ArmorStand) && !Util.hasCustomTexture(((ArmorStand) entity).getEquipment().getHelmet()))
                        .toArray(Entity[]::new);

                int armorStandsCount = armorStands.length;

                if (armorStandsCount > 15) {
                    plugin.getLogger().warning("Chunk " + chunk.getX() + ", " + chunk.getZ() + " has " + armorStandsCount + " armor stands.");
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (int i = 0; i < armorStandsCount - 15; i++) {
                            armorStands[i].remove();
                        }
                    });
                }
            }
        });
    }

    public void removeEntities() {
        List<String> entityTypes = Arrays.asList(
                "zombie",
                "creeper",
                "skeleton",
                "iron_golem",
                "spider",
                "bat",
                "slime",
                "witch",
                "enderman"
        );
        for (String entityType : entityTypes) {
            Bukkit.getServer().dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "minecraft:kill @e[type=" + entityType + "]"
            );
        }
        Bukkit.getServer().dispatchCommand(
                Bukkit.getConsoleSender(),
                "mm mobs killall"
        );
    }
}
