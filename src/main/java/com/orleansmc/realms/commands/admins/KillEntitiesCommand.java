package com.orleansmc.realms.commands.admins;

import com.orleansmc.realms.OrleansRealms;
import me.lucko.helper.Commands;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.util.List;

public class KillEntitiesCommand {
    public static void setup(OrleansRealms plugin) {
        Commands.create()
                .assertOp()
                .assertPermission("orleansmc.realms.admin")
                .handler(c -> {
                    int distance = 0;
                    if (!c.args().isEmpty()) {
                        try {
                            distance = Integer.parseInt(c.args().get(0));
                        } catch (NumberFormatException e) {
                            c.sender().sendMessage("§cMesafe sayısı geçersiz.");
                            return;
                        }
                    }
                    Location location = null;
                    if (c.sender() instanceof Player player) {
                        location = player.getLocation();
                    }

                    List<Entity> entities;
                    int count = 0;
                    if (location != null && distance > 0) {
                        entities = location.getNearbyEntities(distance, distance, distance).stream().toList();
                        for (Entity entity : entities) {
                            if (!(entity instanceof Player)) {
                                entity.remove();
                                count++;
                            }
                        }
                    } else {
                        entities = plugin.entityLagManager.world.getEntities();

                        for (Entity entity : entities) {
                            if (entity instanceof Monster || entity instanceof Item || entity instanceof Projectile || entity instanceof ExperienceOrb) {
                                entity.remove();
                                count++;
                            }
                        }
                    }
                    c.sender().sendMessage("§a" + count + " entity temizlendi. (" + ((distance == 0 ? "Tüm dünya" : "Mesafe: " + distance) + ")"));
                })
                .registerAndBind(plugin, "kill-entities", "entity-temizle");
    }
}
