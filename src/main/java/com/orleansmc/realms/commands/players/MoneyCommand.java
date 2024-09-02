package com.orleansmc.realms.commands.players;

import com.google.common.collect.ImmutableList;
import com.orleansmc.realms.OrleansRealms;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

public class MoneyCommand {
    public static void setup(OrleansRealms plugin) {
        Commands.create()
                .assertPlayer()
                .tabHandler(c -> ImmutableList.of())
                .handler(c -> {
                    Player player = c.sender();
                    double gemAmount = plugin.redisEconomyAPI.getDefaultCurrency().getBalance(player.getName());
                    player.sendMessage("§aMücevher Miktarınız: §e" + gemAmount + "§r" + plugin.redisEconomyAPI.getDefaultCurrency().getCurrencySingular());
                })
                .registerAndBind(plugin, "para", "bakiye", "gem", "mücevher");
    }
}
