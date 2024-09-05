package com.orleansmc.realms.managers;

import com.orleansmc.realms.OrleansRealms;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.track.Track;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Objects;
import java.util.Optional;


public class LuckPermsManager {
    public static LuckPerms api;
    private static OrleansRealms plugin;
    private static Permission perms;
    private static String vipTrackID = "vips";
    private static String staffTrackID = "staff";

    public static void setup(OrleansRealms plugin) {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider == null) {
            throw new RuntimeException("LuckPerms is not installed.");
        }

        api = provider.getProvider();
        LuckPermsManager.plugin = plugin;
        plugin.getLogger().info("LuckPerms hooked.");

        perms = plugin.getService(Permission.class);
        plugin.getLogger().info("Vault hooked.");
    }

    public static String getPlayerGroup(Player player) {
        User user = api.getPlayerAdapter(Player.class).getUser(player);
        return user.getPrimaryGroup();
    }

    public static boolean hasPermission(OfflinePlayer player, String permission) {
        return perms.playerHas(null, player, permission);
    }

    public static boolean hasVIP(Player player) {
        Track track = api.getTrackManager().getTrack(vipTrackID);
        if (track == null) {
            plugin.getLogger().warning("VIP TRACK NOT FOUND");
            return false;
        }
        for (String group : track.getGroups()) {
            if (player.hasPermission("group." + group)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlayerInGroup(Player player, String group) {
        return player.hasPermission("group." + group);
    }

    public static boolean hasBenefit(Player player, String benefit) {
        return player.hasPermission("orleansmc.realms." + benefit);
    }

    public static int getBenefitLevelWithValues(OfflinePlayer player, String benefit, int defaultValue, int[] values) {
        String worldName = player.isOnline() ? Objects.requireNonNull(player.getPlayer()).getWorld().getName() : plugin.entityLagManager.world.getName();
        for (int value : values) {
            if (perms.playerHas(worldName, player, "orleansmc.realms." + benefit + "." + value)) {
                return value;
            }
        }
        return defaultValue;
    }

    public static int getBenefitLevel(OfflinePlayer player, String benefit, int defaultValue, int multiplier) {
        String worldName = player.isOnline() ? Objects.requireNonNull(player.getPlayer()).getWorld().getName() : plugin.entityLagManager.world.getName();
        for (int i = 20; i > 0 ; i--) {
            if (perms.playerHas(worldName, player, "orleansmc.realms." + benefit + "." + i * multiplier)) {
                return i;
            }
        }
        return defaultValue;
    }
}
