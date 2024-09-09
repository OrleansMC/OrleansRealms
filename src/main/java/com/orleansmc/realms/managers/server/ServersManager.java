package com.orleansmc.realms.managers.server;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.orleansmc.bukkit.teleport.TeleportProvider;
import com.orleansmc.common.servers.ServerState;
import com.orleansmc.common.servers.ServerType;
import com.orleansmc.common.servers.ServersProvider;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class ServersManager {
    public final ServersProvider serversProvider;
    public final OrleansRealms plugin;

    public ServersManager(OrleansRealms plugin) {
        this.plugin = plugin;
        serversProvider = Bukkit.getServer().getServicesManager().load(ServersProvider.class);

        if (serversProvider == null) {
            throw new IllegalStateException("ServersProvider not found");
        }
    }

    public ServerType getCurrentServerType() {
        return ServerType.valueOf(serversProvider.getServerStates().get(Settings.SERVER_NAME).type);
    }

    public HashMap<String, ServerState> getServerStates() {
        return serversProvider.getServerStates();
    }

    public void teleportPlayerToServer(Player player, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    public void teleportPlayer(Player player, Location location, String worldName, String serverName) {
        TeleportProvider teleportProvider = Bukkit.getServer().getServicesManager().load(TeleportProvider.class);
        if (teleportProvider == null) {
            throw new IllegalStateException("TeleportProvider not found");
        }
        teleportProvider.teleportPlayer(player, location, worldName, serverName);
    }
}
