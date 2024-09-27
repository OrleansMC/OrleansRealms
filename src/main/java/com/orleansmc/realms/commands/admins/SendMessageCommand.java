package com.orleansmc.realms.commands.admins;

import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.models.config.TextModel;
import me.lucko.helper.Commands;

public class SendMessageCommand {
    public static void setup(OrleansRealms plugin) {
        Commands.create()
                .assertPermission("orleansmc.commands.sendmessage")
                .handler(c -> {
                    if (c.args().size() < 2) {
                        c.reply("Usage: /sendmessage <player> <message>");
                        return;
                    }

                    String target = c.args().getFirst();

                    String message = String.join(" ", c.args().subList(1, c.args().size()));
                    plugin.realmsManager.messageManager.sendMessage(
                            target,
                            new TextModel(
                                    message,
                                    message
                            )
                    );
                    c.reply("Message sent.");
                })
                .registerAndBind(plugin, "sendmessage", "mesaj-gönder");
    }
}
