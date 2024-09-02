package com.orleansmc.realms.managers;

import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class EntityLagManager implements Listener {
    private final OrleansRealms plugin;
    public final World world;

    public EntityLagManager(OrleansRealms plugin) {
        plugin.getLogger().info("EntityLagManager loaded.");
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (plugin.getServer().getTPS()[0] < 18.5) {
                plugin.getLogger().warning("Server TPS is below 18.5. Removing entities.");
                removeEntities();
            }
        }, 0, 20 * 2);

        plugin.getServer().getScheduler().runTaskTimer(plugin, this::detectEntities, 0, 20 * 20);

        world = Bukkit.getWorld(Settings.WORLD_NAME);
        if (world == null) {
            throw new RuntimeException("World " + Settings.WORLD_NAME + " not found");
        }
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
                "item",
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
    }
}
