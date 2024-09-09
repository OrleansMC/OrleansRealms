package com.orleansmc.realms.managers.player;

import com.orleansmc.bukkit.players.PlayersProvider;
import com.orleansmc.bukkit.players.models.PlayerModel;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.managers.common.LuckPermsManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class PlayerDataManager {
    OrleansRealms plugin;
    public PlayersProvider playersProvider;

    public PlayerDataManager(OrleansRealms plugin) {
        this.plugin = plugin;
        this.playersProvider = plugin.getService(PlayersProvider.class);
    }

    public PlayerModel getPlayerData(String playerName) {
        PlayerModel playerModel = playersProvider.getPlayer(playerName);
        if (playerModel == null) {
            Player player = plugin.getServer().getPlayer(playerName);
            if (player != null) {
                player.kick(Component.text("§cOyuncu verileri yüklenemedi. Lütfen tekrar giriş yapın!"));
                throw new RuntimeException("Player data not found for " + playerName);
            } else {
                throw new RuntimeException("Player is not online: " + playerName + ". Cannot load player data.");
            }
        }

        return playerModel;
    }

    public void savePlayerData(PlayerModel playerModel) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            playersProvider.savePlayer(playerModel);
        });
    }

    public boolean getRealmDeleteRight(Player player) {
        PlayerModel playerModel = getPlayerData(player.getName());
        if (playerModel == null) {
            throw new RuntimeException("Player data not found player is probably offline: " + player.getName());
        }
        if (playerModel.totalDeletedRealms < 1) {
            return true;
        } else {
            return LuckPermsManager.hasBenefit(player, "delete_own_realm");
        }
    }

    public void waitPlayerDataThenRun(Player player, Consumer<PlayerModel> consumer) {
        playersProvider.waitPlayerDataThenRun(player, consumer, 0);
    }
}
