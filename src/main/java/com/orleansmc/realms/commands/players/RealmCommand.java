package com.orleansmc.realms.commands.players;

import com.google.common.collect.ImmutableList;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.menus.RealmSettingsMenu;
import com.orleansmc.realms.quests.objectives.InviteMembersObjective;
import com.orleansmc.realms.utils.Util;
import com.orleansmc.realms.configs.texts.Texts;
import com.orleansmc.realms.managers.realm.RegionManager;
import com.orleansmc.realms.menus.RealmMenu;
import com.orleansmc.realms.menus.SelectClimateMenu;
import com.orleansmc.realms.models.messaging.InviteModel;
import com.orleansmc.realms.models.data.RealmMemberModel;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.enums.RealmClimate;
import com.orleansmc.realms.enums.RealmMember;
import me.lucko.helper.Commands;
import me.lucko.helper.messaging.Channel;
import me.lucko.helper.messaging.ChannelAgent;
import me.lucko.helper.messaging.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class RealmCommand {
    private static OrleansRealms plugin;
    public static ChannelAgent<InviteModel> channelAgent;
    public static List<InviteModel> invites = new ArrayList<>();

    public static void setup(OrleansRealms plugin) {
        RealmCommand.plugin = plugin;

        final Messenger messenger = Bukkit.getServer().getServicesManager().load(Messenger.class);
        if (messenger == null) {
            throw new RuntimeException("Messenger service not found");
        }

        final Channel<InviteModel> channel = messenger.getChannel("orleansmc.realms:invites", InviteModel.class);
        channelAgent = channel.newAgent();

        channelAgent.addListener((channelId, message) -> {
            String player = message.inviter;
            String realm = message.realm;
            String member = message.member;
            String rank = message.rank;

            Player memberCandidate = Bukkit.getPlayer(member);
            if (memberCandidate != null) {
                invites.add(message);
                memberCandidate.sendMessage(plugin.getComponent(
                        "<color:#FFB3BA>" + player + " seni </color>" +
                                "<color:#B3CDE3>" + realm + "</color> <color:#FFB3BA>diyarına</color> " +
                                "<color:#B3CDE3>" + rank + "</color> <color:#FFB3BA>olarak davet etti.</color>\n" +
                                "<color:#FFB3BA>Kabul etmek için <color:#B3CDE3>30 saniye</color> içinde " +
                                "<click:suggest_command:'/diyar katıl " + realm + "'><u><color:#B3CDE3>/diyar katıl " + realm + "</color></u></click> yaz."
                ));
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (invites.contains(message)) {
                        plugin.realmsManager.messageManager.sendMessage(message.inviter, Texts.REALM_INVITE_TIMEOUT.clone().addReplacement("{player}", member));
                        plugin.realmsManager.messageManager.sendMessage(member, Texts.REALM_INVITE_TIMEOUT.clone().addReplacement("{player}", player));
                        invites.remove(message);
                    }
                }, 20 * 30);
            }
        });

        Commands.create().assertPlayer().description("Create/Manage/Teleport to realms")
                .tabHandler(c -> {
                    Locale locale = c.sender().locale();
                    if (c.args().size() == 1) {
                        if (locale.getLanguage().equals("tr")) {
                            return ImmutableList.of("oluştur", "git", "üye", "katıl", "yasakla", "yasak-kaldır", "ziyaretçi", "spawn-ayarla", "ayarlar", "ayrıl")
                                    .stream().filter(o -> o.toLowerCase().startsWith(c.args().get(0).toLowerCase())).toList();
                        } else {
                            return ImmutableList.of("create", "teleport", "member", "join", "ban", "unban", "visitor", "set-spawn", "settings", "leave")
                                    .stream().filter(o -> o.toLowerCase().startsWith(c.args().get(0).toLowerCase())).toList();
                        }
                    }
                    String action = c.args().get(0).toLowerCase();
                    if ((action.equals("create") || action.equals("oluştur")) && c.args().size() == 2) {
                        if (locale.getLanguage().equals("tr")) {
                            return ImmutableList.of("karlı", "karasal", "çöl", "savan", "nemli")
                                    .stream().filter(o -> o.toLowerCase().startsWith(c.args().get(1).toLowerCase())).toList();
                        } else {
                            return ImmutableList.of("snowy", "continental", "desert", "savannah", "humid")
                                    .stream().filter(o -> o.toLowerCase().startsWith(c.args().get(1).toLowerCase())).toList();
                        }
                    }
                    if ((action.equals("teleport") || action.equals("git")) && c.args().size() == 2) {
                        return ImmutableList.copyOf(plugin.realmsManager.realms.values().stream().filter(realm -> realm.allow_visitors || realm.members.stream().anyMatch(m -> m.name.equals(c.sender().getName())))
                                .map(realm -> realm.owner)
                                .filter(o -> o.toLowerCase().startsWith(c.args().get(1).toLowerCase())).toList());
                    }
                    if (action.equals("member") || action.equals("üye")) {
                        String memberAction = c.args().get(1).toLowerCase();
                        if (memberAction.equals("add") || memberAction.equals("ekle") || memberAction.equals("rank") || memberAction.equals("rütbe") && c.args().size() >= 3) {
                            RealmModel realm = plugin.realmsManager.getRealm(c.sender().getName());
                            if (realm == null) {
                                return ImmutableList.of();
                            }

                            if (c.args().size() == 5) {
                                return ImmutableList.copyOf(getRealms(c.sender()).stream().map(r -> r.owner)
                                        .filter(o -> o.toLowerCase().startsWith(c.args().get(4).toLowerCase())).toList());
                            }
                            if (c.args().size() == 4) {
                                if (locale.getLanguage().equals("tr")) {
                                    return ImmutableList.of("işçi", "üye", "yönetici")
                                            .stream().filter(o -> o.toLowerCase().startsWith(c.args().get(3).toLowerCase())).toList();
                                } else {
                                    return ImmutableList.of("worker", "member", "manager")
                                            .stream().filter(o -> o.toLowerCase().startsWith(c.args().get(3).toLowerCase())).toList();
                                }
                            }
                            if (c.args().size() == 3) {
                                if (memberAction.equals("add") || memberAction.equals("ekle")) {
                                    List<String> players = plugin.serversManager.getServerStates().values().stream().flatMap(serverState -> serverState.players.stream()).filter(player -> realm.members.stream().noneMatch(m -> m.name.equals(player)))
                                            .filter(o -> o.toLowerCase().startsWith(c.args().get(2).toLowerCase())).toList();
                                    return ImmutableList.copyOf(players);
                                } else
                                    return ImmutableList.copyOf(
                                            realm.members.stream().map(member -> member.name).filter(Objects::nonNull)
                                                    .filter(o -> o.toLowerCase().startsWith(c.args().get(2).toLowerCase())).toList());
                            }
                        } else if (memberAction.equals("remove") || memberAction.equals("çıkar") && c.args().size() >= 3) {
                            RealmModel realm = plugin.realmsManager.getRealm(c.sender().getName());
                            if (realm == null) {
                                return ImmutableList.of();
                            }

                            if (c.args().size() == 4) {
                                return ImmutableList.copyOf(getRealms(c.sender()).stream().map(r -> r.owner)
                                        .filter(o -> o.toLowerCase().startsWith(c.args().get(3).toLowerCase())).toList());
                            }

                            if (c.args().size() == 3) {
                                return ImmutableList.copyOf(realm.members.stream().map(member -> member.name).filter(Objects::nonNull)
                                        .filter(o -> o.toLowerCase().startsWith(c.args().get(2).toLowerCase())).toList());
                            }
                        } else if (c.args().size() == 2) {
                            if (locale.getLanguage().equals("tr")) {
                                return ImmutableList.of("ekle", "çıkar", "rütbe")
                                        .stream().filter(o -> o.toLowerCase().startsWith(c.args().get(1).toLowerCase())).toList();
                            } else {
                                return ImmutableList.of("add", "remove", "rank")
                                        .stream().filter(o -> o.toLowerCase().startsWith(c.args().get(1).toLowerCase())).toList();
                            }
                        }
                    }

                    if ((action.equals("join") || action.equals("katıl")) && c.args().size() == 2) {
                        return ImmutableList.copyOf(invites.stream().filter(i -> i.member.equals(c.sender().getName())).map(i -> i.realm)
                                .filter(o -> o.toLowerCase().startsWith(c.args().get(1).toLowerCase())).toList());
                    }

                    if ((action.equals("set-spawn") || action.equals("spawn-ayarla")) && c.args().size() == 2) {
                        return ImmutableList.copyOf(getRealms(c.sender()).stream().map(r -> r.owner)
                                .filter(o -> o.toLowerCase().startsWith(c.args().get(1).toLowerCase())).toList());
                    }

                    if ((action.equals("leave") || action.equals("ayrıl")) && c.args().size() == 2) {
                        return ImmutableList.copyOf(getRealms(c.sender()).stream()
                                .filter(o -> o.members.stream().anyMatch(m -> m.name.equals(c.sender().getName())))
                                .map(r -> r.owner)
                                .filter(o -> o.toLowerCase().startsWith(c.args().get(1).toLowerCase())).toList());
                    }

                    if ((action.equals("visitor") || action.equals("ziyaretçi")) && c.args().size() >= 2) {
                        if (c.args().size() == 3) {
                            return ImmutableList.copyOf(getRealms(c.sender()).stream().map(r -> r.owner)
                                    .filter(o -> o.toLowerCase().startsWith(c.args().get(2).toLowerCase())).toList());
                        }
                        if (c.args().size() == 2) {
                            if (locale.getLanguage().equals("tr")) {
                                return ImmutableList.of("izin-ver", "reddet")
                                        .stream().filter(o -> o.toLowerCase().startsWith(c.args().get(1).toLowerCase())).toList();
                            } else {
                                return ImmutableList.of("allow", "deny")
                                        .stream().filter(o -> o.toLowerCase().startsWith(c.args().get(1).toLowerCase())).toList();
                            }
                        }
                    }
                    if (action.equals("ban") || action.equals("yasakla") && c.args().size() >= 2) {
                        if (c.args().size() == 3) {
                            return ImmutableList.copyOf(getRealms(c.sender()).stream().map(r -> r.owner)
                                    .filter(o -> o.toLowerCase().startsWith(c.args().get(2).toLowerCase())).toList());
                        }

                        if (c.args().size() == 2) {
                            return ImmutableList.copyOf(plugin.serversManager.getServerStates().values().stream().flatMap(serverState -> serverState.players.stream())
                                    .filter(o -> o.toLowerCase().startsWith(c.args().get(1).toLowerCase())).toList());
                        }
                    }

                    if (action.equals("unban") || action.equals("yasak-kaldır") && c.args().size() == 2) {
                        List<RealmModel> realms = getRealms(c.sender());
                        if (c.args().size() == 3) {
                            return ImmutableList.copyOf(realms.stream().map(r -> r.owner)
                                    .filter(o -> o.toLowerCase().startsWith(c.args().get(2).toLowerCase())).toList());
                        }

                        return ImmutableList.copyOf(realms.stream().flatMap(r -> r.banned_players.stream())
                                .filter(o -> o.toLowerCase().startsWith(c.args().get(1).toLowerCase())).toList());
                    }
                    return ImmutableList.of();
                }).handler(c -> {
                    Player player = c.sender();

                    if (c.args().isEmpty()) {
                        new RealmMenu(player, plugin, null).open();
                        return;
                    }

                    final ImmutableList<String> args = c.args();
                    final String subCommand = args.get(0).toLowerCase();

                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        switch (subCommand) {
                            case "create":
                            case "oluştur":
                                String climate = args.size() > 1 ? args.get(1).toLowerCase() : null;
                                if (climate == null) {
                                    Bukkit.getScheduler().runTask(plugin, () ->
                                            new SelectClimateMenu(player, plugin, null).open());
                                    return;
                                }
                                handleCreateRealm(player, climate);
                                break;
                            case "teleport":
                            case "git":
                                String realmOwner = args.size() > 1 ? args.get(1) : null;
                                if (realmOwner == null) {
                                    realmOwner = player.getName();
                                }

                                RealmModel realm = plugin.realmsManager.getRealm(realmOwner);
                                if (realm == null) {

                                    Bukkit.getScheduler().runTask(plugin, () ->
                                            new SelectClimateMenu(player, plugin, null).open());
                                    return;
                                }

                                if (!realm.allow_visitors && realm.members.stream().noneMatch(m -> m.name.equals(player.getName()))) {
                                    plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.THIS_REALM_NOT_RECEIVING_VISITORS);
                                    return;
                                }

                                if (realm.banned_players.contains(player.getName())) {
                                    if (!player.hasPermission("orleansmc.realms.staff")) {
                                        plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.YOU_ARE_BANNED_FROM_REALM);
                                        return;
                                    }
                                }

                                handleTeleportToRealm(player, realm);
                                break;
                            case "member":
                            case "üye":
                                handleMemberCommand(player, args);
                                break;
                            case "join":
                            case "katıl":
                                handleJoinRealm(player, args.get(1));
                                break;
                            case "ban":
                            case "yasakla":
                                handlePlayerBanCommand(player, args);
                                break;
                            case "unban":
                            case "yasak-kaldır":
                                handlePlayerUnbanCommand(player, args);
                                break;
                            case "visitor":
                            case "ziyaretçi":
                                handleVisitorCommand(player, args);
                                break;
                            case "set-spawn":
                            case "spawn-ayarla":
                                handleSetSpawnCommand(player, args);
                                break;
                            case "settings":
                            case "ayarlar":
                                Bukkit.getScheduler().runTask(plugin, () ->
                                        new RealmSettingsMenu(player, plugin, null).open());
                                break;
                            case "leave":
                            case "ayrıl":
                                handleLeaveCommand(player, args);
                                break;
                        }
                    });
                }).registerAndBind(plugin, "realm", "diyar", "realms", "diyarlar");
    }

    private static void handleJoinRealm(Player player, String realmOwner) {
        InviteModel invite = invites.stream().filter(i -> i.realm.equals(realmOwner) && i.member.equals(player.getName())).findFirst().orElse(null);
        if (invite == null) {
            player.sendMessage("§cDavet bulunamadı.");
            return;
        }

        RealmModel realm = plugin.realmsManager.getRealm(realmOwner);
        if (realm == null) {
            player.sendMessage("§cDavet bulunamadı.");
            return;
        }

        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(player.getName())).findFirst().orElse(null);
        if (member != null) {
            player.sendMessage("§cZaten bu diyara üyesiniz.");
            return;
        }

        int allowedMemberCount = Settings.getAllowedMemberCount(realm);
        if (realm.members.size() >= allowedMemberCount) {
            player.sendMessage("§cBu diyar üye sınırına ulaştı.");
            invites.remove(invite);
            return;
        }

        RealmMember rankType;
        switch (invite.rank) {
            case "worker":
            case "işçi":
                rankType = RealmMember.WORKER;
                break;
            case "member":
            case "üye":
                rankType = RealmMember.MEMBER;
                break;
            case "manager":
            case "yönetici":
                rankType = RealmMember.MANAGER;
                break;
            default:
                return;
        }

        realm.members.add(new RealmMemberModel(player.getName(), rankType));
        plugin.realmsManager.saveRealm(realm);
        invites.remove(invite);
        player.sendMessage("§aDavet kabul edildi.");
        plugin.realmsManager.messageManager.sendMessage(invite.inviter, Texts.REALM_MEMBER_INVITE_ACCEPTED.clone().addReplacement("{player}", player.getName()));
    }

    private static void handleLeaveCommand(Player player, ImmutableList<String> args) {
        String realmOwner = args.size() > 1 ? args.get(1) : "";
        RealmModel realm = plugin.realmsManager.getRealm(realmOwner);
        if (realm == null) {
            player.sendMessage(Util.getExclamation() + "§cDiyar bulunamadı.");
            return;
        }

        RealmMemberModel member = realm.members.stream().filter(m -> m.name.equals(player.getName())).findFirst().orElse(null);
        if (member == null) {
            player.sendMessage(Util.getExclamation() + "§cBu diyara üye değilsiniz.");
            return;
        }

        realm.members.remove(member);
        plugin.realmsManager.saveRealm(realm);
        player.sendMessage("§aDiyardan ayrıldınız.");
    }

    private static void handleCreateRealm(Player player, String climate) {
        RealmClimate climateType;
        switch (climate) {
            case "karlı":
            case "snowy":
                climateType = RealmClimate.SNOWY;
                break;
            case "karasal":
            case "continental":
                climateType = RealmClimate.COLD;
                break;
            case "çöl":
            case "desert":
                climateType = RealmClimate.HOT_BARREN;
                break;
            case "savan":
            case "savannah":
                climateType = RealmClimate.DRY_VEGETATION;
                break;
            case "nemli":
            case "humid":
                climateType = RealmClimate.HUMID_VEGETATION;
                break;
            default:
                plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.REALM_COMMAND_USAGE_CREATE);
                return;
        }
        RealmModel realm = plugin.realmsManager.getRealm(player.getName());
        if (realm != null) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.ALREADY_HAVE_REALM);
            return;
        }
        plugin.realmsManager.createRealm(player, climateType);
    }

    private static void handleTeleportToRealm(Player player, RealmModel realm) {
        plugin.serversManager.teleportPlayer(player, Util.getLocationFromString(realm.spawn), Settings.REALMS_WORLD_NAME, realm.server);
    }

    private static void handleSetSpawnCommand(Player player, ImmutableList<String> args) {
        String realmOwner = args.size() > 1 ? args.get(1) : player.getName();

        RealmModel realm = plugin.realmsManager.getRealm(realmOwner);
        if (realm == null) {
            player.sendMessage("§cDiyar bulunamadı.");
            return;
        }

        RealmMemberModel member = plugin.realmsManager.getRealmMember(realm.owner, player.getName());
        if (member == null || member.rank != RealmMember.MANAGER) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.YOU_ARE_A_WORKER_SO_YOU_CANT_DO_THAT);
            return;
        }

        realm.spawn = Util.getStringFromLocation(player.getLocation());
        plugin.realmsManager.saveRealm(realm);
        player.sendMessage("§aDiyarın spawn noktası ayarlandı.");
    }

    private static void handleVisitorCommand(Player player, ImmutableList<String> args) {
        String visitorAction = args.size() > 1 ? args.get(1).toLowerCase() : null;
        if (visitorAction == null) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.REALM_COMMAND_USAGE_VISITOR);
            return;
        }

        RealmModel realm1 = plugin.realmsManager.getRealm(player.getName());
        if (realm1 == null) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.DONT_HAVE_REALM);
            return;
        }

        RealmMemberModel member = realm1.members.stream().filter(m -> m.name.equals(player.getName())).findFirst().orElse(null);
        if (member == null || member.rank != RealmMember.MANAGER) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.YOU_ARE_A_WORKER_SO_YOU_CANT_DO_THAT);
            return;
        }

        if (visitorAction.equals("allow") || visitorAction.equals("izin-ver")) {
            realm1.allow_visitors = true;
            plugin.realmsManager.saveRealm(realm1);
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.VISITOR_PERMISSION_CHANGE_SUCCESS);
        } else if (visitorAction.equals("deny") || visitorAction.equals("reddet")) {
            realm1.allow_visitors = false;
            plugin.realmsManager.saveRealm(realm1);
            plugin.realmsManager.kickVisitors(realm1);
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.VISITOR_PERMISSION_CHANGE_SUCCESS);
        } else {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.REALM_COMMAND_USAGE_VISITOR);
        }
    }

    public static void handlePlayerBanCommand(Player player, ImmutableList<String> args) {
        String bannedPlayer = args.size() > 1 ? args.get(1) : null;
        if (bannedPlayer == null) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.REALM_COMMAND_USAGE_BAN);
            return;
        }

        String realmOwner = args.size() > 2 ? args.get(2) : null;
        if (realmOwner == null) {
            realmOwner = player.getName();
        }

        RealmModel realm = plugin.realmsManager.getRealm(realmOwner);
        if (realm == null) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.REALM_COMMAND_USAGE_BAN);
            return;
        }

        RealmMemberModel member = plugin.realmsManager.getRealmMember(realm.owner, player.getName());
        if (member == null || member.rank != RealmMember.MANAGER) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.YOU_ARE_A_WORKER_SO_YOU_CANT_DO_THAT);
            return;
        }

        if (realm.banned_players.size() >= 20) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.YOU_CAN_BAN_ONLY_20_PLAYERS);
            return;
        }

        if (realm.banned_players.contains(bannedPlayer)) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.THIS_PLAYER_ALREADY_BANNED.clone().addReplacement("{player}", bannedPlayer));
            return;
        }

        Player banned = Bukkit.getPlayer(bannedPlayer);
        if (banned != null) {
            int[] region = RegionManager.getRegionFromLocation(banned.getLocation().getBlockX(), banned.getLocation().getBlockZ());
            if (Arrays.equals(region, Util.getRegionCoordinatesFromString(realm.region))) {
                banned.sendMessage("§cDiyar sahibi tarafından yasaklandınız.");
                banned.kick();
            }
        }
        realm.banned_players.add(bannedPlayer);
        plugin.realmsManager.saveRealm(realm);
        plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.MEMBER_BANNED_SUCCESS.clone().addReplacement("{player}", bannedPlayer));
    }

    public static void handlePlayerUnbanCommand(Player player, ImmutableList<String> args) {
        String bannedPlayer = args.size() > 1 ? args.get(1) : null;
        if (bannedPlayer == null) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.REALM_COMMAND_USAGE_UNBAN);
            return;
        }

        String realmOwner = args.size() > 2 ? args.get(2) : null;
        if (realmOwner == null) {
            realmOwner = player.getName();
        }

        RealmModel realm = plugin.realmsManager.getRealm(realmOwner);
        if (realm == null) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.REALM_COMMAND_USAGE_UNBAN);
            return;
        }

        RealmMemberModel member = plugin.realmsManager.getRealmMember(realm.owner, player.getName());
        if (member == null || member.rank != RealmMember.MANAGER) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.YOU_ARE_A_WORKER_SO_YOU_CANT_DO_THAT);
            return;
        }

        if (!realm.banned_players.contains(bannedPlayer)) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.THIS_PLAYER_NOT_BANNED.clone().addReplacement("{player}", bannedPlayer));
            return;
        }

        realm.banned_players.remove(bannedPlayer);
        plugin.realmsManager.saveRealm(realm);
        plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.MEMBER_UNBANNED_SUCCESS.clone().addReplacement("{player}", bannedPlayer));
    }

    public static void handleMemberCommand(Player player, ImmutableList<String> args) {
        String memberAction = args.size() > 1 ? args.get(1) : null;
        if (memberAction == null) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.REALM_COMMAND_USAGE_MEMBER);
            return;
        }

        String memberName = args.size() > 2 ? args.get(2) : null;
        if (memberName == null) {
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.REALM_COMMAND_USAGE_MEMBER);
            return;
        }

        if (memberAction.equals("add") || memberAction.equals("ekle") || memberAction.equals("rütbe") || memberAction.equals("rank")) {
            boolean isRank = memberAction.equals("rütbe") || memberAction.equals("rank");
            String rank = args.size() > 3 ? args.get(3) : null;
            if (rank == null) {
                plugin.realmsManager.messageManager.sendMessage(player.getName(), isRank ? Texts.REALM_COMMAND_USAGE_MEMBER_RANK : Texts.REALM_COMMAND_USAGE_MEMBER_ADD);
                return;
            }

            String realmName = args.size() > 4 ? args.get(4) : player.getName();
            RealmModel realm1 = plugin.realmsManager.getRealm(realmName);
            if (realm1 == null) {
                plugin.realmsManager.messageManager.sendMessage(player.getName(), realmName.equals(player.getName()) ? Texts.DONT_HAVE_REALM : isRank ? Texts.REALM_COMMAND_USAGE_MEMBER_RANK : Texts.REALM_COMMAND_USAGE_MEMBER_ADD);
                return;
            }
            RealmMemberModel member1 = realm1.members.stream().filter(m -> m.name.equals(player.getName())).findFirst().orElse(null);
            if (member1 == null || member1.rank != RealmMember.MANAGER) {
                plugin.realmsManager.messageManager.sendMessage(player.getName(), isRank ? Texts.REALM_COMMAND_USAGE_MEMBER_RANK : Texts.REALM_COMMAND_USAGE_MEMBER_ADD);
                return;
            }
            RealmMember rankType;
            switch (rank) {
                case "worker":
                case "işçi":
                    rankType = RealmMember.WORKER;
                    break;
                case "member":
                case "üye":
                    rankType = RealmMember.MEMBER;
                    break;
                case "manager":
                case "yönetici":
                    rankType = RealmMember.MANAGER;
                    break;
                default:
                    plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.REALM_COMMAND_USAGE_MEMBER_ADD);
                    return;
            }
            if (isRank) {
                RealmMemberModel member = realm1.members.stream().filter(m -> m.name.equals(memberName)).findFirst().orElse(new RealmMemberModel(memberName, rankType));
                member.rank = rankType;
                realm1.members.removeIf(m -> m.name.equals(memberName));
                realm1.members.add(member);
                plugin.realmsManager.saveRealm(realm1);

                plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.MEMBER_RANK_CHANGE_SUCCESS.clone().addReplacement("{player}", memberName));
            } else {
                int allowedMemberCount = Settings.getAllowedMemberCount(realm1);
                if (realm1.members.size() >= allowedMemberCount) {
                    player.sendMessage("§cBu diyar üye sınırına ulaştı.");
                    return;
                }
                InviteModel oldInvite = invites.stream().filter(i -> i.member.equals(memberName) && i.realm.equals(realmName)).findFirst().orElse(null);
                if (oldInvite != null) {
                    player.sendMessage("§cBu oyuncuya zaten davet gönderilmiş.");
                    return;
                }
                InviteModel invite = new InviteModel(realm1.owner, player.getName(), memberName, rank);

                channelAgent.getChannel().sendMessage(invite);
                plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.REALM_MEMBER_INVITED_SUCCESS.clone().addReplacement("{player}", memberName));
                InviteMembersObjective.instance.onPlayerInviteMember(player, 1);
            }
        }

        if (memberAction.equals("remove") || memberAction.equals("çıkar")) {
            String realmName = args.size() > 3 ? args.get(3) : player.getName();
            RealmModel realm1 = plugin.realmsManager.getRealm(realmName);
            if (realm1 == null) {
                plugin.realmsManager.messageManager.sendMessage(player.getName(), realmName.equals(player.getName()) ? Texts.DONT_HAVE_REALM : Texts.REALM_COMMAND_USAGE_MEMBER_REMOVE);
                return;
            }
            RealmMemberModel member1 = realm1.members.stream().filter(m -> m.name.equals(player.getName())).findFirst().orElse(null);
            if (member1 == null || member1.rank != RealmMember.MANAGER) {
                plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.REALM_COMMAND_USAGE_MEMBER_REMOVE);
                return;
            }
            realm1.members.removeIf(m -> m.name.equals(memberName));
            plugin.realmsManager.saveRealm(realm1);
            plugin.realmsManager.messageManager.sendMessage(player.getName(), Texts.MEMBER_RANK_CHANGE_SUCCESS.clone().addReplacement("{player}", memberName));
        }
    }

    private static List<RealmModel> getRealms(Player player) {
        return plugin.realmsManager.realms.values().stream().filter(r -> {
            RealmMemberModel member = r.members.stream().filter(m -> m.name.equals(player.getName())).findFirst().orElse(null);
            return member != null && member.rank == RealmMember.MANAGER;
        }).toList();
    }
}
