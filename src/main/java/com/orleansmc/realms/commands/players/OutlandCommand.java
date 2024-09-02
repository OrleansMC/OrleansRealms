package com.orleansmc.realms.commands.players;

import com.google.common.collect.ImmutableList;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.menus.OutlandMenu;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

public class OutlandCommand {
    public static void setup(OrleansRealms plugin) {
        Commands.create()
                .assertPlayer()
                .tabHandler(c -> ImmutableList.of())
                .handler(c -> {
                    Player player = c.sender();
                    new OutlandMenu(player, plugin, null).open();
                })
                .registerAndBind(plugin, "vahşi-dünya", "outland");
    }
}
