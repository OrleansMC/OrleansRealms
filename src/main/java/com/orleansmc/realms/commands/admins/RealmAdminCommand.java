package com.orleansmc.realms.commands.admins;

import com.google.common.collect.ImmutableList;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.utils.Util;
import me.lucko.helper.Commands;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

public class RealmAdminCommand {
    private static OrleansRealms plugin;

    public static void setup(OrleansRealms plugin) {
        RealmAdminCommand.plugin = plugin;

        Commands.create().assertOp().assertPermission("orleansmc.realms.admin").tabHandler(c -> {
            if (c.args().size() == 1) {
                return ImmutableList.of("sil", "boyut", "spawn-ayarla");
            } else if (c.args().size() >= 2) {
                if (c.args().get(0).equalsIgnoreCase("sil") && c.args().size() == 2) {
                    return plugin.realmsManager.realms.values().stream().map(realm -> realm.owner).collect(ImmutableList.toImmutableList());
                }
                if (c.args().get(0).equalsIgnoreCase("boyut")) {
                    if (c.args().size() == 2) {
                        return plugin.realmsManager.realms.values().stream().map(realm -> realm.owner).collect(ImmutableList.toImmutableList());
                    }
                    if (c.args().size() == 3) {
                        return ImmutableList.of("150x150", "300x300", "500x500", "1000x1000");
                    }
                }
            }
            return ImmutableList.of();
        }).handler(c -> {
            if (c.args().isEmpty()) {
                c.sender().sendMessage("§cKullanım: /diyar-admin <sil|boyut> [oyuncu|boyut]");
                return;
            }

            if (c.args().get(0).equalsIgnoreCase("sil")) {
                String playerName = c.args().get(1);
                RealmModel realm = plugin.realmsManager.getRealm(playerName);

                if (realm == null) {
                    c.sender().sendMessage("§c" + playerName + " adlı oyuncunun bir diyarı bulunamadı.");
                    return;
                }
                deleteRealm(realm);
                c.sender().sendMessage("§a" + playerName + " adlı oyuncunun diyarı silindi.");
            } else if (c.args().get(0).equalsIgnoreCase("boyut")) {
                String playerName = c.args().get(1);
                RealmModel realm = plugin.realmsManager.getRealm(playerName);

                if (realm == null) {
                    c.sender().sendMessage("§c" + playerName + " adlı oyuncunun bir diyarı bulunamadı.");
                    return;
                }

                String size = c.args().get(2);
                int newSize = Integer.parseInt(size.split("x")[0]);
                if (newSize < 10 || newSize > 1300) {
                    c.sender().sendMessage("§cBoyut 10x10 ve 1000x1000 arasında olmalıdır.");
                    return;
                }
                changeRealmRadius(realm, newSize / 2);
            } else if (c.args().get(0).equalsIgnoreCase("spawn-ayarla")) {
                if (!(c.sender() instanceof Player admin)) {
                    c.sender().sendMessage("§cBu komut sadece oyuncular tarafından kullanılabilir.");
                    return;
                }

                RealmModel realm = plugin.realmsManager.getRealmByLocation(
                        admin.getLocation()
                );

                if (realm == null) {
                    c.sender().sendMessage("§cBu konumda bir diyar bulunamadı.");
                    return;
                }

                realm.spawn = Util.getStringFromLocation(admin.getLocation());
                plugin.realmsManager.saveRealm(realm);
                c.sender().sendMessage("§a" + realm.owner + " adlı oyuncunun diyarının spawn noktası ayarlandı.");
            }
        }).registerAndBind(plugin, "realm-admin", "diyar-admin");
    }

    private static void deleteRealm(RealmModel realm) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.realmsManager.deleteRealm(realm.owner);
        });
    }

    private static void changeRealmRadius(RealmModel realm, int newRadius) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.realmsManager.realmsRedisManager.changeRealmRadius(realm.owner, newRadius);
        });
    }
}
