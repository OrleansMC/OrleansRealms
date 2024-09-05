package com.orleansmc.realms.utils;

import com.google.common.collect.ImmutableList;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.enums.RealmMember;
import com.orleansmc.realms.enums.RealmTime;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    public static String removeHexCodes(String input) {
        // Hex kodlarını yakalamak için regex deseni
        String hexPattern = "#[A-Fa-f0-9]{6}";
        // Regex kullanarak hex kodlarını kaldırma
        return input.replaceAll(hexPattern, "");
    }

    public static String getWorldNameFromLocationString(String locationString) {
        return locationString.split(":")[0];
    }

    public static Location getLocationFromString(String configString) {
        String spawnWorld = configString.split(":")[0];
        String[] spawnCoordinates = configString.split(":")[1].split(",");

        return new Location(Bukkit.getWorld(spawnWorld), Double.parseDouble(spawnCoordinates[0]), Double.parseDouble(spawnCoordinates[1]), Double.parseDouble(spawnCoordinates[2]), Float.parseFloat(spawnCoordinates[3]), Float.parseFloat(spawnCoordinates[4]));
    }

    public static String getStringFromLocation(Location location) {
        return location.getWorld().getName() + ":" + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
    }

    public static int[] getRegionCoordinatesFromString(String regionString) {
        String[] regionCoordinates = regionString.split(",");
        return new int[]{Integer.parseInt(regionCoordinates[0]), Integer.parseInt(regionCoordinates[1])};
    }

    public static String getFirstHexColor(String input) {
        // Hex kodlarını yakalamak için regex deseni
        String hexPattern = "#[A-Fa-f0-9]{6}";
        // İlk hex kodunu döndürme
        Matcher matcher = Pattern.compile(hexPattern).matcher(input);
        return matcher.find() ? matcher.group() : null;
    }

    public static ImmutableList<String> getAllPlayers(OrleansRealms plugin) {
        return ImmutableList.copyOf(plugin.serversManager.getServerStates().values().stream().map(serverState -> serverState.players).reduce(new ArrayList<>(), (players, players2) -> {
            players.addAll(players2);
            return players;
        }));
    }

    public static String formatTime(long ms) {
        long secNum = ms / 1000;
        long days = secNum / 86400;
        long hours = (secNum - (days * 86400)) / 3600;
        long minutes = (secNum - (days * 86400) - (hours * 3600)) / 60;
        long seconds = secNum - (days * 86400) - (hours * 3600) - (minutes * 60);

        StringBuilder result = new StringBuilder();
        if (days > 0) result.append(days).append(" Gün");
        if (hours > 0) result.append((!result.isEmpty() ? ", " : "")).append(hours).append(" Saat");
        if (minutes > 0 && (days == 0 || hours == 0))
            result.append((!result.isEmpty() ? ", " : "")).append(minutes).append(" Dakika");
        if (seconds > 0 && (hours == 0 || minutes == 0))
            result.append((!result.isEmpty() ? ", " : "")).append(seconds).append(" Saniye");

        return result.toString();
    }

    public static String hexToMinecraftStyle(String hexColor) {
        final String hex = hexColor.substring(1); // remove the #
        StringBuilder minecraftFormat = new StringBuilder("§x");
        for (char c : hex.toCharArray()) {
            minecraftFormat.append("§").append(c);
        }
        return minecraftFormat.toString();
    }

    public static String stripColor(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "");
    }

    public static String getColorCodeFromPlayerPrefix(String playerName) {
        // Get the offline player
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        // Get the player prefix
        String input = PlaceholderAPI.setPlaceholders(offlinePlayer, "%realms_prefix%");
        // Regex to match the color code pattern
        Pattern pattern = Pattern.compile("#([a-fA-F0-9]{6})");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // Extract the color code
            String colorCode = matcher.group(1);
            // Build the output string
            StringBuilder result = new StringBuilder("§x");
            for (char c : colorCode.toCharArray()) {
                result.append("§").append(c);
            }
            return result.toString();
        } else {
            throw new IllegalArgumentException("Invalid color code format");
        }
    }

    public static int getTimeByRealmTimeType(RealmTime timeType) {
        return switch (timeType) {
            case DAY -> 6000;
            case NIGHT -> 20000;
            case SUNSET -> 13000;
            case SUNRISE -> 23500;
            default -> 0;
        };
    }

    public static String getExclamation() {
        return PlaceholderAPI.setPlaceholders(null, "%img_exclamation%") + " ";
    }

    public static String stripMiniMessage(String miniMessageText) {
        Component component = MiniMessage.miniMessage().deserialize(miniMessageText);
        String legacyText = LegacyComponentSerializer.legacySection().serialize(component);
        return stripColor(legacyText);
    }

    public static String getHexColorFromRealmMemberRank(RealmMember rank) {
        return switch (rank) {
            case MANAGER -> "#ff4848";
            case MEMBER -> "#ff9148";
            case WORKER -> "#b6b625";
        };
    }

    public static boolean hasCustomTexture(ItemStack item) {
        if (item == null) return false;

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            return meta.hasCustomModelData();
        }

        return false;
    }

    public static String prettyNumber(long number) {
        // Milyon gruplamak için kalıp (pattern)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.'); // Gruplama ayıracı olarak nokta kullan

        DecimalFormat df = new DecimalFormat("#,###,###", symbols);

        // Sonucu döndür
        return df.format(number);
    }

    public static Location findSafeLocation(Location location) {
        for (int y = 0; y < 100; y++) {
            if (location.clone().add(0, -y, 0).getBlock().getType().isSolid()) {
                location.setX(location.toCenterLocation().getX());
                location.setZ(location.toCenterLocation().getZ());
                location.setY(location.blockY() - y + 1);
                break;
            }
        }
        return location;
    }
}