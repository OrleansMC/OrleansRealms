package com.orleansmc.realms.placeholders;

import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.managers.QuestManager;
import com.orleansmc.realms.utils.Util;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.managers.RegionManager;
import com.orleansmc.realms.models.data.RealmMemberModel;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.enums.RealmMember;
import me.clip.placeholderapi.PlaceholderAPI;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Objective;
import me.pikamug.quests.quests.components.Stage;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class RealmsExpansion extends PlaceholderExpansion {

    private final OrleansRealms plugin;

    public RealmsExpansion(OrleansRealms plugin) {
        this.plugin = plugin;
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "MustqfaCan";
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "realms";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equals("time")) {
            ZoneId turkeyZoneId = ZoneId.of("Europe/Istanbul");
            ZonedDateTime turkeyTime = ZonedDateTime.now(turkeyZoneId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return turkeyTime.format(formatter);
        }

        if (player == null) {
            return null;
        }

        switch (params) {
            case "pretty_gem" -> {
                Player onlinePlayer = player.getPlayer();
                if (onlinePlayer == null) {
                    return "";
                }
                return Util.prettyNumber((long) plugin.getGemCurrency().getBalance(onlinePlayer));
            }
            case "pretty_credit" -> {
                Player onlinePlayer = player.getPlayer();
                if (onlinePlayer == null) {
                    return "";
                }
                return Util.prettyNumber((long) plugin.getCreditCurrency().getBalance(onlinePlayer));
            }
            case "quests_start_message" -> {
                Player onlinePlayer = player.getPlayer();
                if (onlinePlayer == null) {
                    return "";
                }
                Quester quester = QuestManager.questsPlugin.getQuester(player.getUniqueId());
                Quest quest = quester.getCurrentQuests().keySet().stream().findFirst().orElse(null);
                if (quest == null) {
                    return "";
                }
                Stage stage = quester.getCurrentStage(quest);
                if (stage == null) {
                    return "";
                }
                return Util.stripColor(stage.getStartMessage());
            }
            case "quest_objectives" -> {
                String questObjectives = PlaceholderAPI.setPlaceholders(player, "%quests_player_compass_quest_objectives%");
                Player onlinePlayer = player.getPlayer();
                if (onlinePlayer == null) {
                    return Util.stripColor(questObjectives);
                }
                questObjectives = plugin.questManager.getPlayerObjectiveTitle(onlinePlayer, questObjectives);
                return Util.stripColor(questObjectives);
            }
        }

        if (params.startsWith("bossbar") || params.equals("nameplate_tag")) {
            if (plugin.serversManager.getCurrentServerType() != ServerType.REALMS) {
                return "";
            }

            final Player onlinePlayer = player.getPlayer();
            if (onlinePlayer == null) {
                return "";
            }

            final Location playerLocation = onlinePlayer.getLocation();
            final int[] region = RegionManager.getRegionFromLocation(playerLocation.getBlockX(), playerLocation.getBlockZ());
            final RealmModel realm = plugin.realmsManager.getRealmByRegionCoordinates(region[0], region[1]);
            if (realm == null) {
                return "";
            }

            switch (params) {
                case "bossbar_name" -> {
                    return realm.owner;
                }
                case "bossbar_level" -> {
                    return String.valueOf(realm.level);
                }
                case "bossbar_size" -> {
                    int size = realm.radius * 2;
                    return size + "x" + size;
                }
                case "bossbar_climate" -> {
                    switch (realm.climate) {
                        case COLD -> {
                            return "Karasal";
                        }
                        case HOT_BARREN -> {
                            return "Çöl";
                        }
                        case DRY_VEGETATION -> {
                            return "Savan";
                        }
                        case HUMID_VEGETATION -> {
                            return "Nemli";
                        }
                        case SNOWY -> {
                            return "Karlı";
                        }
                    }
                }
            }

            final String playerType;
            final RealmMemberModel member = plugin.realmsManager.getRealmMember(realm.owner, onlinePlayer.getName());
            if (member == null) {
                playerType = "visitor";
            } else if (realm.owner.equalsIgnoreCase(onlinePlayer.getName())) {
                playerType = "owner";
            } else if (member.rank == RealmMember.MANAGER) {
                playerType = "manager";
            } else if (member.rank == RealmMember.WORKER) {
                playerType = "worker";
            } else {
                playerType = "member";
            }
            if (params.equals("bossbar_prefix")) {
                final OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(realm.owner);
                String groupName = PlaceholderAPI.setPlaceholders(offlinePlayer, "%vault_group%");
                if (groupName.equalsIgnoreCase("default")) {
                    groupName = "player";
                }
                final String realmTag = PlaceholderAPI.setPlaceholders(offlinePlayer, "%img_realm_tag_" + groupName.toLowerCase() + "%");
                return realmTag + " " + Util.getColorCodeFromPlayerPrefix(realm.owner);
            }
            if (params.equals("nameplate_tag")) {
                return PlaceholderAPI.setPlaceholders(player, "%img_realm_" + playerType + "_nameplate" + "%");
            }
            final String playerTag = PlaceholderAPI.setPlaceholders(player, "%img_realm_" + playerType + "%");
            if (params.equals("bossbar_tag")) {
                return playerTag;
            }
            if (params.equals("bossbar_space")) {
                return switch (playerType) {
                    case "owner" -> " ".repeat(15);
                    case "manager" -> " ".repeat(19);
                    case "worker" -> " ".repeat(10) + "\uF801 ";
                    case "visitor" -> " ".repeat(11) + "\uF801 \uF801 \uF801 ";
                    default -> " ".repeat(13);
                };
            }
            return null;
        }

        return null; // Placeholder is unknown by the Expansion
    }
}