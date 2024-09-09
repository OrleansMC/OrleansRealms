package com.orleansmc.realms.managers.common;

import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.managers.realm.RealmsManager;
import com.orleansmc.realms.models.messaging.MessageModel;
import com.orleansmc.realms.models.config.TextModel;
import me.lucko.helper.messaging.Channel;
import me.lucko.helper.messaging.ChannelAgent;
import me.lucko.helper.messaging.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MessageManager {
    public final ChannelAgent<MessageModel> channelAgent;
    public final OrleansRealms plugin;

    public MessageManager(RealmsManager realmsManager) {
        this.plugin = realmsManager.plugin;
        final Messenger messenger = Bukkit.getServer().getServicesManager().load(Messenger.class);
        if (messenger == null) {
            throw new RuntimeException("Messenger service not found");
        }
        final Channel<MessageModel> channel = messenger.getChannel("orleans:message", MessageModel.class);
        this.channelAgent = channel.newAgent();

        channelAgent.addListener((agent, message) -> {
            if (message.target.equals("all")) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.sendMessage(
                            plugin.getComponent(message.textModel.get(player.locale()))
                    );
                });
            } else {
                Player player = Bukkit.getPlayer(message.target);
                if (player != null) {
                    try {
                        plugin.getLogger().info("Target found! Sending message to " + message.target);
                        player.sendMessage(
                                plugin.getComponent(message.textModel.get(player.locale()))
                        );
                    } catch (Exception e) {
                        plugin.getLogger().info("Error sending message to " + message.target);
                        throw e;
                    }
                }
            }
        });
    }

    public void sendMessage(String target, TextModel textModel) {
        MessageModel messageModel = new MessageModel(target, textModel);
        Player player = Bukkit.getPlayer(target);
        if (player != null) {
            player.sendMessage(
                    plugin.getComponent(textModel.get(player.locale()))
            );
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            channelAgent.getChannel().sendMessage(messageModel);
        });
    }
}
