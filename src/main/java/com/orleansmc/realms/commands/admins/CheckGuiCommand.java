package com.orleansmc.realms.commands.admins;

import com.google.common.collect.ImmutableList;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.menus.CheckGuiMenu;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

public class CheckGuiCommand {
    public static void setup(OrleansRealms plugin) {
        Commands.create()
                .assertOp()
                .assertPermission("orleansmc.realms.admin")
                .tabHandler(c -> {
                    if (c.args().size() > 2) {
                        return ImmutableList.of();
                    } else {
                        return ImmutableList.of("ui:img_name");
                    }
                })
                .handler(c -> {
                    if (!(c.sender() instanceof Player player)) {
                        c.sender().sendMessage("§cBu komutu sadece oyuncular kullanabilir.");
                        return;
                    }
                    String namespacedID = c.args().isEmpty() ? null : c.args().get(0);
                    if (namespacedID == null) {
                        player.sendMessage("§cKullanım: /kontrol-gui <namespacedID>");
                        return;
                    }
                    new CheckGuiMenu(player, plugin, namespacedID).open();
                })
                .registerAndBind(plugin, "check-gui", "kontrol-gui");
    }
}
