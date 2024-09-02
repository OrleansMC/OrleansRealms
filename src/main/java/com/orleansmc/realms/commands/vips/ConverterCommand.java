package com.orleansmc.realms.commands.vips;

import com.google.common.collect.ImmutableList;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.menus.ConverterMenu;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

public class ConverterCommand {
    public static void setup(OrleansRealms plugin) {
        Commands.create()
                .assertPermission("orleansmc.realms.admin")
                .tabHandler(c -> ImmutableList.of())
                .handler(c -> {
                    Player sender = null;
                    if (c.sender() instanceof Player) {
                        sender = (Player) c.sender();
                    }
                    String playerName = c.args().size() == 1 ? c.args().getFirst() : sender != null ? sender.getName() : null;
                    if (playerName == null) {
                        c.reply("§cKullanım: /converter <oyuncu>");
                        return;
                    }
                    if (sender != null && !sender.hasPermission("orleansmc.realms.admin")) {
                        playerName = sender.getName();
                    }
                    Player player = plugin.getServer().getPlayer(playerName);
                    new ConverterMenu(player, plugin).open();
                })
                .register("converter", "donusturucu", "dönüştürücü");
    }
}
