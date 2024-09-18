package com.orleansmc.realms.managers.common;

import com.orleansmc.common.webhooks.DiscordWebhook;
import com.orleansmc.common.webhooks.WebhookProvider;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.utils.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class WebhookManager {
    public static WebhookProvider webhookProvider = Bukkit.getServicesManager().load(WebhookProvider.class);

    public static void sendRealmCreateWebhook(RealmModel realm) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(realm.owner);
        DiscordWebhook webhook = new DiscordWebhook(Settings.REALM_WEBHOOK_URL);
        webhook.setAvatarUrl("https://mc-heads.net/avatar/" + player.getName() + "/64.png");
        webhook.setUsername(player.getName());
        webhook.addEmbed(
                new DiscordWebhook.EmbedObject()
                        .setTitle("Oyuncu Yeni Bir Diyar Oluşturdu")
                        .setDescription(player.getName() + " isimli oyuncu yeni bir diyar oluşturdu.")
                        .addField("Diyar Adı", realm.owner, true)
                        .addField("Sahip", player.getName(), true)
                        .addField("İklim", realm.climate.name(), false)
                        .addField("Bölge", realm.region, true)
                        .addField("Diyar Sunucusu", realm.server, true)
                        .addField("Tarih", "<t:" + (System.currentTimeMillis() / 1000) + ":F>", false)
                        .setColor(Color.GREEN)
        );
        webhookProvider.sendWebhook(webhook);
    }

    public static void sendRealmDeleteWebhook(RealmModel realm) {
        DiscordWebhook webhook = new DiscordWebhook(Settings.REALM_WEBHOOK_URL);
        OfflinePlayer player = Bukkit.getOfflinePlayer(realm.owner);
        webhook.setAvatarUrl("https://mc-heads.net/avatar/" + player.getName() + "/64.png");
        webhook.setUsername(player.getName());
        webhook.addEmbed(
                new DiscordWebhook.EmbedObject()
                        .setTitle("Oyuncu Bir Diyarı Sildi")
                        .setDescription(player.getName() + " isimli oyuncu bir diyarı sildi.")
                        .addField("Diyar Adı", realm.owner, true)
                        .addField("Sahip", player.getName(), true)
                        .addField("İklim", realm.climate.name(), false)
                        .addField("Bölge", realm.region, true)
                        .addField("Diyar Sunucusu", realm.server, true)
                        .addField("Üyeler", realm.members.stream().collect(StringBuilder::new, (sb, s) -> sb.append(s.name).append(" - ").append(s.rank.name()).append(", "), StringBuilder::append).toString(), false)
                        .addField("Oluşturulma Tarihi", "<t:" + realm.created.getTime() / 1000 + ":F>", true)
                        .addField("Tarih", "<t:" + (System.currentTimeMillis() / 1000) + ":F>", true)
                        .setColor(Color.RED)
        );
        webhookProvider.sendWebhook(webhook);
    }

    public static void sendPlayerDeathWebhook(OfflinePlayer player, String cause, Location location, Inventory inventory) {
        String playerName = player.getName();
        DiscordWebhook webhook = new DiscordWebhook(Settings.DEATH_WEBHOOK_URL);
        webhook.setAvatarUrl("https://mc-heads.net/avatar/" + playerName + "/64.png");
        webhook.setUsername(playerName);
        StringBuilder sb = new StringBuilder();
        sb.append(playerName).append(" isimli oyuncu öldü.");
        if (inventory != null) {
            for (int i = 0; i < inventory.getContents().length; i++) {
                if (inventory.getContents()[i] != null) {
                    sb.append("- ").append(Objects.requireNonNull(inventory.getContents()[i]).getAmount()).append("x ").append(inventory.getContents()[i].getItemMeta().getDisplayName().isEmpty() ? inventory.getContents()[i].getType().name() : inventory.getContents()[i].getItemMeta().getDisplayName());
                }
            }
        }
        System.out.println(sb.toString());
        webhook.addEmbed(
                new DiscordWebhook.EmbedObject()
                        .setTitle("Oyuncu Öldü")
                        .setDescription(sb.substring(0, Math.min(sb.toString().length(), 5000)).replace("\n", " "))
                        .addField("Sebep", cause.isEmpty() ? "Bilinmiyor" : cause, false)
                        .addField("Konum", Util.getStringFromLocation(location), false)
                        .addField("Sunucu", Settings.SERVER_NAME, false)
                        .addField("Tarih", "<t:" + (System.currentTimeMillis() / 1000) + ":F>", false)
                        .setColor(Color.RED)
        );
        webhookProvider.sendWebhook(webhook);
    }

    public static void sendRedstoneExceedLimitWebhook(RealmModel realm) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(realm.owner);
        DiscordWebhook webhook = new DiscordWebhook(Settings.REDSTONE_ALERT_WEBHOOK_URL);
        webhook.setAvatarUrl("https://mc-heads.net/avatar/" + player.getName() + "/64.png");
        webhook.setUsername(player.getName());
        webhook.addEmbed(
                new DiscordWebhook.EmbedObject()
                        .setTitle("Redstone Kısıtlaması Uygulandı")
                        .setDescription(player.getName() + " isimli oyuncunun redstone aktiviteleri kısıtlandı.")
                        .addField("Diyar Adı", realm.owner, true)
                        .addField("Sahip", player.getName(), true)
                        .addField("İklim", realm.climate.name(), false)
                        .addField("Bölge", realm.region, true)
                        .addField("Diyar Sunucusu", realm.server, true)
                        .addField("Tarih", "<t:" + (System.currentTimeMillis() / 1000) + ":F>", false)
                        .setColor(Color.RED)
        );
        webhookProvider.sendWebhook(webhook);
    }
}
