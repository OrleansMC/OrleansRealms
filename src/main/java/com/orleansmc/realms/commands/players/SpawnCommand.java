package com.orleansmc.realms.commands.players;

import com.google.common.collect.ImmutableList;
import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.spawn.Spawn;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

public class SpawnCommand {
    public static void setup(OrleansRealms plugin) {
        Commands.create()
                .assertPlayer()
                .tabHandler(c -> ImmutableList.of())
                .handler(c -> {
                    Player player = c.sender();
                    String availableServer = plugin.serversManager.getServerStates().values()
                            .stream()
                            .filter(serverState -> ServerType.valueOf(serverState.type) == ServerType.REALMS_SPAWN)
                            .min((o1, o2) -> o2.players.size() - o1.players.size())
                            .map(serverState -> serverState.name)
                            .orElse("lobby");

                    plugin.serversManager.teleportPlayer(
                            player,
                            Spawn.LOCATION,
                            "spawn",
                            availableServer
                    );
                })
                .registerAndBind(plugin, "spawn", "hub", "lobi", "lobby");
    }
}
