package com.orleansmc.realms.configs.texts;

import com.orleansmc.realms.models.config.TextModel;
import org.bukkit.configuration.file.YamlConfiguration;

public class Texts {
    public static TextModel REALM_CREATION_PENDING;
    public static TextModel ANOTHER_REALM_CREATION_PENDING;
    public static TextModel REALM_QUEUE_REMAINING;
    public static TextModel REALM_CREATION_PROGRESS;
    public static TextModel REALM_CREATION_SUCCESS;
    public static TextModel ALREADY_HAVE_REALM;
    public static TextModel REALM_DELETED_SUCCESS;
    public static TextModel DONT_HAVE_REALM;
    public static TextModel MEMBER_RANK_CHANGE_SUCCESS;
    public static TextModel YOU_ARE_A_WORKER_SO_YOU_CANT_DO_THAT;
    public static TextModel YOU_ARE_BANNED_FROM_REALM;
    public static TextModel THIS_PLAYER_ALREADY_BANNED;
    public static TextModel THIS_PLAYER_NOT_BANNED;
    public static TextModel THIS_REALM_NOT_RECEIVING_VISITORS;
    public static TextModel REALM_COMMAND_USAGE_CREATE;
    public static TextModel REALM_COMMAND_USAGE_TELEPORT;
    public static TextModel REALM_COMMAND_USAGE_BAN;
    public static TextModel REALM_COMMAND_USAGE_UNBAN;
    public static TextModel REALM_COMMAND_USAGE_MEMBER;
    public static TextModel REALM_COMMAND_USAGE_MEMBER_ADD;
    public static TextModel REALM_COMMAND_USAGE_MEMBER_REMOVE;
    public static TextModel REALM_COMMAND_USAGE_MEMBER_RANK;
    public static TextModel REALM_MEMBER_RANKED_SUCCESS;
    public static TextModel REALM_MEMBER_REMOVED_SUCCESS;
    public static TextModel MEMBER_BANNED_SUCCESS;
    public static TextModel MEMBER_UNBANNED_SUCCESS;
    public static TextModel YOU_CAN_BAN_ONLY_20_PLAYERS;
    public static TextModel REALM_MEMBER_INVITED_SUCCESS;
    public static TextModel REALM_MEMBER_INVITE_ACCEPTED;

    public static TextModel REALM_COMMAND_USAGE_VISITOR;
    public static TextModel VISITOR_PERMISSION_CHANGE_SUCCESS;
    public static TextModel REALM_INVITE_TIMEOUT;


    public static void load(YamlConfiguration textFile) {
        REALM_CREATION_PENDING = new TextModel(
                textFile.getString("realm-creation-pending.en"),
                textFile.getString("realm-creation-pending.tr")
        );

        ANOTHER_REALM_CREATION_PENDING = new TextModel(
                textFile.getString("another-realm-creation-pending.en"),
                textFile.getString("another-realm-creation-pending.tr")
        );

        REALM_QUEUE_REMAINING = new TextModel(
                textFile.getString("realm-queue-remaining.en"),
                textFile.getString("realm-queue-remaining.tr")
        );

        REALM_CREATION_PROGRESS = new TextModel(
                textFile.getString("realm-creation-progress.en"),
                textFile.getString("realm-creation-progress.tr")
        );

        REALM_CREATION_SUCCESS = new TextModel(
                textFile.getString("realm-creation-success.en"),
                textFile.getString("realm-creation-success.tr")
        );

        ALREADY_HAVE_REALM = new TextModel(
                textFile.getString("already-have-realm.en"),
                textFile.getString("already-have-realm.tr")
        );

        REALM_DELETED_SUCCESS = new TextModel(
                textFile.getString("realm-deleted-success.en"),
                textFile.getString("realm-deleted-success.tr")
        );

        DONT_HAVE_REALM = new TextModel(
                textFile.getString("dont-have-realm.en"),
                textFile.getString("dont-have-realm.tr")
        );

        MEMBER_RANK_CHANGE_SUCCESS = new TextModel(
                textFile.getString("member-rank-change-success.en"),
                textFile.getString("member-rank-change-success.tr")
        );

        YOU_ARE_A_WORKER_SO_YOU_CANT_DO_THAT = new TextModel(
                textFile.getString("you-are-a-worker-so-you-cant-do-that.en"),
                textFile.getString("you-are-a-worker-so-you-cant-do-that.tr")
        );

        YOU_ARE_BANNED_FROM_REALM = new TextModel(
                textFile.getString("you-are-banned-from-realm.en"),
                textFile.getString("you-are-banned-from-realm.tr")
        );

        THIS_PLAYER_ALREADY_BANNED = new TextModel(
                textFile.getString("this-player-already-banned.en"),
                textFile.getString("this-player-already-banned.tr")
        );

        THIS_REALM_NOT_RECEIVING_VISITORS = new TextModel(
                textFile.getString("this-realm-not-receiving-visitors.en"),
                textFile.getString("this-realm-not-receiving-visitors.tr")
        );

        REALM_COMMAND_USAGE_CREATE = new TextModel(
                textFile.getString("realm-command-usage-create.en"),
                textFile.getString("realm-command-usage-create.tr")
        );

        REALM_COMMAND_USAGE_TELEPORT = new TextModel(
                textFile.getString("realm-command-usage-teleport.en"),
                textFile.getString("realm-command-usage-teleport.tr")
        );

        REALM_COMMAND_USAGE_BAN = new TextModel(
                textFile.getString("realm-command-usage-ban.en"),
                textFile.getString("realm-command-usage-ban.tr")
        );

        REALM_COMMAND_USAGE_UNBAN = new TextModel(
                textFile.getString("realm-command-usage-unban.en"),
                textFile.getString("realm-command-usage-unban.tr")
        );

        REALM_COMMAND_USAGE_MEMBER = new TextModel(
                textFile.getString("realm-command-usage-member.en"),
                textFile.getString("realm-command-usage-member.tr")
        );

        REALM_COMMAND_USAGE_MEMBER_ADD = new TextModel(
                textFile.getString("realm-command-usage-member-add.en"),
                textFile.getString("realm-command-usage-member-add.tr")
        );

        REALM_COMMAND_USAGE_MEMBER_REMOVE = new TextModel(
                textFile.getString("realm-command-usage-member-remove.en"),
                textFile.getString("realm-command-usage-member-remove.tr")
        );

        REALM_COMMAND_USAGE_MEMBER_RANK = new TextModel(
                textFile.getString("realm-command-usage-member-rank.en"),
                textFile.getString("realm-command-usage-member-rank.tr")
        );

        REALM_MEMBER_RANKED_SUCCESS = new TextModel(
                textFile.getString("realm-member-ranked-success.en"),
                textFile.getString("realm-member-ranked-success.tr")
        );

        REALM_MEMBER_REMOVED_SUCCESS = new TextModel(
                textFile.getString("realm-member-removed-success.en"),
                textFile.getString("realm-member-removed-success.tr")
        );

        MEMBER_BANNED_SUCCESS = new TextModel(
                textFile.getString("member-banned-success.en"),
                textFile.getString("member-banned-success.tr")
        );

        MEMBER_UNBANNED_SUCCESS = new TextModel(
                textFile.getString("member-unbanned-success.en"),
                textFile.getString("member-unbanned-success.tr")
        );

        THIS_PLAYER_NOT_BANNED = new TextModel(
                textFile.getString("this-player-not-banned.en"),
                textFile.getString("this-player-not-banned.tr")
        );

        YOU_CAN_BAN_ONLY_20_PLAYERS = new TextModel(
                textFile.getString("you-can-ban-only-20-players.en"),
                textFile.getString("you-can-ban-only-20-players.tr")
        );

        REALM_MEMBER_INVITED_SUCCESS = new TextModel(
                textFile.getString("realm-member-invited-success.en"),
                textFile.getString("realm-member-invited-success.tr")
        );

        REALM_MEMBER_INVITE_ACCEPTED = new TextModel(
                textFile.getString("realm-member-invite-accepted.en"),
                textFile.getString("realm-member-invite-accepted.tr")
        );

        REALM_COMMAND_USAGE_VISITOR = new TextModel(
                textFile.getString("realm-command-usage-visitor.en"),
                textFile.getString("realm-command-usage-visitor.tr")
        );

        VISITOR_PERMISSION_CHANGE_SUCCESS = new TextModel(
                textFile.getString("visitor-permission-change-success.en"),
                textFile.getString("visitor-permission-change-success.tr")
        );

        REALM_INVITE_TIMEOUT = new TextModel(
                textFile.getString("realm-invite-timeout.en"),
                textFile.getString("realm-invite-timeout.tr")
        );
    }
}